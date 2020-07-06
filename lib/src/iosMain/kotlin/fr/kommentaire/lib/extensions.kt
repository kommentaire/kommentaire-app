package fr.kommentaire.lib

import fr.kommentaire.lib.fragment.QuestionFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.CoroutineContext
import kotlin.native.concurrent.freeze
import platform.darwin.*



fun KomRepository.getQuestions(user: KomUser, callback: (List<QuestionFragment>) -> Unit) {
    GlobalScope.launch(mainDispatcher) {
        getQuestions(user).collect {
            callback(it)
        }
    }
}

