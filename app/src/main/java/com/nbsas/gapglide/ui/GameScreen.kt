package com.nbsas.gapglide.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nbsas.gapglide.Difficulty
import com.nbsas.gapglide.GameEngine
import com.nbsas.gapglide.GameState
import com.nbsas.gapglide.GameStatus
import com.nbsas.gapglide.ui.theme.GapGlideTheme

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    highScore: Int = 0,
    onNewHighScore: (Int) -> Unit = {}
) {
    var gameState by remember { mutableStateOf(GameState(highScore = highScore)) }
    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }

    // Sync highScore from external state
    LaunchedEffect(highScore) {
        gameState = gameState.copy(highScore = highScore)
    }

    // Game Loop
    LaunchedEffect(gameState.status) {
        if (gameState.status == GameStatus.PLAYING) {
            var lastTime = withFrameNanos { it }
            while (gameState.status == GameStatus.PLAYING) {
                withFrameNanos { time ->
                    val dt = (time - lastTime) / 1_000_000_000f
                    lastTime = time
                    
                    var nextState = GameEngine.updatePlayer(gameState, dt, screenHeight)
                    nextState = GameEngine.updateObstacles(nextState, dt, screenWidth, screenHeight)
                    
                    if (GameEngine.checkCollisions(nextState, screenWidth, screenHeight)) {
                        nextState = nextState.copy(status = GameStatus.GAME_OVER)
                        if (nextState.currentScore > nextState.highScore) {
                            onNewHighScore(nextState.currentScore)
                        }
                    }
                    
                    gameState = nextState
                }
            }
        }
    }

    val obstacleColor = MaterialTheme.colorScheme.primary
    val playerColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onGloballyPositioned {
                screenWidth = it.size.width.toFloat()
                screenHeight = it.size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    if (gameState.status == GameStatus.PLAYING) {
                        gameState = gameState.copy(playerVelocity = GameEngine.JUMP_IMPULSE)
                    }
                }
            }
    ) {
        // Game Canvas (Base layer)
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (gameState.status != GameStatus.START) {
                val gapHeight = gameState.difficulty.gapHeight
                // Draw Obstacles
                gameState.obstacles.forEach { obstacle ->
                    // Top pipe
                    drawRect(
                        color = obstacleColor,
                        topLeft = Offset(obstacle.x, 0f),
                        size = Size(GameState.OBSTACLE_WIDTH, obstacle.gapY - gapHeight / 2)
                    )
                    // Bottom pipe
                    drawRect(
                        color = obstacleColor,
                        topLeft = Offset(obstacle.x, obstacle.gapY + gapHeight / 2),
                        size = Size(GameState.OBSTACLE_WIDTH, size.height - (obstacle.gapY + gapHeight / 2))
                    )
                }

                // Draw Player (Nova - Winged Orb)
                val cx = size.width / 2
                val cy = gameState.playerY
                val radius = GameState.PLAYER_RADIUS

                // Core Body
                drawCircle(
                    color = playerColor,
                    radius = radius,
                    center = Offset(cx, cy)
                )

                // Wing Details (Symmetric, swept-back, inside radius)
                val wingColor = Color.White.copy(alpha = 0.6f)
                val wingPath = Path().apply {
                    // Top Wing
                    moveTo(cx + radius * 0.3f, cy - radius * 0.2f)
                    lineTo(cx - radius * 0.7f, cy - radius * 0.5f)
                    lineTo(cx - radius * 0.4f, cy - radius * 0.1f)
                    close()
                    // Bottom Wing
                    moveTo(cx + radius * 0.3f, cy + radius * 0.2f)
                    lineTo(cx - radius * 0.7f, cy + radius * 0.5f)
                    lineTo(cx - radius * 0.4f, cy + radius * 0.1f)
                    close()
                }
                drawPath(path = wingPath, color = wingColor)
            }
        }

        // Playing UI (Score overlay)
        if (gameState.status == GameStatus.PLAYING) {
            Text(
                text = "${gameState.currentScore}",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .safeDrawingPadding()
                    .padding(top = 16.dp),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 120.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
        }

        // Start Overlay
        AnimatedVisibility(
            visible = gameState.status == GameStatus.START,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .safeDrawingPadding(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Gap Glide",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Best: ${gameState.highScore}",
                        fontSize = 20.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Difficulty Selection
                    Text(
                        text = "Select Difficulty",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Difficulty.entries.forEach { difficulty ->
                            val isSelected = gameState.difficulty == difficulty
                            Button(
                                onClick = { gameState = gameState.copy(difficulty = difficulty) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = difficulty.name,
                                    fontSize = 14.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = {
                        gameState = gameState.copy(
                            status = GameStatus.PLAYING,
                            currentScore = 0,
                            playerY = screenHeight / 2,
                            playerVelocity = 0f,
                            obstacles = emptyList()
                        )
                    }) {
                        Text(text = "Play", fontSize = 24.sp)
                    }
                }
            }
        }

        // Game Over Overlay
        AnimatedVisibility(
            visible = gameState.status == GameStatus.GAME_OVER,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .safeDrawingPadding(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Game Over",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Score: ${gameState.currentScore}",
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Best: ${gameState.highScore}",
                        fontSize = 20.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = {
                        gameState = gameState.copy(
                            status = GameStatus.PLAYING,
                            currentScore = 0,
                            playerY = screenHeight / 2,
                            playerVelocity = 0f,
                            obstacles = emptyList()
                        )
                    }) {
                        Text(text = "Restart", fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,navigation=buttons")
@Composable
fun GameScreenPreview() {
    GapGlideTheme {
        GameScreen(highScore = 42)
    }
}
