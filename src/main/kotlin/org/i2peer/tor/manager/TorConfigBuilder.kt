@file:Suppress("unused")

package org.i2peer.tor.manager

import arrow.core.Try
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*

class TorConfigBuilder(private val settings: TorSettings) {

    private var buffer = StringBuffer()

    fun writeConfig(file: File): Try<Boolean> {
        return Try {
            updateAllSettings()
            file.writeText(asString())
            true
        }
    }

    /**
     * Updates the tor config for all methods annotated with SettingsConfig
     */
    @Throws(Exception::class)
    fun updateAllSettings(): TorConfigBuilder {
        javaClass.methods.forEach { method ->
            method.annotations.forEach { annotation ->
                if (annotation is SettingsConfig) {
                    method.invoke(this)
                }
            }
        }
        return this
    }

    fun asString(): String = buffer.toString()

    fun line(value: String): TorConfigBuilder {
        if (!isNullOrEmpty(value)) buffer.append(value).append("\n")
        return this
    }

    fun reset() {
        buffer = StringBuffer()
    }

    @Throws(IOException::class)
    fun configurePluggableTransportsFromSettings(pluggableTransportClient: File?): TorConfigBuilder {
        if (pluggableTransportClient == null || !settings.hasBridges) {
            return this
        }
        if (pluggableTransportClient.exists() && pluggableTransportClient.canExecute()) {
            useBridges()

            val bridges = settings.listOfSupportedBridges ?: return this
            if (bridges.contains("obfs3") || bridges.contains("obfs4")) {
                transportPluginObfs(pluggableTransportClient.canonicalPath)
            }
            if (bridges.contains("meek")) {
                transportPluginMeek(pluggableTransportClient.canonicalPath)
            }

            if (bridges.length > 5) {
                for (bridge in bridges.split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                    line("Bridge $bridge")
                }
            } else {
                val type = if (bridges.contains("meek")) "meek_lite" else "obfs4"
                addBridgesFromSettings(type, 2)
            }
        } else {
            throw IOException(
                "Bridge binary does not exist: " + pluggableTransportClient
                    .canonicalPath
            )
        }

        return this
    }

    @SettingsConfig
    fun automapHostsOnResolveFromSettings(): TorConfigBuilder =
        if (settings.isAutomapHostsOnResolve) automapHostsOnResolve() else this

    @SettingsConfig
    fun connectionPaddingFromSettings(): TorConfigBuilder {
        return if (settings.hasConnectionPadding) connectionPadding() else this
    }

    @SettingsConfig
    fun controlPortWriteToFileFromConfig(): TorConfigBuilder = controlPortWriteToFile(
        settings.controlPort,
        File(settings.torDir, "control.txt")
    )

    @SettingsConfig
    fun debugLogsFromSettings(): TorConfigBuilder {
        return if (settings.hasDebugLogs) debugLogs() else this
    }

    @SettingsConfig
    fun disableNetworkFromSettings(): TorConfigBuilder {
        return if (settings.disableNetwork) disableNetwork() else this
    }

    @SettingsConfig
    fun dnsPortFromSettings(): TorConfigBuilder = dnsPort(settings.dnsPort)

    @SettingsConfig
    fun httpTunnelPortFromSettings(): TorConfigBuilder {
        if (settings.httpTunnelPort != null) return this
        return httpTunnelPort(
            settings.httpTunnelPort,
            if (settings.hasIsolationAddressFlagForTunnel) "IsolateDestAddr" else null
        )
    }

    /**
     * Adds non exit relay to builder. This method uses a default google nameserver.
     */
    /*
    @SettingsConfig
    fun nonExitRelayFromSettings(): TorConfigBuilder {
        if (!settings.hasReachableAddress() && !settings.hasBridges() && settings.isRelay) {
            try {
                val resolv = context.createGoogleNameserverFile()
                makeNonExitRelay(
                    resolv.getCanonicalPath(), settings.relayPort, settings
                        .relayNickname
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return this
    }
    */

    /**
     * Sets the entry/exit/exclude nodes
     */
    @SettingsConfig
    fun nodesFromSettings(): TorConfigBuilder {
        entryNodes(settings.entryNodes).exitNodes(settings.exitNodes)
            .excludeNodes(settings.excludeNodes)
        return this
    }

    @SettingsConfig
    fun proxyOnAllInterfacesFromSettings(): TorConfigBuilder {
        return if (settings.hasOpenProxyOnAllInterfaces) proxyOnAllInterfaces() else this
    }

    @SettingsConfig
    fun proxySocks5FromSettings(): TorConfigBuilder {
        return if (settings.useSocks5 && !settings.hasBridges && !isNullOrEmpty(settings.proxySocks5Host)
            && settings.proxySocks5ServerPort != null
        )
            proxySocks5(
                settings
                    .proxySocks5Host,
                settings.proxySocks5ServerPort
            )
        else
            this
    }

    @SettingsConfig
    fun proxyWithAuthenticationFromSettings(): TorConfigBuilder {
        return if (!settings.useSocks5 && !settings.hasBridges && !isNullOrEmpty(settings.proxyType) &&
            !isNullOrEmpty(settings.proxyHost) && settings.proxyPort != null
        )
            proxyWithAuthentication(
                settings.proxyType, settings.proxyHost,
                settings.proxyPort, settings.proxyUser, settings
                    .proxyPassword
            )
        else
            this
    }

    @SettingsConfig
    fun reachableAddressesFromSettings(): TorConfigBuilder {
        return if (settings.hasReachableAddress)
            reachableAddressPorts(
                settings
                    .reachableAddressPorts
            )
        else
            this
    }

    @SettingsConfig
    fun reducedConnectionPaddingFromSettings(): TorConfigBuilder {
        return if (settings.hasReducedConnectionPadding) reducedConnectionPadding() else this
    }

    @SettingsConfig
    fun safeSocksFromSettings(): TorConfigBuilder {
        return if (!settings.hasSafeSocks) safeSocksDisable() else this
    }

    @SettingsConfig
    fun socksPortFromSettings(): TorConfigBuilder {
        var socksPort = settings.socksPort
        if (socksPort.indexOf(':') != -1) {
            socksPort = socksPort.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        }

        if (!socksPort.equals("auto", ignoreCase = true) && isLocalPortOpen(socksPort.toInt())) {
            socksPort = "auto"
        }
        return socksPort(
            socksPort, if (settings.hasIsolationAddressFlagForTunnel)
                "IsolateDestAddr"
            else
                null
        )
    }

    @SettingsConfig
    fun strictNodesFromSettings(): TorConfigBuilder =
        if (settings.hasStrictNodes) strictNodesEnable() else strictNodesDisable()


    @SettingsConfig
    fun testSocksFromSettings(): TorConfigBuilder {
        return if (!settings.hasTestSocks) testSocksDisable() else this
    }

    @SettingsConfig
    fun transPortFromSettings(): TorConfigBuilder = transPort(settings.transPort)

    @SettingsConfig
    fun useBridgesFromSettings(): TorConfigBuilder {
        return if (!settings.hasBridges) dontUseBridges() else this
    }

    @SettingsConfig
    fun virtualAddressNetworkFromSettings(): TorConfigBuilder = virtualAddressNetwork(settings.virtualAddressNetwork)

    /**
     * Add bridges from bridges.txt file.
     */
    private fun addBridges(input: InputStream?, bridgeType: String, maxBridges: Int): TorConfigBuilder {
        if (input == null || isNullOrEmpty(bridgeType) || maxBridges < 1) {
            return this
        }
        val bridges = ArrayList<Bridge>()
        try {
            val br = BufferedReader(InputStreamReader(input, "UTF-8"))
            var line: String? = br.readLine()
            while (line != null) {
                val tokens = line.split(" ".toRegex(), 1).toTypedArray()
                if (tokens.size != 2) {
                    line = br.readLine()
                    continue//bad entry
                }
                bridges.add(Bridge(tokens[0], tokens[1]))
                line = br.readLine()
            }
            br.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        bridges.shuffle(Random(System.nanoTime()))
        var bridgeCount = 0
        for (b in bridges) {
            if (b.type == bridgeType) {
                bridge(b.type, b.config)
                if (++bridgeCount > maxBridges)
                    break
            }
        }
        return this
    }

    private fun addBridgesFromSettings(type: String, maxBridges: Int): TorConfigBuilder =
        addBridges(settings.bridges, type, maxBridges)

    private fun automapHostsOnResolve(): TorConfigBuilder {
        buffer.append("AutomapHostsOnResolve 1").append('\n')
        return this
    }

    private fun bridge(type: String, config: String): TorConfigBuilder {
        if (!isNullOrEmpty(type) && !isNullOrEmpty(config)) {
            buffer.append("Bridge ").append(type).append(' ').append(config).append('\n')
        }
        return this
    }

    private fun connectionPadding(): TorConfigBuilder {
        buffer.append("ConnectionPadding 1").append('\n')
        return this
    }

    private fun controlPortWriteToFile(controlPort: String, controlPortFile: File): TorConfigBuilder {
        buffer.append("ControlPort $controlPort\n")
        buffer.append("ControlPortWriteToFile ").append(controlPortFile.absolutePath).append('\n')
        return this
    }

    private fun debugLogs(): TorConfigBuilder {
        buffer.append("Log debug syslog\n")
        buffer.append("Log info syslog\n")
        buffer.append("SafeLogging 0\n")
        return this
    }

    private fun disableNetwork(): TorConfigBuilder {
        buffer.append("DisableNetwork 0\n")
        return this
    }

    private fun dnsPort(dnsPort: Int?): TorConfigBuilder {
        if (dnsPort != null) buffer.append("DNSPort ").append(dnsPort).append('\n')
        return this
    }

    private fun dontUseBridges(): TorConfigBuilder {
        buffer.append("UseBridges 0\n")
        return this
    }

    private fun entryNodes(entryNodes: String?): TorConfigBuilder {
        if (!isNullOrEmpty(entryNodes))
            buffer.append("EntryNodes ").append(entryNodes).append('\n')
        return this
    }

    private fun excludeNodes(excludeNodes: String?): TorConfigBuilder {
        if (!isNullOrEmpty(excludeNodes))
            buffer.append("ExcludeNodes ").append(excludeNodes).append('\n')
        return this
    }

    private fun exitNodes(exitNodes: String?): TorConfigBuilder {
        if (!isNullOrEmpty(exitNodes))
            buffer.append("ExitNodes ").append(exitNodes).append('\n')
        return this
    }

    private fun geoIpFile(path: String): TorConfigBuilder {
        if (!isNullOrEmpty(path)) buffer.append("GeoIPFile ").append(path).append('\n')
        return this
    }

    private fun geoIpV6File(path: String): TorConfigBuilder {
        if (!isNullOrEmpty(path)) buffer.append("GeoIPv6File ").append(path).append('\n')
        return this
    }

    private fun httpTunnelPort(port: Int?, isolationFlags: String?): TorConfigBuilder {
        if (port == null) return this
        buffer.append("HTTPTunnelPort ").append(port)
        if (!isNullOrEmpty(isolationFlags)) {
            buffer.append(" ").append(isolationFlags)
        }
        buffer.append('\n')
        return this
    }

    private fun makeNonExitRelay(dnsFile: String, orPort: Int, nickname: String): TorConfigBuilder {
        with(buffer) {
            append("ServerDNSResolvConfFile ").append(dnsFile).append('\n')
            append("ORPort ").append(orPort).append('\n')
            append("Nickname ").append(nickname).append('\n')
            append("ExitPolicy reject *:*").append('\n')
        }
        return this
    }

    private fun proxyOnAllInterfaces(): TorConfigBuilder {
        buffer.append("SocksListenAddress 0.0.0.0\n")
        return this
    }

    /**
     * Set socks5 proxy with no authentication. This can be set if you are using a VPN.
     */
    private fun proxySocks5(host: String?, port: Int?): TorConfigBuilder {
        if (!isNullOrEmpty(host) && port != null) buffer.append("socks5Proxy ").append(host).append(':').append(port).append(
            '\n'
        )
        return this
    }

    /**
     * Sets proxyWithAuthentication information. If proxyType, proxyHost or proxyPort is empty,
     * then this method does nothing.
     */
    private fun proxyWithAuthentication(
        proxyType: String?,
        proxyHost: String?,
        proxyPort: Int?,
        proxyUser: String?,
        proxyPass: String?
    ): TorConfigBuilder {
        if (!isNullOrEmpty(proxyType) && !isNullOrEmpty(proxyHost) && proxyPort != null) {
            buffer.append(proxyType).append("Proxy ").append(proxyHost).append(':').append(proxyPort).append('\n')

            if (proxyUser != null && proxyPass != null) {
                if (proxyType.equals("socks5", ignoreCase = true)) {
                    buffer.append("Socks5ProxyUsername ").append(proxyUser).append('\n')
                    buffer.append("Socks5ProxyPassword ").append(proxyPass).append('\n')
                } else {
                    buffer.append(proxyType).append("ProxyAuthenticator ").append(proxyUser)
                        .append(':').append(proxyPort).append('\n')
                }
            } else if (proxyPass != null) {
                buffer.append(proxyType).append("ProxyAuthenticator ").append(proxyUser)
                    .append(':').append(proxyPort).append('\n')
            }
        }
        return this
    }

    private fun reachableAddressPorts(reachableAddressesPorts: String): TorConfigBuilder {
        if (!isNullOrEmpty(reachableAddressesPorts))
            buffer.append("ReachableAddresses ").append(reachableAddressesPorts).append('\n')
        return this
    }

    private fun reducedConnectionPadding(): TorConfigBuilder {
        buffer.append("ReducedConnectionPadding 1\n")
        return this
    }

    private fun safeSocksDisable(): TorConfigBuilder {
        buffer.append("SafeSocks 0\n")
        return this
    }

    private fun safeSocksEnable(): TorConfigBuilder {
        buffer.append("SafeSocks 1\n")
        return this
    }

    private fun socksPort(socksPort: String, isolationFlag: String?): TorConfigBuilder {
        if (isNullOrEmpty(socksPort)) {
            return this
        }
        buffer.append("SOCKSPort ").append(socksPort)
        if (!isNullOrEmpty(isolationFlag)) {
            buffer.append(" ").append(isolationFlag)
        }
        buffer.append('\n')
        return this
    }

    private fun strictNodesDisable(): TorConfigBuilder {
        buffer.append("StrictNodes 0\n")
        return this
    }

    private fun strictNodesEnable(): TorConfigBuilder {
        buffer.append("StrictNodes 1\n")
        return this
    }

    private fun testSocksDisable(): TorConfigBuilder {
        buffer.append("TestSocks 0\n")
        return this
    }

    private fun testSocksEnable(): TorConfigBuilder {
        buffer.append("TestSocks 1\n")
        return this
    }

    private fun transPort(transPort: Int?): TorConfigBuilder {
        if (transPort != null)
            buffer.append("TransPort ").append(transPort).append('\n')
        return this
    }

    private fun transportPluginMeek(clientPath: String): TorConfigBuilder {
        buffer.append("ClientTransportPlugin meek_lite exec ").append(clientPath).append('\n')
        return this
    }

    private fun transportPluginObfs(clientPath: String): TorConfigBuilder {
        buffer.append("ClientTransportPlugin obfs3 exec ").append(clientPath).append('\n')
        buffer.append("ClientTransportPlugin obfs4 exec ").append(clientPath).append('\n')
        return this
    }

    private fun useBridges(): TorConfigBuilder {
        buffer.append("UseBridges 1").append('\n')
        return this
    }

    private fun virtualAddressNetwork(address: String?): TorConfigBuilder {
        if (!isNullOrEmpty(address))
            buffer.append("VirtualAddrNetwork ").append(address).append('\n')
        return this
    }

    private fun isNullOrEmpty(value: String?): Boolean = (value == null || value.isEmpty())

    private fun isLocalPortOpen(port: Int): Boolean {
        return try {
            Socket().apply {
                connect(InetSocketAddress("127.0.0.1", port), 500)
                close()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private data class Bridge(internal val type: String, internal val config: String)
}
