package com.knockturnmc.informous.discord

import com.destroystokyo.paper.exception.*
import com.knockturnmc.informous.Informous
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.channel.TextChannel
import kotlinx.datetime.Clock

object RelayController {

    private const val CHANNEL_LIST_PATH = "relay.registered_channels"
    private val exceptionChannels: MutableSet<TextChannel> = mutableSetOf()

    private val config = Informous.instance.config

    /**
     * Reads the plugin configuration to determine which discord channels should be registered as relays
     */
    suspend fun setUp() {
        val kord = Informous.discordBot.kordRef

        config.getLongList(CHANNEL_LIST_PATH)
            .map { kord.getChannel(Snowflake(it)) as TextChannel }
            .forEach(exceptionChannels::add)
    }

    /**
     * Saves changes to the exception relay channels to the plugin configuration file.
     */
    fun serializeRegistry() {
        val channelIds = exceptionChannels.map { it.id.value.toLong() }.toList()
        config.set(CHANNEL_LIST_PATH, channelIds)
        Informous.instance.saveConfig()
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