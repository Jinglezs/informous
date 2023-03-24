package com.knockturnmc.informous.minecraft

import com.knockturnmc.informous.Informous
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand

class RelayTestCommand(
    private val informous: Informous
): Command("relay-test"), CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.isOp || args.isNullOrEmpty()) return false

        when (args[0]) {
            "duplicate" -> {
               val task = informous.server.scheduler.runTaskTimer(informous, Runnable {
                   throw RuntimeException("Testing duplicate exception detection!")
               }, 0L, 20L)

               informous.server.scheduler.runTaskLater(informous, Runnable { task.cancel() }, 60L)
            }
            else -> throw RuntimeException("Testing exception relay!")
        }

        return true
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>?): Boolean {
        return this.onCommand(sender, this, commandLabel, args);
    }

}