package fr.kommentaire.lib

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.http.ApolloHttpNetworkTransport
import com.apollographql.apollo.network.ws.ApolloWebSocketNetworkTransport
import fr.kommentaire.lib.fragment.QuestionFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

expect fun setPreference(key: String, value: String)
expect fun removePreference(key: String)
expect fun getPreference(key: String): String?
expect fun randomString(length: Int): String
expect fun newTransport(accessToken: String?): ApolloHttpNetworkTransport
expect fun newWebSocketTransport(accessToken: String?): ApolloWebSocketNetworkTransport

class KomRepository {
    fun getClient(accessToken: String? = null): ApolloClient {
        return ApolloClient(
                networkTransport = newTransport(accessToken),
                subscriptionNetworkTransport = newWebSocketTransport(accessToken)
        )
    }

    companion object {
        val KEY_PSEUDO = "pseudo"
        val KEY_PASSWORD = "password"
        val KEY_TOKEN = "token"
    }

    sealed class CreateUserResult {
        class Success(val user: KomUser) : CreateUserResult()
        class Failure(val message: String) : CreateUserResult()
    }

    suspend fun createUser(pseudo: String): CreateUserResult {
        val password = randomString(12)
        try {
            val response = getClient().mutate(CreateUserMutation(pseudo = pseudo, pwd = password))
                    .execute()
                    .first()

            val user = response
                    .data
                    ?.createUser
                    ?.fragments
                    ?.userFragment

            if (user == null) {
                val message = response.errors?.firstOrNull()?.message ?: "unknown error"
                return CreateUserResult.Failure(message)
            }
            setPreference(KEY_PASSWORD, password)
            setPreference(KEY_PSEUDO, pseudo)
            setPreference(KEY_TOKEN, user.token)
            return CreateUserResult.Success(KomUser(pseudo, user.token))
        } catch (e: Exception) {
            return CreateUserResult.Failure(e.message ?: "unknown exception")
        }
    }

    fun getUser(): KomUser? {
        val password = getPreference(KEY_PASSWORD)
        val pseudo = getPreference(KEY_PSEUDO)
        val token = getPreference(KEY_TOKEN)

        if (password != null && pseudo != null && token != null) {
            return KomUser(pseudo, token)
        } else {
            return null
        }
    }

    fun logout() {
        removePreference(KEY_PASSWORD)
        removePreference(KEY_PSEUDO)
        removePreference(KEY_TOKEN)
    }

    fun getQuestions(user: KomUser): Flow<List<QuestionFragment>> {
        return flow {
            val initialList = getClient(user.token).query(GetQuestionsQuery()).execute()
                    .mapNotNull {
                        it.data?.questions?.map {
                            it.fragments.questionFragment
                        }
                    }
                    .first()

            var list = mergeLists(initialList, emptyList())

            emit(list)

            getClient(user.token).subscribe(QuestionChangeSubscription())
                    .execute()
                    .retry {
                        delay(1000)
                        true
                    }
                    .collect {
                        val changes = it.data?.questionChange?.mapNotNull { it.fragments.questionFragment }
                        if (changes != null) {
                            list = mergeLists(list, changes)
                            emit(list)
                        }
                    }
        }
    }

    private fun mergeLists(initialList: List<QuestionFragment>, changes: List<QuestionFragment>): List<QuestionFragment> {
        val map = initialList.groupBy { it.id }.mapValues { it.value.first() }.toMutableMap()

        map.putAll(changes.map { it.id to it })
        val result = map.values.sortedByDescending {
            it.votes
        }
        return result
    }

    fun upvoteQuestion(user: KomUser, questionId: Int) = GlobalScope.launch {
        getClient(user.token).mutate(UpvoteQuestionMutation(questionId)).execute().first()
    }

    fun downvoteQuestion(user: KomUser, questionId: Int) = GlobalScope.launch {
        getClient(user.token).mutate(DownvoteQuestionMutation(questionId)).execute().first()
    }

    fun cancelQuestionVote(user: KomUser, questionId: Int) = GlobalScope.launch {
        getClient(user.token).mutate(CancelQuestionVoteMutation(questionId)).execute().first()
    }

    fun createQuestion(user: KomUser, content: String) = GlobalScope.launch {
        getClient(user.token).mutate(CreateQuestionMutation(content = content)).execute().first()
    }
}

class KomUser(val pseudo: String, val token: String) {

}