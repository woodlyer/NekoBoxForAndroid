package io.nekohasekai.sagernet.fmt.gost

import io.nekohasekai.sagernet.fmt.LOCALHOST
import io.nekohasekai.sagernet.ktx.linkBuilder
import io.nekohasekai.sagernet.ktx.toLink
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import org.json.JSONObject

fun parseGost(link: String): GostBean {
    val url = link.replaceFirst("gost://", "https://").toHttpUrlOrNull()
        ?: error("Invalid gost link: $link")
    return GostBean().apply {
        serverAddress = url.host
        serverPort = url.port
        username = url.username
        password = url.password
        protocol = url.queryParameter("protocol") ?: "socks5"
        name = url.fragment ?: ""
        initializeDefaultValues()
    }
}

fun GostBean.toUri(): String {
    val builder = linkBuilder().host(finalAddress).port(finalPort)
    if (username.isNotBlank()) builder.username(username)
    if (password.isNotBlank()) builder.password(password)
    builder.addQueryParameter("protocol", protocol)
    if (name.isNotBlank()) builder.encodedFragment(name)
    return builder.toLink("gost", false)
}

fun GostBean.buildGostArgs(port: Int): String {
    val args = JSONArray()
    // NekoBox expects a SOCKS5 inbound, and gost's default listener (without scheme) 
    // is an auto-negotiation HTTP/SOCKS5 proxy, which perfectly satisfies it.
    args.put("-L")
    args.put("$LOCALHOST:$port")

    if (customArgs.isNotBlank()) {
        var resolvedArgs = customArgs
        if (customConfigFileContent.isNotBlank()) {
            try {
                val file = java.io.File(io.nekohasekai.sagernet.SagerNet.application.filesDir, "kcp.json")
                file.writeText(customConfigFileContent)
                resolvedArgs = resolvedArgs.replace("kcp.json", file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Simple space split for custom args, you might want a proper shell parser later
        resolvedArgs.split("\\s+".toRegex()).forEach { if (it.isNotBlank()) args.put(it) }
    } else {
        // Fallback to simple -F mode based on basic fields
        var auth = ""
        if (username.isNotBlank() && password.isNotBlank()) {
            auth = "$username:$password@"
        }
        val target = "$protocol://$auth$serverAddress:$serverPort"
        args.put("-F")
        args.put(target)
    }

    return args.toString()
}
