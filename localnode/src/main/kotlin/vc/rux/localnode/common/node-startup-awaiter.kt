package vc.rux.localnode.common

import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URL
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val log = LoggerFactory.getLogger("node-startup-awaiter")

// A very simple RPC client without extra dependencies
internal fun waitForRpcToBoot(
    localRpcNodeUrl: String,
    nodeName: String,
    maxWaitBootTime: Duration,
    timeBetweenChecks: Duration = 300.milliseconds
) {
    val startedAt = System.currentTimeMillis()
    val requestBody = """{"jsonrpc":"2.0","method":"net_version","params":[],"id":67}"""
    while(System.currentTimeMillis() - startedAt < maxWaitBootTime.inWholeMilliseconds) {
        try {
            val conn = URL(localRpcNodeUrl).openConnection() as HttpURLConnection
            conn.doOutput = true
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.outputStream.use { it.write(requestBody.toByteArray()) }

            val code = conn.responseCode
            if (code == 200) {
                log.debug("The local RPC node became available after {}ms", System.currentTimeMillis() - startedAt)
                return
            }
            throw IllegalStateException("The local RPC node isn't available yet, got $code response")
        } catch (e: Exception) {
            log.debug("Failed to communicate with the local RPC: $e")
        }

        Thread.sleep(timeBetweenChecks.inWholeMilliseconds)
    }

    throw IllegalStateException("The container $nodeName started but the RPC is not available after $maxWaitBootTime")
}
