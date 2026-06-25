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
            if (cookie.name == "dairy_session" || cookie.name == "dairy_login" || cookie.name == "dairy_subscription") {
                editor.putString(cookie.name, cookie.value)
            }
        }
        editor.apply()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = mutableListOf<Cookie>()
        val session = sharedPrefs.getString("dairy_session", null)
        val login = sharedPrefs.getString("dairy_login", null)
        val subscription = sharedPrefs.getString("dairy_subscription", null)

        val domain = url.host
        if (session != null) {
            cookies.add(Cookie.Builder().name("dairy_session").value(session).domain(domain).build())
        }
        if (login != null) {
            cookies.add(Cookie.Builder().name("dairy_login").value(login).domain(domain).build())
        }
        if (subscription != null) {
            cookies.add(Cookie.Builder().name("dairy_subscription").value(subscription).domain(domain).build())
        }
        return cookies
    }

    @Synchronized
    fun clear() {
        sharedPrefs.edit().clear().apply()
    }
}
