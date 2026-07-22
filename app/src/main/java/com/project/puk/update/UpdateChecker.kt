package com.project.puk.update

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateResult(
    val hasUpdate: Boolean,
    val latestVersion: String? = null,
    val htmlUrl: String? = null,
    val error: String? = null
)

class UpdateChecker(private val context: Context) {

    companion object {
        private const val API_URL = "https://api.github.com/repos/Mirox921/SpotiPuk/releases/latest"
        private const val PREFS_NAME = "spotipuk_prefs"
        private const val KEY_HAS_UPDATE = "hasUpdateAvailable"
        private const val KEY_LAST_CHECK = "LastUpdateCheck"
        private const val CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L
    }

    fun hasUpdateAvailable(): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HAS_UPDATE, false)
    }

    fun clearUpdateAvailable() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_HAS_UPDATE, false).apply()
    }

    fun autoCheck() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0)
        if (System.currentTimeMillis() - lastCheck < CHECK_INTERVAL_MS) return

        prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()
        fetchUpdateAvailable()
    }

    fun checkForUpdate(onResult: (UpdateResult) -> Unit) {
        val mainHandler = Handler(Looper.getMainLooper())
        Thread {
            try {
                val url = URL(API_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Accept", "application/vnd.github+json")
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(body)
                    val tagName = json.optString("tag_name", "").removePrefix("v")
                    val htmlUrl = json.optString("html_url", "https://github.com/Mirox921/SpotiPuk/releases/latest")
                    val currentVersion = context.packageManager
                        .getPackageInfo(context.packageName, 0).versionName ?: ""

                    val hasUpdate = isNewer(tagName, currentVersion)
                    if (hasUpdate) {
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit().putBoolean(KEY_HAS_UPDATE, true).apply()
                    } else {
                        clearUpdateAvailable()
                    }

                    val res = UpdateResult(
                        hasUpdate = hasUpdate,
                        latestVersion = tagName,
                        htmlUrl = htmlUrl
                    )
                    mainHandler.post { onResult(res) }
                } else {
                    val res = UpdateResult(
                        hasUpdate = false,
                        error = "Server response: ${conn.responseCode}"
                    )
                    mainHandler.post { onResult(res) }
                }
            } catch (e: Exception) {
                val res = UpdateResult(
                    hasUpdate = false,
                    error = e.localizedMessage ?: "Network error"
                )
                mainHandler.post { onResult(res) }
            }
        }.start()
    }

    private fun fetchUpdateAvailable() {
        checkForUpdate {}
    }

    private fun isNewer(latest: String, current: String): Boolean {
        if (latest.isEmpty()) return false
        val latestParts = latest.split(".")
        val currentParts = current.split(".")
        val size = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until size) {
            val l = latestParts.getOrNull(i)?.toIntOrNull() ?: 0
            val c = currentParts.getOrNull(i)?.toIntOrNull() ?: 0
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}

