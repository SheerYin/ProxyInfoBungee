package io.github.yin.proxyinfobungee

import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.event.ServerSwitchEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class ProxyInfoBungeeMain : Plugin(), Listener {

    companion object {
        lateinit var instance: ProxyInfoBungeeMain

        lateinit var pluginName: String
        lateinit var lowercaseName: String
        lateinit var pluginVersion: String
        lateinit var pluginAuthor: String
        lateinit var pluginPrefix: String

        const val pluginChannel = "proxyinfo:channel"

        val logins: MutableSet<String> = mutableSetOf()
    }

    override fun onEnable() {
        instance = this
        pluginName = description.name
        lowercaseName = pluginName.lowercase()
        pluginVersion = description.version
        pluginAuthor = description.author
        pluginPrefix = "§f[§3代理信息§f] "

        proxy.console.sendMessage(TextComponent(pluginPrefix + "插件开始加载 " + pluginVersion))

        proxy.pluginManager.registerListener(this, this)
    }

    override fun onDisable() {
        proxy.console.sendMessage(TextComponent(pluginPrefix + "插件开始卸载 " + pluginVersion))

        proxy.pluginManager.unregisterListener(this)
    }

//    @EventHandler(priority = EventPriority.NORMAL)
//    fun onPostLogin(event: PostLoginEvent) {
//        logins.add(event.player.name)
//    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onServerConnected(event: ServerConnectedEvent) {
        val playerName = event.player.name

        if (playerName !in logins) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            DataOutputStream(byteArrayOutputStream).use { output ->
                val players = proxy.players.joinToString(",") { it.name }
                output.writeUTF("players")
                output.writeUTF(players)
            }
            for ((_, serverInfo) in proxy.servers) {
                if (serverInfo.players.isNotEmpty()) {
                    serverInfo.players.first().server.info.sendData(pluginChannel, byteArrayOutputStream.toByteArray())
                }
            }
            logins.add(playerName)
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        val playerName = event.player.name
        val byteArrayOutputStream = ByteArrayOutputStream()
        DataOutputStream(byteArrayOutputStream).use { output ->
            val players = proxy.players.joinToString(",") { it.name }
            output.writeUTF("players")
            output.writeUTF(players)
        }
        for ((_, serverInfo) in proxy.servers) {
            if (serverInfo.players.isNotEmpty()) {
                serverInfo.players.first().server.info.sendData(pluginChannel, byteArrayOutputStream.toByteArray())
            }
        }
        logins.remove(playerName)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onServerSwitch(event: ServerSwitchEvent) {
        val proxiedPlayer = event.player
        val serverName = proxiedPlayer.server.info.name

        val byteArrayOutputStream = ByteArrayOutputStream()
        DataOutputStream(byteArrayOutputStream).use { output ->
            output.writeUTF("servername")
            output.writeUTF(serverName)
        }
        proxiedPlayer.server.sendData(pluginChannel, byteArrayOutputStream.toByteArray())

        byteArrayOutputStream.reset()
        DataOutputStream(byteArrayOutputStream).use { output ->
            val players = proxy.players.joinToString(",") { it.name }
            output.writeUTF("players")
            output.writeUTF(players)
        }
        proxiedPlayer.server.sendData(pluginChannel, byteArrayOutputStream.toByteArray())
    }


}

/*
// 手动模式
@EventHandler(priority = 3)
fun onPluginMessage(event: PluginMessageEvent) {
    if (event.tag != pluginChannel) {
        return
    }

    val proxiedPlayer = event.receiver as? ProxiedPlayer ?: return
    DataInputStream(ByteArrayInputStream(event.data)).use { input ->
        val action = input.readUTF()
        val byteArrayOutputStream = ByteArrayOutputStream()

        when (action) {
            "servername" -> {
                DataOutputStream(byteArrayOutputStream).use { out ->
                    out.writeUTF("servername")
                    out.writeUTF(proxiedPlayer.server.info.name)
                }
                proxiedPlayer.server.sendData(pluginChannel, byteArrayOutputStream.toByteArray())
            }

            "players" -> {
                val players = proxy.players.joinToString(", ") { it.displayName }
                DataOutputStream(byteArrayOutputStream).use { out ->
                    out.writeUTF("players")
                    out.writeUTF(players)
                }
                proxiedPlayer.server.sendData(pluginChannel, byteArrayOutputStream.toByteArray())
            }
        }

    }
}

 */

