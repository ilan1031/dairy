package com.example.data.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar(context: Context) : CookieJar {
    private val sharedPrefs = context.getSharedPreferences("dairy_cookies_prefs", Context.MODE_PRIVATE)

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val editor = sharedPrefs.edit()
        cookies.forEach { cookie ->
            if (cookie.value.isEmpty() || cookie.expiresAt <= System.currentTimeMillis()) {
                editor.remove(cookie.name)
            } else {
                editor.putString(cookie.name, cookie.toString())
            }
        }
        editor.apply()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = mutableListOf<Cookie>()
        val domain = url.host
        sharedPrefs.all.forEach { (key, value) ->
            if (value is String) {
                var cookie = Cookie.parse(url, value)
                if (cookie == null) {
                    val rawValue = if (value.contains(";")) {
                        val firstPart = value.substringBefore(";")
                        if (firstPart.contains("=")) {
                            firstPart.substringAfter("=")
                        } else {
                            value
                        }
                    } else {
                        value
                    }
                    try {
                        cookie = Cookie.Builder()
                            .name(key)
                            .value(rawValue)
                            .domain(domain)
                            .path("/")
                            .build()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (cookie != null) {
                    cookies.add(cookie)
                }
            }
        }
        return cookies
    }

    @Synchronized
    fun clear() {
        sharedPrefs.edit().clear().apply()
    }
}
