//package com.example.wikipedia_app.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.example.wikipedia_app.data.GameException
//import com.example.wikipedia_app.data.GameService
//import com.example.wikipedia_app.model.GameState
//import com.example.wikipedia_app.ui.components.WikiArticle
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import java.util.concurrent.TimeUnit
//
//@Composable
//fun GameScreen(
//    gameService: GameService,
//    onPlayAgain: () -> Unit
//) {
//    var gameState by remember { mutableStateOf(GameState()) }
//    var isLoading by remember { mutableStateOf(true) }
//    var error by remember { mutableStateOf<String?>(null) }
//    val scope = rememberCoroutineScope()
//
//    LaunchedEffect(Unit) {
//        scope.launch {
//            try {
//                isLoading = true
//                error = null
//                val startArticle = gameService.getRandomArticle()
//                val targetArticle = gameService.getRandomArticle()
//                gameState = gameState.copy(
//                    startArticle = startArticle,
//                    currentArticle = startArticle,
//                    targetArticleTitle = targetArticle.title
//                )
//            } catch (e: GameException) {
//                error = e.message
//            } finally {
//                isLoading = false
//            }
//        }
//    }
//
//    LaunchedEffect(gameState.startTime) {
//        while (!gameState.isGameWon) {
//            delay(1000)
//            gameState = gameState.copy() // Trigger recomposition
//        }
//    }
//
//    if (isLoading) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            CircularProgressIndicator()
//        }
//    } else if (error != null) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                Text(
//                    text = error!!,
//                    style = MaterialTheme.typography.bodyLarge,
//                    color = MaterialTheme.colorScheme.error
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//                Button(onClick = {
//                    error = null
//                    isLoading = true
//                    scope.launch {
//                        try {
//                            val startArticle = gameService.getRandomArticle()
//                            val targetArticle = gameService.getRandomArticle()
//                            gameState = gameState.copy(
//                                startArticle = startArticle,
//                                currentArticle = startArticle,
//                                targetArticleTitle = targetArticle.title
//                            )
//                        } catch (e: GameException) {
//                            error = e.message
//                        } finally {
//                            isLoading = false
//                        }
//                    }
//                }) {
//                    Text("Retry")
//                }
//            }
//        }
//    } else if (gameState.isGameWon) {
//        GameWonScreen(
//            steps = gameState.steps,
//            timeElapsed = System.currentTimeMillis() - gameState.startTime,
//            onPlayAgain = onPlayAgain
//        )
//    } else {
//        GameInProgressScreen(
//            gameState = gameState,
//            onLinkClick = { link ->
//                scope.launch {
//                    try {
//                        isLoading = true
//                        error = null
//                        val newArticle = gameService.getArticle(link.target)
//                        val isWon = newArticle.title == gameState.targetArticleTitle
//                        gameState = gameState.copy(
//                            currentArticle = newArticle,
//                            navigationPath = gameState.navigationPath + newArticle,
//                            steps = gameState.steps + 1,
//                            isGameWon = isWon
//                        )
//                    } catch (e: GameException) {
//                        error = e.message
//                    } finally {
//                        isLoading = false
//                    }
//                }
//            },
//            onBackClick = {
//                if (gameState.navigationPath.isNotEmpty()) {
//                    val previousArticle = gameState.navigationPath.last()
//                    gameState = gameState.copy(
//                        currentArticle = previousArticle,
//                        navigationPath = gameState.navigationPath.dropLast(1),
//                        steps = gameState.steps - 1
//                    )
//                }
//            }
//        )
//    }
//}
//
//@Composable
//fun GameInProgressScreen(
//    gameState: GameState,
//    onLinkClick: (com.example.wikipedia_app.model.WikiLink) -> Unit,
//    onBackClick: () -> Unit
//) {
//    Column(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        // Game info header
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp)
//            ) {
//                Text(
//                    text = "Target: ${gameState.targetArticleTitle ?: "Loading..."}",
//                    style = MaterialTheme.typography.titleLarge
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "Steps: ${gameState.steps}",
//                    style = MaterialTheme.typography.bodyLarge
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "Time: ${formatTime(System.currentTimeMillis() - gameState.startTime)}",
//                    style = MaterialTheme.typography.bodyLarge
//                )
//            }
//        }
//
//        // Back button
//        if (gameState.navigationPath.isNotEmpty()) {
//            Button(
//                onClick = onBackClick,
//                modifier = Modifier
//                    .padding(16.dp)
//                    .align(Alignment.Start)
//            ) {
//                Text("Back")
//            }
//        }
//
//        // Article content
//        gameState.currentArticle?.let { article ->
//            WikiArticle(
//                article = article,
//                onLinkClick = onLinkClick,
//                modifier = Modifier.weight(1f)
//            )
//        } ?: run {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        }
//    }
//}
//
//@Composable
//fun GameWonScreen(
//    steps: Int,
//    timeElapsed: Long,
//    onPlayAgain: () -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text(
//            text = "Congratulations!",
//            style = MaterialTheme.typography.headlineLarge
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            text = "You reached the target in:",
//            style = MaterialTheme.typography.bodyLarge
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = "$steps steps",
//            style = MaterialTheme.typography.titleMedium
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = formatTime(timeElapsed),
//            style = MaterialTheme.typography.titleMedium
//        )
//        Spacer(modifier = Modifier.height(32.dp))
//        Button(onClick = onPlayAgain) {
//            Text("Play Again")
//        }
//    }
//}
//
//private fun formatTime(millis: Long): String {
//    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
//    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
//    return String.format("%02d:%02d", minutes, seconds)
//}