package com.knockturnmc.informous

import com.knockturnmc.informous.minecraft.ExceptionListener
import com.knockturnmc.informous.discord.RelayController
import com.knockturnmc.informous.discord.RelayExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot
import kotlinx.coroutines.*
import org.bukkit.plugin.java.JavaPlugin

class Informous : JavaPlugin() {

    companion object {
        lateinit var instance: Informous
            private set

        lateinit var discordBot: ExtensibleBot
            private set

        // A coroutine scope that is only active as long as the plugin is enabled.
        val pluginScope = CoroutineScope(Dispatchers.Default)
    }

    override fun onEnable() {
        instance = this
        server.pluginManager.registerEvents(ExceptionListener(), this)

        // Start the discord bot
        runBlocking {
            discordBot = ExtensibleBot(token = config.getString("discord_token")!!) {
                extensions {
                    add(::RelayExtension)
                }

                plugins { this.enabled = false }
            }

            discordBot.startAsync()
            RelayController.setUp()
        }
    }

    override fun onDisable() {
        if (pluginScope.isActive) pluginScope.cancel() // Cancel all coroutines running in the plugin's scope.
        RelayController.serializeRegistry()
    }

}