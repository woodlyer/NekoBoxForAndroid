package io.nekohasekai.sagernet.fmt.gost

import io.nekohasekai.sagernet.fmt.LOCALHOST
import io.nekohasekai.sagernet.ktx.linkBuilder
import io.nekohasekai.sagernet.ktx.toLink
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import java.net.InetAddress
import java.net.URI
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

fun parseGost(link: String): GostBean {
    return GostBean().apply {
        initializeDefaultValues()
    }
}

fun GostBean.toUri(): String {
    val builder = linkBuilder().host("127.0.0.1").port(1080)
    if (name.isNotBlank()) builder.encodedFragment(name)
    return builder.toLink("gost", false)
}

fun GostBean.buildGostArgs(port: Int): String {
    val args = JSONArray()
    args.put("-L")
    args.put("$LOCALHOST:$port")

    if (customArgs.isNotBlank()) {
        var resolvedArgs = customArgs
        try {
            val tokens = customArgs.split("\\s+".toRegex())
            val fIndex = tokens.indexOf("-F")
            if (fIndex != -1 && fIndex + 1 < tokens.size) {
                val targetUrl = tokens[fIndex + 1].trim('"', '\'')
                
                // standard http replacement for URL parsing
                val standardUrl = if (targetUrl.contains("://")) {
                    targetUrl.replaceFirst("^[a-zA-Z0-9+.-]+://".toRegex(), "http://")
                } else {
                    "http://$targetUrl"
                }
                
                val uri = URI(standardUrl)
                val originalHost = uri.host
                if (!originalHost.isNullOrBlank()) {
                    // Try to resolve the hostname to IP using system resolver with a strict timeout
                    val resolveTask = FutureTask(Callable {
                        try {
                            InetAddress.getByName(originalHost).hostAddress
                        } catch (e: Exception) {
                            null
                        }
                    })
                    Thread(resolveTask).start()
                    val resolvedIp = try {
                        resolveTask.get(2, TimeUnit.SECONDS)
                    } catch (e: Exception) {
                        null
                    }

                    if (!resolvedIp.isNullOrBlank()) {
                        // Keep serverAddress as the original domain (host) so ConfigBuilder automatically adds it to direct/bypass list
                        serverAddress = originalHost
                        
                        // Replace the host in command args to prevent Gost from doing DNS resolution
                        resolvedArgs = resolvedArgs.replace(originalHost, resolvedIp)
                    } else {
                        // Fallback to original host
                        serverAddress = originalHost
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val fileName = if (customConfigFileName.isNotBlank()) customConfigFileName else "kcp.json"
        if (customConfigFileContent.isNotBlank()) {
            try {
                val file = java.io.File(io.nekohasekai.sagernet.SagerNet.application.filesDir, fileName)
                file.writeText(customConfigFileContent)
                resolvedArgs = resolvedArgs.replace(fileName, file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        resolvedArgs.split("\\s+".toRegex()).forEach { if (it.isNotBlank()) args.put(it) }
    }

    return args.toString()
}
