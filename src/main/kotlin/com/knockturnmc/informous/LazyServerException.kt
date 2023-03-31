package com.knockturnmc.informous

import com.destroystokyo.paper.exception.ServerException
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

private const val PASTE_POST_LINK = "https://api.pastes.dev/post"

class LazyServerException(
    private val informous: Informous,
    private val exception: ServerException
) {

    private val httpClient: HttpClient
        get() = informous.discordBot.kordRef.resources.httpClient

    /**
     * Lazily generates the link to this exception's stacktrace via Pastes.
     * Delegates cannot be suspendable. This WILL block the thread it is called from!
     */
    val stackTraceLink: String by lazy {
        runBlocking(informous.pluginScope.coroutineContext) {
            val response = httpClient.put {
                url(PASTE_POST_LINK)
                userAgent("jinglezs/informous")
                contentType(ContentType.Text.Plain)
                setBody(stackTraceString)
            }

            // 200 values indicate successful POST
            if (response.status.value in 200..299 && response.headers.contains("location")) {
                response.headers["location"]!!
            } else ""
        }
    }

    private val nestedException: Throwable
        get() = exception.cause ?: exception

    val stackTraceString: String
        get() = nestedException.stackTraceToString()

    val exceedsCharacterLimit: Boolean
        get() = stackTraceString.length > 2000

}