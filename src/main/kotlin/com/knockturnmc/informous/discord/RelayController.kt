package com.knockturnmc.informous.discord

import com.destroystokyo.paper.exception.ServerException
import com.destroystokyo.paper.exception.ServerSchedulerException
import com.knockturnmc.informous.Informous
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.channel.TextChannel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.properties.Delegates

object RelayController {

    private const val CHANNEL_LIST_PATH = "relay.registered_channels"
    private val exceptionChannels: MutableSet<TextChannel> = mutableSetOf()
    private lateinit var informous: Informous

    private var queuedExceptions = mutableListOf<ServerException>()

    // Exceptions may occur while the bot has not completed start-up, or if it disconnects for any reason
    // This will automatically send the queued exceptions when discord is (re)connected
    var discordConnected: Boolean by Delegates.observable(false) { _, _, newValue ->
        if (newValue) {
            informous.pluginScope.launch {
                queuedExceptions.forEach { relayServerException(it) }
                queuedExceptions.clear()
            }
        }
    }

    /**
     * Reads the plugin configuration to determine which discord channels should be registered as relays
     * Should only be called after the bot has connected to Discord
     */
    suspend fun setUp(informous: Informous) {
        this.informous = informous
        val kord = informous.discordBot.kordRef

        informous.config.getLongList(CHANNEL_LIST_PATH)
            .map { kord.getChannel(Snowflake(it)) as TextChannel }
            .forEach(exceptionChannels::add)

        discordConnected = true
    }

    /**
     * Saves changes to the exception relay channels to the plugin configuration file.
     */
    fun serializeRegistry() {
        val channelIds = exceptionChannels.map { it.id.value.toLong() }.toList()
        informous.config.set(CHANNEL_LIST_PATH, channelIds)
        informous.saveConfig()
    }

    /**
     * Registers a channel for exception relaying.
     * @param channel the channel to register
     */
    fun registerExceptionRelay(channel: TextChannel) {
        exceptionChannels.add(channel)
    }

    /**
     * Stop a channel from receiving exception messages permanently.
     * @param channel the channel to unregister
     */
    fun removeExceptionRelay(channel: TextChannel): Boolean = exceptionChannels.remove(channel)

    /**
     * Creates and decorates an embed message with the details of a server exception.
     */
    suspend fun relayServerException(exception: ServerException) {
        val exceptionType = exception.toExceptionType()

        // Ignore duplicate exceptions from repeating bukkit tasks
        if (exception is ServerSchedulerException && DuplicateExceptionIdentifier.isDuplicateTaskException(exception)) {
            return
        }

        // Queue exceptions when discord is disconnected
        if (!informous.discordConnected) {
            queuedExceptions.add(exception)
            return
        }

        exceptionChannels.forEach {
            it.createEmbed {
                this.color = Color(255, 0, 0)
                this.timestamp = Clock.System.now()
                this.description = "${exception.message}\n${exception.rootCause.message ?: "No details provided"}"

                field("Root Exception", false) { exception.rootCause.javaClass.name }

                // Append additional information based on exception type
                exceptionType.embedDecorator.invoke(this, exception)
            }
        }
    }
}

/**
 * Get the exception that was initially thrown. ServerException is used as a wrapper class, so there will generally be
 * at least one nested exception.
 */
val ServerException.rootCause: Throwable
    get() {
        var rootCause: Throwable = this
        while (rootCause.cause != null) {
            rootCause = rootCause.cause!!
        }
        return rootCause
    }