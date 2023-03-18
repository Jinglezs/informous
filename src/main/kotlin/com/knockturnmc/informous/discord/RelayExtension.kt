package com.knockturnmc.informous.discord

import com.knockturnmc.informous.Informous
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.core.entity.channel.TextChannel
import dev.kord.gateway.PrivilegedIntent

class RelayExtension(
    private val informous: Informous
): Extension() {

    override val name: String = "relay"

    @OptIn(PrivilegedIntent::class)
    override suspend fun setup() {
        ephemeralSlashCommand {
            name = "relay"
            description = "Manage the exception relay extension"

            // Register this as a guild-specific command
            val config = informous.config
            if (config.contains("guild-id")) guild(config.getLong("guild-id"))

            slashCommandCheck {
                hasPermission(Permission.Administrator)
            }

            group("channel") {
                description = "Manage the exception relay channels"

                ephemeralSubCommand(::RegisterCommandArguments) {
                    name = "register"
                    description = "Registers a new server relay channel"

                    action {
                        // The parsed channel is of type ResolvedChannel, which does not implement TextChannel
                        val resolvedChannel = arguments.target

                        // Use the channel id to retrieve a TextChannel instance
                        val textChannelId = resolvedChannel.data.id
                        val textChannel = resolvedChannel.kord.getChannel(textChannelId) as TextChannel

                        RelayController.registerExceptionRelay(textChannel)

                        respond {
                            content =
                                "Registered ${textChannel.mention} as an exception relay channel"
                        }
                    }
                }

                ephemeralSubCommand(::RemoveRelayCommandArguments) {
                    name = "remove"
                    description = "Stop relaying exceptions to a text channel"

                    action {
                        // The parsed channel is of type ResolvedChannel, which does not implement TextChannel
                        val resolvedChannel = arguments.target

                        // Use the channel id to retrieve a TextChannel instance
                        val textChannelId = resolvedChannel.data.id
                        val textChannel = resolvedChannel.kord.getChannel(textChannelId) as TextChannel

                        val removed = RelayController.removeExceptionRelay(textChannel)

                        respond {
                            content = if (removed) "${textChannel.mention} will no longer relay exceptions."
                            else "That channel is not a registered exception relay"
                        }
                    }
                }
            }
        }
    }
}