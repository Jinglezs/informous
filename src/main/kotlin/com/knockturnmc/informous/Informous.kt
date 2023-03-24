package com.knockturnmc.informous

import com.knockturnmc.informous.discord.DuplicateExceptionIdentifier
import com.knockturnmc.informous.discord.RelayController
import com.knockturnmc.informous.discord.RelayExtension
import com.knockturnmc.informous.minecraft.ExceptionListener
import com.knockturnmc.informous.minecraft.RelayTestCommand
import com.kotlindiscord.kord.extensions.ExtensibleBot
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.bukkit.plugin.java.JavaPlugin

class Informous : JavaPlugin() {

    lateinit var discordBot: ExtensibleBot
        private set

    var discordConnected = false

    // A coroutine scope that is only active as long as the plugin is enabled.
    val pluginScope = CoroutineScope(Dispatchers.IO)

    override fun onLoad() {
        val discordToken = config.getString("discord_token")

        // Do not attempt to start the bot without a token
        if (discordToken.isNullOrEmpty()) {
            logger.warning("Informous Discord features have been disabled - a bot token has not been configured.")
            return
        }

        // Start the discord bot off the main thread
        pluginScope.launch {
            discordBot = ExtensibleBot(token = config.getString("discord_token")!!) {
                extensions {
                    add { RelayExtension(this@Informous) }
                }

                plugins { enabled = false }
            }

            discordBot.start()
        }
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(ExceptionListener(this), this)
        getCommand("relay-test")?.setExecutor(RelayTestCommand(this))

        // Clean the recent exception map periodically
        server.scheduler.runTaskTimer(this, { _ ->
            DuplicateExceptionIdentifier.cleanUp()
        }, 0L, 1_200L)
    }

    override fun onDisable() {
        RelayController.serializeRegistry()

        runBlocking(Dispatchers.IO) {
            discordBot.close()
            if (pluginScope.isActive) pluginScope.cancel() // Cancel all coroutines running in the plugin's scope.
        }
    }

}