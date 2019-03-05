package org.i2peer.tor.manager

import java.io.File
import java.io.InputStream
import java.util.*

class PropertyTorSettings : TorSettings {

    val properties: Properties = Properties()

    override fun controlPortAuto() = File(torDir, "control.txt").readText().split(":")[1].trim().toInt()

    override var controlPort: String
        get() = properties.getProperty("controlPort", "auto")
        set(value) {
            add("controlPort", value)
        }

    override val bridges: InputStream?
        get() = null

    override var customTorrc: File
        get() = File(properties.getProperty("customTorrc", "$torDir/torrc"))
        set(value) {
            add("customTorrc", value.absolutePath)
        }

    override var entryNodes: String?
        get() = properties.getProperty("entryNodes")
        set(value) {
            add("entryNodes", value)
        }

    override var excludeNodes: String?
        get() = properties.getProperty("excludeNode")
        set(value) {
            add("excludeNodes", value)
        }

    override var exitNodes: String?
        get() = properties.getProperty("exitNodes")
        set(value) {
            add("exitNodes", value)
        }

    override var httpTunnelPort: Int?
        get() = properties.getProperty("httpTunnelPort")?.toInt()
        set(value) {
            add("httpTunnelPort", value)
        }

    override val listOfSupportedBridges: String?
        get() = null

    override var proxyHost: String?
        get() = properties.getProperty("proxyHost")
        set(value) {
            add("proxyHost", value)
        }

    override var proxyPassword: String?
        get() = properties.getProperty("proxyPassword")
        set(value) {
            add("proxyPassword", value)
        }

    override var proxyPort: Int?
        get() = properties.getProperty("proxyPort")?.toInt()
        set(value) {
            add("proxyPort", value)
        }

    override var proxySocks5Host: String?
        get() = properties.getProperty("proxySocks5Host")
        set(value) {
            add("proxySocks5Host", value)
        }

    override var proxySocks5ServerPort: Int?
        get() = properties.getProperty("proxySocks5ServerPort")?.toInt()
        set(value) {
            add("proxySocks5ServerPort", value)
        }

    override var proxyType: String?
        get() = properties.getProperty("proxyType")
        set(value) {
            add("proxyType", value)
        }

    override var proxyUser: String?
        get() = properties.getProperty("proxyUser")
        set(value) {
            add("proxyUser", value)
        }

    override var reachableAddressPorts: String
        get() = properties.getProperty("reachableAddressPorts", "*:80,*:443")
        set(value) {
            add("reachableAddressPorts", value)
        }

    override var relayNickname: String
        get() = properties.getProperty("relayNickname", "i2peerRelay")
        set(value) {
            add("relayNickname", value)
        }

    override var relayPort: Int?
        get() = properties.getProperty("relayPort")?.toInt()
        set(value) {
            add("relayPort", value)
        }

    override var socksPort: String
        get() = properties.getProperty("socksPort", "auto")
        set(value) {
            add("socksPort", value)
        }

    override var virtualAddressNetwork: String?
        get() = properties.getProperty("virtualAddressNetwork")
        set(value) {
            add("virtualAddressNetwork", value)
        }

    override var isAutomapHostsOnResolve: Boolean
        get() = properties.getProperty("isAutomapHostsOnResolve", "true")!!.toBoolean()
        set(value) {
            add("isAutomapHostsOnResolve", value.toString())
        }

    override var isRelay: Boolean
        get() = properties.getProperty("isRelay", "false")!!.toBoolean()
        set(value) {
            properties.setProperty("isRelay", value.toString())
        }

    override var disableNetwork: Boolean
        get() = properties.getProperty("disableNetwork", "false")!!.toBoolean()
        set(value) {
            properties.setProperty("disableNetwork", value.toString())
        }

    override var dnsPort: Int?
        get() = properties.getProperty("dnsPort")?.toInt()
        set(value) {
            add("dnsPort", value)
        }

    override var hasBridges: Boolean
        get() = properties.getProperty("hasBridges", "false")!!.toBoolean()
        set(value) {
            properties.setProperty("hasBridges", value.toString())
        }

    override var hasConnectionPadding: Boolean
        get() = properties.getProperty("hasConnectionPadding", "false")!!.toBoolean()
        set(value) {
            properties.setProperty("hasConnectionPadding", value.toString())
        }

    override var hasDebugLogs: Boolean
        get() = properties.getProperty("hasDebugLogs", "false")!!.toBoolean()
        set(value) {
            properties.setProperty("hasDebugLogs", value.toString())
        }

    override var hasIsolationAddressFlagForTunnel: Boolean
        get() = properties.getProperty("hasIsolationAddressFlagForTunnel", "false")!!.toBoolean()
        set(value) {
            properties.setProperty("hasIsolationAddressFlagForTunnel", value.toString())
        }

    override var hasOpenProxyOnAllInterfaces: Boolean
        get() = properties.getProperty("hasOpenProxyOnAllInterfaces", "false")!!.toBoolean()
        set(value) {
            properties.setProperty("hasOpenProxyOnAllInterfaces", value.toString())
        }

    override var hasReachableAddress: Boolean
        get() = properties.getProperty("hasReachableAddress", "false")!!.toBoolean()
        set(value) {
            properties.setProperty("hasReachableAddress", value.toString())
        }

    override var hasReducedConnectionPadding: Boolean
        get() = properties.getProperty("hasReducedConnectionPadding", "true")!!.toBoolean()
        set(value) {
            properties.setProperty("hasReducedConnectionPadding", value.toString())
        }

    override var hasSafeSocks: Boolean
        get() = properties.getProperty("hasSafeSocks", "false")!!.toBoolean()
        set(value) {
            properties.setProperty("hasSafeSocks", value.toString())
        }

    override var hasStrictNodes: Boolean
        get() = properties.getProperty("hasStrictNodes", "false")!!.toBoolean()
        set(value) {
            properties.setProperty("hasStrictNodes", value.toString())
        }

    override var hasTestSocks: Boolean
        get() = properties.getProperty("hasTestSocks", "false")!!.toBoolean()
        set(value) {
            properties.setProperty("hasTestSocks", value.toString())
        }

    override var torDir: File
        get() = File(properties.getProperty("torDir", "."))
        set(value) {
            add("torDir", value.absolutePath)
        }

    override var transPort: Int?
        get() = properties.getProperty("transPort")?.toInt()
        set(value) {
            add("transPort", value)
        }

    override var useSocks5: Boolean
        get() = properties.getProperty("useSocks5", "false")!!.toBoolean()
        set(value) {
            properties.setProperty("useSocks5", value.toString())
        }

    private fun add(key: String, value: String?) {
        if (value != null && !value.isEmpty())
            properties.setProperty(key, value)
        else properties.remove(key)
    }

    private fun add(key: String, value: Int?) {
        if (value != null)
            properties.setProperty(key, value.toString())
        else properties.remove(key)
    }
}