package com.knockturnmc.informous.discord

import com.destroystokyo.paper.exception.*
import dev.kord.rest.builder.message.EmbedBuilder

/**
 * Defines the types of server exceptions that may be thrown, as well as the information they add to a relayed exception.
 */
enum class ExceptionType(
    val embedDecorator: EmbedBuilder.(ServerException) -> Unit
) {

    /**
     * An internal exception the server was able to recover from
     */
    INTERNAL({
        appendLocationInfo(this, it.rootCause.stackTrace[0])
    }),

    /**
     * An exception thrown during the execution of a command. This includes tab completion exceptions.
     */
    COMMAND({ serverException ->
        serverException as ServerCommandException // Smart cast to the command exception

        // Find the stack trace element whose class name matches the command name
        val element = serverException.stackTrace.firstOrNull { it.className == serverException.command.javaClass.name }
            ?: serverException.rootCause.stackTrace[0]

        appendLocationInfo(this, element)

        with (serverException) {
            field("Command", true) { command.name }
            field("Sender", true) { commandSender.name }
            field("Arguments", false) { arguments.joinToString(" ") }
        }
    }),

    /**
     * An exception thrown by a plugin's event listener.
     */
    EVENT_LISTENER({
        it as ServerEventException // Smart cast to event exception

        // Find the stack trace element whose class name matches the listener class.
        val element = it.stackTrace.firstOrNull { e -> e.className == it.listener.javaClass.name }
            ?: it.rootCause.stackTrace[0]

        appendLocationInfo(this, element)

        with (it) {
            field("Event", true) { event.eventName }
            field("Async", true) { event.isAsynchronous.toString() }
            field("Listener", true) { listener.javaClass.name }
        }
    }),

    /**
     * An exception thrown by an active BukkitTask scheduled by a plugin.
     */
    SCHEDULER({
        it as ServerSchedulerException // Smart cast to scheduler exception

        val pluginPackage = it.task.owner.javaClass.packageName

        // Find the stack trace element whose package matches the plugin that owns the task
        val element = it.stackTrace.firstOrNull { e -> e.className.startsWith(pluginPackage) }
            ?: it.rootCause.stackTrace[0]

        appendLocationInfo(this, element)
        field("Plugin", true) { it.task.owner.name }
        field("Async", true) { ((!it.task.isSync).toString()) }
        field("BukkitTask ID", true) { it.task.taskId.toString() }
    }),

    /**
     * An exception thrown when a plugin attempts to enable or disable.
     */
    ENABLE_DISABLE({
        appendLocationInfo(this, it.rootCause.stackTrace[0])
    }),

    /**
     * An exception thrown by an incoming plugin message.
     */
    PLUGIN_MESSAGE({
        it as ServerPluginMessageException
        appendLocationInfo(this, it.rootCause.stackTrace[0])
        field("Channel", true) { it.channel }
        field("Player", true) { it.player.name }

        if (it.data.size < 8_000) {
            field("Data", false) { it.data.toString() }
        }
    }),

    /**
     * An exception thrown from an unknown context
     */
    UNKNOWN({
        appendLocationInfo(this, it.rootCause.stackTrace[0])
    });
}

/**
 * Append information regarding the source of an exception to an embed builder.
 */
private fun appendLocationInfo(builder: EmbedBuilder, element: StackTraceElement) = with (builder) {
    field("File/Class", true) { element.fileName ?: element.className }
    field("Method", true) { element.methodName }
    field("Line Number", true) { element.lineNumber.toString() }
}

/**
 * Get the correlated ExceptionType from a ServerException instance
 */
fun ServerException.toExceptionType() = when (this) {
    is ServerInternalException -> ExceptionType.INTERNAL
    is ServerCommandException -> ExceptionType.COMMAND
    is ServerEventException -> ExceptionType.EVENT_LISTENER
    is ServerSchedulerException -> ExceptionType.SCHEDULER
    is ServerPluginEnableDisableException -> ExceptionType.ENABLE_DISABLE
    is ServerPluginMessageException -> ExceptionType.PLUGIN_MESSAGE
    else -> ExceptionType.UNKNOWN
}