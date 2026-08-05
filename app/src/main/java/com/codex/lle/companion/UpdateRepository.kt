package com.codex.lle.companion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class UpdateRepository {
    suspend fun fetchLatestVersion(): LleVersion = withContext(Dispatchers.IO) {
        val url = URL(BuildConfig.VERSION_FILE_URL)
        require(url.protocol == "https" && url.host == "raw.githubusercontent.com")

        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            useCaches = false
            setRequestProperty("User-Agent", UpdateConfig.USER_AGENT)
            setRequestProperty("Accept", "text/plain")
            setRequestProperty("Cache-Control", "no-cache")
        }

        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("GitHub returned HTTP ${connection.responseCode}")
            }
            val text = connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                val content = reader.readText()
                if (content.length > 64) throw IOException("Version file is too large")
                content.trim()
            }
            LleVersion.parse(text) ?: throw IOException("Invalid version file")
        } finally {
            connection.disconnect()
        }
    }
}
