package com.knockturnmc.informous.minecraft

import com.knockturnmc.informous.Informous
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitTask

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

            "multiple-tasks" -> {
                val runnable = Runnable {
                    throw RuntimeException("Testing multi-task duplicate exceptions")
                }

                val scheduledTasks = arrayOfNulls<BukkitTask>(3)

                (0..2).forEach {
                    scheduledTasks[it] = informous.server.scheduler.runTaskTimer(informous, runnable, 0L, 20L)
                }

                informous.server.scheduler.runTaskLater(informous, Runnable {
                    scheduledTasks.forEach { it?.cancel() }
                }, 60L)
            }

            "max-char-limit" -> {
                var exception = RuntimeException("Big boi nested exception")
                (0..12).forEach { exception = RuntimeException("Wrapper exception $it", exception) }
                throw exception
            }

            else -> throw RuntimeException("Testing exception relay!")
        }

        return true
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>?): Boolean {
        return this.onCommand(sender, this, commandLabel, args)
    }

}