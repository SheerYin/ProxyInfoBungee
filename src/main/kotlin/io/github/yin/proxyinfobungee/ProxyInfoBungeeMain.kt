package io.github.yin.proxyinfobungee

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class ProxyInfoBungeeMain : Plugin(), Listener {

    companion object {
        lateinit var instance: ProxyInfoBungeeMain
        const val prefix = "§f[§3代理信息§f] "
        const val pluginChannel = "proxyinfo:channel"

        val logins: MutableSet<String> = mutableSetOf()
    }

    override fun onEnable() {
        instance = this
        proxy.console.sendMessage(TextComponent(prefix + "插件开始加载 " + description.version))

        proxy.pluginManager.registerListener(this, this)
    }

    override fun onDisable() {
        proxy.console.sendMessage(TextComponent(prefix + "插件开始卸载 " + description.version))

        proxy.pluginManager.unregisterListener(this)
    }

    @EventHandler(priority = 3)
    fun onPostLogin(event: PostLoginEvent) {
        logins.add(event.player.displayName)
    }

    @EventHandler(priority = 3)
    fun onServerConnected(event: ServerConnectedEvent) {
        val playerName = event.player.displayName

        if (playerName in logins) {
            ProxyServer.getInstance().scheduler.runAsync(instance) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                DataOutputStream(byteArrayOutputStream).use { out ->
                    val players = proxy.players.joinToString(", ") { it.displayName }
                    out.writeUTF("players")
                    out.writeUTF(players)
                }
                for ((_, serverInfo) in proxy.servers) {
                    if (serverInfo.players.isNotEmpty()) {
                        serverInfo.players.first().server.info.sendData(pluginChannel, byteArrayOutputStream.toByteArray())
                    }
                }
            }
            logins.remove(playerName)
        }
    }

    @EventHandler(priority = 3)
    fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        val playerName = event.player.displayName
        ProxyServer.getInstance().scheduler.runAsync(instance) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            DataOutputStream(byteArrayOutputStream).use { out ->
                val players = proxy.players.joinToString(", ") { it.displayName }
                out.writeUTF("players")
                out.writeUTF(players)
            }
            for ((_, serverInfo) in proxy.servers) {
                if (serverInfo.players.isNotEmpty()) {
                    serverInfo.players.first().server.info.sendData(pluginChannel, byteArrayOutputStream.toByteArray())
                }
            }
        }
        logins.remove(playerName)
    }

    @EventHandler(priority = 3)
    fun onServerSwitch(event: ServerSwitchEvent) {
        val proxiedPlayer = event.player
        val serverName = proxiedPlayer.server.info.name

        val byteArrayOutputStream = ByteArrayOutputStream()
        DataOutputStream(byteArrayOutputStream).use { out ->
            out.writeUTF("servername")
            out.writeUTF(serverName)
        }
        proxiedPlayer.server.sendData(pluginChannel, byteArrayOutputStream.toByteArray())

        byteArrayOutputStream.reset()
        DataOutputStream(byteArrayOutputStream).use { out ->
            val players = proxy.players.joinToString(", ") { it.displayName }
            out.writeUTF("players")
            out.writeUTF(players)
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

