package org.i2peer.tor.manager

import java.io.File
import java.io.InputStream

interface TorSettings {

    fun controlPortAuto(): Int

    val bridges: InputStream?

    var customTorrc: File

    var controlPort: String

    var entryNodes: String?

    var excludeNodes: String?

    var exitNodes: String?

    var httpTunnelPort: Int?

    val listOfSupportedBridges: String?

    var proxyHost: String?

    var proxyPassword: String?

    var proxyPort: Int?

    var proxySocks5Host: String?

    var proxySocks5ServerPort: Int?

    var proxyType: String?

    var proxyUser: String?

    var reachableAddressPorts: String

    var relayNickname: String

    var relayPort: Int?

    var socksPort: String

    var virtualAddressNetwork: String?

    var isAutomapHostsOnResolve: Boolean

    var isRelay: Boolean

    var disableNetwork: Boolean

    var dnsPort: Int?

    var hasBridges: Boolean

    var hasConnectionPadding: Boolean

    var hasDebugLogs: Boolean

    var hasIsolationAddressFlagForTunnel: Boolean

    var hasOpenProxyOnAllInterfaces: Boolean

    var hasReachableAddress: Boolean

    var hasReducedConnectionPadding: Boolean

    var hasSafeSocks: Boolean

    var hasStrictNodes: Boolean

    var hasTestSocks: Boolean

    /**
     * The Tor install directory
     */
    val torDir: File

    var transPort: Int?

    var useSocks5: Boolean

}
