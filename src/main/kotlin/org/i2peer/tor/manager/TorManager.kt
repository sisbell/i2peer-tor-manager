package org.i2peer.tor.manager

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

data class Bootstrap(val percentage: Double)
data class EventError(val message: String, val e: Throwable? = null)
data class EventMessage(val text: String)
data class StartOk(val message: String, val torProcess: Process)
data class StartProcessMessage(val message: String)
data class TorStartData(val torDir: File, val torSettings: TorSettings, val event: SendChannel<Any>)

@ObsoleteCoroutinesApi
suspend fun startTor(installDir: File, torSettings: TorSettings, event: SendChannel<Any>) =
    torConfigWriter().send(TorStartData(installDir, torSettings, event))

@ObsoleteCoroutinesApi
private fun torConfigWriter() = GlobalScope.actor<TorStartData> {
    val data = channel.receive()
    data.event.send(EventMessage("TorConfigWriter"))
    val builder = TorConfigBuilder(data.torSettings)
    builder.writeConfig(data.torSettings.customTorrc).fold(
        {
            data.event.send(EventError("Failed to write config files", it))
        }, {
            data.event.send(EventMessage("Wrote config file ${data.torSettings.customTorrc}"))
            torStarter().send(data)
        })
    channel.cancel()
}

@ObsoleteCoroutinesApi
private fun torStarter() = GlobalScope.actor<TorStartData> {
    val data = channel.receive()
    val event = data.event

    event.send(EventMessage("Starter: ${data.torSettings.customTorrc}"))

    val processBuilder = ProcessBuilder(runTorArgs(File(data.torDir, "tor"), data.torSettings.customTorrc))
    val environment = processBuilder.environment()
    environment["LD_LIBRARY_PATH"] = data.torDir.absolutePath
    event.send(StartProcessMessage("Starting tor with the following commands: ${processBuilder.command()}"))
    val process = processBuilder.start()

    process.waitFor(5000, TimeUnit.MILLISECONDS)
    if (!process.isAlive) {
        val baos = ByteArrayOutputStream()
        process.errorStream.copyTo(baos)
        println("ERROR:$baos")
        event.send(EventError("Tor targetProcess failed to start: ${processBuilder.command()}"))
        return@actor
    }

    process.inputStream.bufferedReader().lines().forEach {
        when {
            it.contains("Bootstrapped ") -> {
                val result = boostrapRegex.find(it)
                val progress = result!!.groups[1]!!.value.toDouble()
                runBlocking {
                    event.send(
                        if (progress == 100.toDouble()) {
                            StartOk("OK", process)
                        } else Bootstrap(progress)
                    )
                }
            }
            it.contains("[err]") -> {
                runBlocking {
                    event.send(EventError("Tor targetProcess failed to start: $it"))
                }
                process.destroy()
            }
        }
        System.out.println(it)
        runBlocking {
            event.send(StartProcessMessage(it))
        }
    }
}

private fun runTorArgs(torExecutableFile: File, torrcFile: File): List<String> {
    return listOf(torExecutableFile.absolutePath, "-f", torrcFile.absolutePath)
}

private val boostrapRegex = "Bootstrapped (.*)%".toRegex()
