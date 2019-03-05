### Tor Manager
This library manages the Tor Runtime and Configuration

#### Managing Settings
To manage Tor settings, you can use the default TorPropertySettings class that implements TorSettings. 

    val torSettings = PropertyTorSettings()
    torSettings.controlPort = "auto"
    torSettings.hasDebugLogs = true
    torSettings.socksPort = "9070"
    torSettings.torDir = File("tor-install-dir")

Next you can save the properties, to the file system

    torSettings.properties.store(FileOutputStream("props.txt"), "Comments")

To output the settings into a torrc format that Tor runtime will use

    TorConfigBuilder(torSettings).writeConfig(File("tor-install-dir/torrc"))

If you need an alternative TorSettings implementation, say for Android, you can implement this against
a shared preference or a sqlite database.


#### Running Tor
Now that you have a settings file and a generated torrc file, it's time to run Tor.

First create an eventChannel (or actor) to handle the startup events

    fun eventChannel() = GlobalScope.actor<Any> {
        for (message in channel) {
            when (message) {
                is StartOk -> {
                    //tor has started. you can start the tor control connection here
                }
                is StartMessage -> {
                    //messages relating to the startup and bootstrap of tor
                }
                is Bootstrap -> {
                    //tor is bootstrapping
                }
                is StartError -> {
                    //tor failed to start
                }
                else -> { }
        }
    }

Now you are ready to start Tor

    GlobalScope.async {
        startTor(
            torSettings = torSettings,
            event = eventChannel()
    )
    
Note that this method will write out the torrc file prior to starting Tor.
