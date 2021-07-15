package com.dwolla.util

import com.dwolla.Dwolla
import com.dwolla.Environment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UrlBuilderTest {
    private val client = Dwolla("id", "secret")
    private val urlBuilder = client.urlBuilder

    @Test fun `builds url with path`() {
        val p1 = "baz"

        val url = urlBuilder.buildUrl(p1)

        assertEquals("${client.environment.apiBaseUrl()}/$p1", url)
    }

    @Test fun `builds url with slashed path`() {
        val p1 = "/baz"

        val url = urlBuilder.buildUrl(p1)

        assertEquals("${client.environment.apiBaseUrl()}$p1", url)
    }

    @Test fun `ignores anything before last absolute uri`() {
        val p1 = client.environment.apiBaseUrl()
        val p2 = "abc"
        val p3 = client.environment.apiBaseUrl()
        val p4 = "123"
        val p5 = "/456"
        val p6 = "789"

        val url = urlBuilder.buildUrl(p1, p2, p3, p4, p5, p6)

        assertEquals("$p3/$p4$p5/$p6", url)
    }

    @Test fun `throws exception if ILLEGAL url`() {
        val p1 = "${client.environment.apiBaseUrl()}.foo.bar"

        assertFailsWith(IllegalArgumentException::class, "should not build url not starting with apiUrl") {
            urlBuilder.buildUrl(p1)
        }
    }

    private val testHttpUrlsEnv = object : Environment {
        override fun apiBaseUrl() = "http://api-sandbox.dwolla.com"
        override fun authBaseUrl() = "http://accounts-sandbox.dwolla.com/auth"
        override fun tokenUrl() = "http://api-sandbox.dwolla.com/token"
    }
    private val testHttpUrlsClient = Dwolla("id", "secret", testHttpUrlsEnv)

    @Test fun `given the environment configured with http urls replaces https with http for further build attempts`() {
        val httpsUrl = "https://api-sandbox.dwolla.com"
        val url = testHttpUrlsClient.urlBuilder.buildUrl(httpsUrl)

        assertEquals("http://api-sandbox.dwolla.com", url)
    }

    @Test fun `given the environment configured with http urls fails if domains not equal`() {
        val wrongDomainHttpsUrl = "https://something-else.dwolla.com"

        assertFailsWith(IllegalArgumentException::class, "should not build url not starting with apiUrl") {
            testHttpUrlsClient.urlBuilder.buildUrl(wrongDomainHttpsUrl)
        }
    }
}
