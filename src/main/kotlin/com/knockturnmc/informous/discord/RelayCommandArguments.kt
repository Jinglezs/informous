package com.knockturnmc.informous.discord

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import dev.kord.common.entity.ChannelType

internal class RegisterCommandArguments: Arguments() {
    val target by channel {
        name = "channel"
        description = "The channel to begin relaying exceptions to"
        requireChannelType(ChannelType.GuildText)
    }
}

internal class RemoveRelayCommandArguments: Arguments() {
    val target by channel {
        name = "channel"
        description = "The channel to stop relaying exceptions to"
        requireChannelType(ChannelType.GuildText)
    }
}