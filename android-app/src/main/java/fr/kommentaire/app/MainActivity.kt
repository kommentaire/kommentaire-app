package fr.kommentaire.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.*
import androidx.lifecycle.lifecycleScope
import androidx.ui.core.*
import androidx.ui.foundation.*
import androidx.ui.graphics.Color
import androidx.ui.graphics.SolidColor
import androidx.ui.graphics.imageFromResource
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.layout.*
import androidx.ui.layout.ColumnScope.weight
import androidx.ui.layout.RowScope.gravity
import androidx.ui.layout.RowScope.weight
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.Send
import androidx.ui.material.icons.filled.ThumbDown
import androidx.ui.material.icons.filled.ThumbUp
import androidx.ui.material.icons.outlined.ThumbDown
import androidx.ui.material.icons.outlined.ThumbUp
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import fr.kommentaire.lib.KomRepository
import fr.kommentaire.lib.KomUser
import fr.kommentaire.lib.fragment.QuestionFragment
import fr.kommentaire.lib.type.UpvoteType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

val KomRepositoryAmbient = staticAmbientOf<KomRepository>()

sealed class Screen {
    class Loading(val pseudo: String) : Screen()
    class Login(val message: String?) : Screen()
    class List(val user: KomUser) : Screen()
}

@Composable
fun App() {
    val context = ContextAmbient.current

    Providers(KomRepositoryAmbient.provides(KomRepository())) {
        val repository = KomRepositoryAmbient.current
        MaterialTheme {
            val user = KomRepositoryAmbient.current.getUser()
            val screenState = state {
                if (user != null) {
                    Screen.List(user = user)
                } else {
                    Screen.Login(message = null)
                }
            }

            Scaffold(
                    topAppBar = {
                        TopAppBar(
                                title = {
                                    Text(text = context.getString(R.string.app_name))
                                },
                                actions = {
                                    if (screenState.value is Screen.List) {
                                        Button(
                                                text = {
                                                    Text("LOGOUT")
                                                },
                                                onClick = {
                                                    repository.logout()
                                                    screenState.value = Screen.Login(null)
                                                })
                                    }
                                }
                        )
                    },
                    bodyContent = { modifier ->
                        when (val screen = screenState.value) {
                            is Screen.Loading -> LoadingScreen(pseudo = screen.pseudo) {
                                screenState.value = when (it) {
                                    is KomRepository.CreateUserResult.Success -> Screen.List(it.user)
                                    is KomRepository.CreateUserResult.Failure -> Screen.Login(it.message)
                                }
                            }
                            is Screen.Login -> LoginScreen(modifier, screen.message) {
                                screenState.value = Screen.Loading(it)
                            }
                            is Screen.List -> ListScreen(screen.user)
                        }
                    }
            )
        }
    }
}

@Composable
fun scoped(block: suspend CoroutineScope.() -> Unit) {
    val lifecycleOwner = LifecycleOwnerAmbient.current

    onActive {
        val job = lifecycleOwner.lifecycleScope.launch {
            block()
        }
        onDispose {
            job.cancel()
        }
    }
}

@Composable
fun ListScreen(user: KomUser) {
    Column {
        val questions = state { emptyList<QuestionFragment>() }

        val repository = KomRepositoryAmbient.current
        scoped {
            try {
                repository.getQuestions(user).collect {
                    questions.value = it
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "cannot get list of questions", e)
            }
        }

        VerticalScroller(modifier = Modifier.weight(1f)) {
            Spacer(modifier = Modifier.height(10.dp))
            questions.value.forEach {
                QuestionItem(it, user)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        val backgroundModifier = Modifier.drawBehind {
            drawRect(
                    size = this.size,
                    brush = SolidColor(Color.LightGray)
            )
        }
        val username = state { TextFieldValue() }
        Stack(backgroundModifier + Modifier.padding(10.dp) + Modifier.fillMaxWidth()) {
            Divider(
                    modifier = Modifier.gravity(Alignment.BottomCenter) + Modifier.padding(end = 40.dp),
                    color = Color.Black,
                    thickness = 2.dp
            )
            Row {
                Stack(modifier = Modifier.weight(1f)) {
                    TextField(
                            value = username.value,
                            onValueChange = {
                                username.value = it
                            })
                }
                IconButton(icon = {
                    Icon(
                            asset = Icons.Filled.Send
                    )

                }, onClick = {
                    val content = username.value.text
                    if (!content.isNullOrBlank()) {
                        repository.createQuestion(user, content)
                    }
                    username.value = TextFieldValue()
                })
            }
            if (username.value.text.isNullOrBlank()) {
                Text(
                        text = "Votre question",
                        color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun QuestionItem(questionFragment: QuestionFragment, user: KomUser?) {
    val (upvote, updateUpvote) = stateFor(questionFragment) {
        questionFragment.userVoteType == UpvoteType.UPVOTE
    }
    val (count, updateCount) = stateFor(questionFragment) {
        questionFragment.votes
    }
    val (downvote, updateDownvote) = stateFor(questionFragment) {
        questionFragment.userVoteType == UpvoteType.DOWNVOTE
    }

    val repository = KomRepositoryAmbient.current

    Row {
        Spacer(modifier = Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                    text = questionFragment.user?.pseudo ?: "Anonymous",
                    style = MaterialTheme.typography.subtitle2,
                    color = Color.Gray

            )
            Text(
                    text = questionFragment.content,
                    style = MaterialTheme.typography.body1
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        Row(modifier = Modifier.padding(top = 7.dp)) {
            ThumbButton(
                    checked = downvote,
                    onCheckChange = {
                        updateDownvote(it)
                        updateUpvote(false)
                        if (it) {
                            repository.downvoteQuestion(user!!, questionFragment.id)
                        } else {
                            repository.cancelQuestionVote(user!!, questionFragment.id)
                        }
                        //updateCount(count - it.toInt())
                    },
                    checkedAsset = Icons.Filled.ThumbDown,
                    uncheckedAsset = Icons.Outlined.ThumbDown
            )
            Text(
                    text = count.toString(),
                    modifier = Modifier.gravity(Alignment.CenterVertically)
            )
            ThumbButton(
                    checked = upvote,
                    onCheckChange = {
                        updateDownvote(false)
                        updateUpvote(it)
                        if (it) {
                            repository.upvoteQuestion(user!!, questionFragment.id)
                        } else {
                            repository.cancelQuestionVote(user!!, questionFragment.id)
                        }
                        //updateCount(count + it.toInt())
                    },
                    checkedAsset = Icons.Filled.ThumbUp,
                    uncheckedAsset = Icons.Outlined.ThumbUp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))
    }
}

fun Boolean.toInt() = if (this) 1 else -1

@Composable
fun ThumbButton(checked: Boolean,
                onCheckChange: (Boolean) -> Unit,
                checkedAsset: VectorAsset,
                uncheckedAsset: VectorAsset
) {
    IconToggleButton(
            checked = checked,
            onCheckedChange = onCheckChange,
            modifier = Modifier.gravity(Alignment.CenterVertically)
    ) {
        if (checked) {
            Icon(
                    asset = checkedAsset,
                    tint = Color.Blue
            )
        } else {
            Icon(
                    asset = uncheckedAsset
            )
        }
    }

}

@Preview("Question")
@Composable
fun question() {
    val question = QuestionFragment(
            id = 1,
            user = QuestionFragment.User(
                    pseudo = "testUser"
            ),
            content = "Is this working?",//Is this working?Is this working?Is this working?Is this working?Is this working?Is this working?Is this working?",
            userVoteType = UpvoteType.UPVOTE,
            votes = 13
    )

    QuestionItem(questionFragment = question, user = null)
}


@Composable
fun LoadingScreen(pseudo: String, callback: (KomRepository.CreateUserResult) -> Unit) {
    val repository = KomRepositoryAmbient.current
    scoped {
        val result = repository.createUser(pseudo = pseudo)
        callback(result)
    }
    Stack(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.gravity(Alignment.Center))
    }
}

@Composable
fun LoginScreen(modifier: Modifier, message: String?, onLogin: (String) -> Unit) {

    val (error, updateError) = state {
        message
    }
    scoped {
        delay(5000L)
        updateError(null)
    }
    val backgroundModifier = Modifier.drawBehind {
        drawRect(
                size = this.size,
                brush = SolidColor(Color.LightGray)
        )
    }

    Stack(modifier = modifier.fillMaxSize() + Modifier.padding(20.dp)) {
        Column(modifier = modifier + Modifier.fillMaxSize()) {
            val username = state { TextFieldValue() }
            Stack(modifier = Modifier.fillMaxWidth()) {
                Image(
                        asset = imageFromResource(ContextAmbient.current.resources, R.mipmap.ic_launcher),
                        modifier = Modifier.width(100.dp) + Modifier.height(100.dp) + Modifier.gravity(Alignment.Center),
                        contentScale = ContentScale.FillHeight
                )

            }
            Spacer(modifier = Modifier.height(100.dp))
            Text("username:")
            TextField(
                    value = username.value,
                    modifier = backgroundModifier + Modifier.padding(10.dp) + modifier + Modifier.fillMaxWidth(),
                    onValueChange = {
                        username.value = it
                    })
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                    onClick = {
                        onLogin(username.value.text)
                    },
                    text = {
                        Text("View questions")
                    },
                    modifier = Modifier.fillMaxWidth()
            )
        }

        if (error != null) {
            Snackbar(
                    modifier = Modifier.gravity(Alignment.BottomCenter),
                    text = {
                        Text(error)
                    },
                    action = {
                        TextButton(
                                onClick = {
                                    updateError(null)
                                },
                                text = {
                                    Text("dismiss")
                                })
                    }
            )
        }
    }
}