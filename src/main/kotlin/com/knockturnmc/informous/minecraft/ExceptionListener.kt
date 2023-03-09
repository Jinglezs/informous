package com.knockturnmc.informous.minecraft

import com.destroystokyo.paper.event.server.ServerExceptionEvent
import com.knockturnmc.informous.Informous
import com.knockturnmc.informous.discord.RelayController
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ExceptionListener: Listener {
    @EventHandler
    fun onServerException(event: ServerExceptionEvent) {
        // Send an embed message using the plugin's coroutine scope.
        Informous.pluginScope.launch {
            RelayController.relayServerException(event.exception)
        }
    }

}