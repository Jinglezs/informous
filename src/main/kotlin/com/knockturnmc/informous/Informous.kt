package com.knockturnmc.informous

import com.knockturnmc.informous.discord.RelayController
import com.knockturnmc.informous.discord.RelayExtension
import com.knockturnmc.informous.minecraft.ExceptionListener
import com.kotlindiscord.kord.extensions.ExtensibleBot
import kotlinx.coroutines.*
import org.bukkit.plugin.java.JavaPlugin

class Informous : JavaPlugin() {

        lateinit var discordBot: ExtensibleBot
            private set

        // A coroutine scope that is only active as long as the plugin is enabled.
        val pluginScope = CoroutineScope(Dispatchers.Default)


    override fun onEnable() {
        server.pluginManager.registerEvents(ExceptionListener(this), this)

        val discordToken = config.getString("discord_token")

        // Do not attempt to start the bot without a token
        if (discordToken.isNullOrEmpty()) {
            logger.warning("Informous Discord features have been disabled - a bot token has not been configured.")
            return
        }

        // Start the discord bot
        runBlocking {
            discordBot = ExtensibleBot(token = config.getString("discord_token")!!) {
                extensions {
                    add { RelayExtension(this@Informous ) }
                }

                plugins { this.enabled = false }
            }

            discordBot.startAsync()
            RelayController.setUp(this@Informous)
        }
    }

    override fun onDisable() {
        if (pluginScope.isActive) pluginScope.cancel() // Cancel all coroutines running in the plugin's scope.
        RelayController.serializeRegistry()
    }

}