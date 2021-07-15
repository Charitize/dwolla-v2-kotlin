package com.dwolla.util

import com.dwolla.Environment

internal class UrlBuilder(private val environment: Environment) {
    private val apiDomain = "^(http://|https://)(.+)(/.*)?\$".toRegex()
            .find(environment.apiBaseUrl())!!
            .destructured.component2()
    private val isUseSsl = environment.apiBaseUrl().startsWith("https://")

    fun buildUrl(vararg parts: String): String =
        parts.fold(environment.apiBaseUrl()) { url, p ->
            when {
                hasApiUrlIgnoringProtocol(p) -> convertUrl(p, isUseSsl)
                p.contains(":") -> throw IllegalArgumentException("Invalid host ($p). Must start with ${environment.apiBaseUrl()}")
                else -> "${convertUrl(url, isUseSsl)}/${trimSlashes(p)}"
            }
        }

    private fun hasApiUrlIgnoringProtocol(p: String) =
            p == environment.apiBaseUrl() || p.matches("^(http://|https://)(${this.apiDomain})(/.*)?\$".toRegex())

    private fun convertUrl(p: String, shouldUseSsl: Boolean): String {
        val trimmed = trimSlashes(p)
        return if (shouldUseSsl) {
            trimmed.replace("http://", "https://")
        } else {
            trimmed.replace("https://", "http://")
        }
    }

    private fun trimSlashes(s: String): String =
        s.removePrefix("/").removeSuffix("/")
}
