package fr.kommentaire.lib

import com.apollographql.apollo.network.HttpMethod
import com.apollographql.apollo.network.http.ApolloHttpNetworkTransport
import com.apollographql.apollo.network.ws.ApolloWebSocketFactory
import com.apollographql.apollo.network.ws.ApolloWebSocketNetworkTransport
import platform.Foundation.*
import kotlin.random.Random

actual fun setPreference(key: String, value: String) {
    NSUserDefaults.standardUserDefaults.setValue(value = value, forKey = key)
}

actual fun getPreference(key: String): String? {
    return NSUserDefaults.standardUserDefaults.stringForKey(defaultName = key)
}

actual fun removePreference(key: String) {
    NSUserDefaults.standardUserDefaults.removeObjectForKey(defaultName = key)
}

actual fun randomString(length: Int): String {
    val s = StringBuilder()
    val random = Random(NSDate().timeIntervalSince1970().toLong())
    repeat(length) {
        s.append('a' + random.nextInt(26))
    }
    return s.toString()
}
actual fun defaultDispatcher() = mainDispatcher

actual fun newTransport(accessToken: String?): ApolloHttpNetworkTransport {
    return ApolloHttpNetworkTransport(
            serverUrl = "https://stark-river-82454.herokuapp.com/graphql",
            headers = mutableMapOf(
                    "Accept" to "application/json",
                    "Content-Type" to "application/json"
            ).apply {
                if (accessToken != null) {
                    put("access_token", accessToken)
                }
            },
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
