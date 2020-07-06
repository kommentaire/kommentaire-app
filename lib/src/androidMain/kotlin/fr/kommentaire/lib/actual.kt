package fr.kommentaire.lib

import android.content.Context
import android.content.SharedPreferences
import androidx.startup.Initializer
import com.apollographql.apollo.network.HttpMethod
import com.apollographql.apollo.network.http.ApolloHttpNetworkTransport
import com.apollographql.apollo.network.ws.ApolloWebSocketFactory
import com.apollographql.apollo.network.ws.ApolloWebSocketNetworkTransport
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.lang.StringBuilder
import java.util.*
import kotlin.random.Random

actual fun setPreference(key: String, value: String) {
    KommentaireInitializer.preferences.edit().putString(key, value).apply()
}

actual fun getPreference(key: String): String? {
    return KommentaireInitializer.preferences.getString(key, null)
}

actual fun removePreference(key: String) {
    KommentaireInitializer.preferences.edit().remove(key).apply()
}

actual fun randomString(length: Int): String {
    val s = StringBuilder()
    val random = Random(Date().time)
    repeat(length) {
        s.append('a' + random.nextInt(26))
    }
    return s.toString()
}

actual fun newTransport(accessToken: String?): ApolloHttpNetworkTransport {
    val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    return ApolloHttpNetworkTransport(
            serverUrl = "https://stark-river-82454.herokuapp.com/graphql".toHttpUrl(),
            httpCallFactory = okHttpClient,
            headers = mutableMapOf(
                    "Accept" to "application/json",
                    "Content-Type" to "application/json"
            ).apply {
                if (accessToken != null) {
                    put("access_token", accessToken)
                }
            }.toHeaders(),
            httpMethod = HttpMethod.Post
    )
}

actual fun newWebSocketTransport(accessToken: String?): ApolloWebSocketNetworkTransport {
    return ApolloWebSocketNetworkTransport(
            webSocketFactory = ApolloWebSocketFactory(
                    serverUrl = "https://stark-river-82454.herokuapp.com/subscriptions",
                    headers = mutableMapOf<String, String>(
                    ).apply {
                        if (accessToken != null) {
                            put("Cookie", "")
                            put("access_token", accessToken)
                        }
                    }
            )
    )
}

class KommentaireInitializer : Initializer<Context> {
    companion object {
        lateinit var preferences: SharedPreferences
        lateinit var networkFlipperPlugin: NetworkFlipperPlugin
    }

    override fun create(context: Context): Context {
        SoLoader.init(context, false)
        networkFlipperPlugin = NetworkFlipperPlugin()
        AndroidFlipperClient.getInstance(context).apply {
            addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
            addPlugin(networkFlipperPlugin)
            start()
        }

        preferences = context.getSharedPreferences("Kommentaire", Context.MODE_PRIVATE)
        return context
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}

actual fun defaultDispatcher() = Dispatchers.Main as CoroutineDispatcher