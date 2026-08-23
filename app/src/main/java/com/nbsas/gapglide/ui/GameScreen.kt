package com.nbsas.gapglide.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nbsas.gapglide.AvatarType
import com.nbsas.gapglide.Difficulty
import com.nbsas.gapglide.GameEngine
import com.nbsas.gapglide.GameState
import com.nbsas.gapglide.GameStatus
import com.nbsas.gapglide.SceneType
import com.nbsas.gapglide.ui.PixelArtLibrary.drawAvatar
import com.nbsas.gapglide.ui.PixelArtLibrary.drawHeritageBackground
import com.nbsas.gapglide.ui.PixelArtLibrary.drawThemedObstacle
import com.nbsas.gapglide.ui.theme.GapGlideTheme

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    highScore: Int = 0,
    initialScene: SceneType = SceneType.TAJ_MAHAL,
    initialAvatar: AvatarType = AvatarType.NOVA,
    onNewHighScore: (Int) -> Unit = {},
    onSceneChanged: (SceneType) -> Unit = {},
    onAvatarChanged: (AvatarType) -> Unit = {}
) {
    var gameState by remember { 
        mutableStateOf(GameState(
            highScore = highScore,
            selectedScene = initialScene,
            selectedAvatar = initialAvatar
        )) 
    }
    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }

    // Sync highScore from external state
    LaunchedEffect(highScore) {
        gameState = gameState.copy(highScore = highScore)
    }

    // Handle Back button on Game Over
    BackHandler(enabled = gameState.status == GameStatus.GAME_OVER) {
        gameState = gameState.copy(
            status = GameStatus.START,
            currentScore = 0,
            playerY = screenHeight / 2,
            playerVelocity = 0f,
            obstacles = emptyList()
        )
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

    val context = LocalContext.current

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
                // Background Layer
                drawHeritageBackground(gameState.selectedScene)

                val gapHeight = gameState.difficulty.gapHeight
                // Draw Obstacles
                gameState.obstacles.forEach { obstacle ->
                    // Top pipe
                    drawThemedObstacle(
                        scene = gameState.selectedScene,
                        x = obstacle.x,
                        y = 0f,
                        width = GameState.OBSTACLE_WIDTH,
                        height = obstacle.gapY - gapHeight / 2,
                        isTop = true
                    )
                    // Bottom pipe
                    drawThemedObstacle(
                        scene = gameState.selectedScene,
                        x = obstacle.x,
                        y = obstacle.gapY + gapHeight / 2,
                        width = GameState.OBSTACLE_WIDTH,
                        height = size.height - (obstacle.gapY + gapHeight / 2),
                        isTop = false
                    )
                }

                // Draw Player
                drawAvatar(
                    avatar = gameState.selectedAvatar,
                    x = size.width / 2,
                    y = gameState.playerY,
                    radius = GameState.PLAYER_RADIUS
                )
            }
        }

        // Playing UI (Score overlay)
        if (gameState.status == GameStatus.PLAYING) {
            Text(
                text = "${gameState.currentScore}",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .safeDrawingPadding()
                    .padding(top = 32.dp),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 120.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
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
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Difficulty Selection
                    Text(
                        text = "Difficulty",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Difficulty.entries.forEach { difficulty ->
                            val isSelected = gameState.difficulty == difficulty
                            Button(
                                onClick = { gameState = gameState.copy(difficulty = difficulty) },
                                modifier = Modifier.heightIn(min = 40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = difficulty.name,
                                    fontSize = 12.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Scene Selection
                    Text(
                        text = "Choose Scene",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SceneType.entries.forEach { scene ->
                            val isSelected = gameState.selectedScene == scene
                            Button(
                                onClick = { 
                                    gameState = gameState.copy(selectedScene = scene)
                                    onSceneChanged(scene)
                                },
                                modifier = Modifier.heightIn(min = 40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else Color.DarkGray
                                )
                            ) {
                                Text(text = scene.displayName, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Avatar Selection
                    Text(
                        text = "Choose Character",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AvatarType.entries.forEach { avatar ->
                            val isSelected = gameState.selectedAvatar == avatar
                            Button(
                                onClick = { 
                                    gameState = gameState.copy(selectedAvatar = avatar)
                                    onAvatarChanged(avatar)
                                },
                                modifier = Modifier.heightIn(min = 40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.DarkGray
                                )
                            ) {
                                Text(text = avatar.displayName, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            gameState = gameState.copy(
                                status = GameStatus.PLAYING,
                                currentScore = 0,
                                playerY = screenHeight / 2,
                                playerVelocity = 0f,
                                obstacles = emptyList()
                            )
                        },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .widthIn(min = 200.dp)
                    ) {
                        Text(text = "Play", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { (context as? Activity)?.finish() },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .widthIn(min = 200.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(text = "Exit Game", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${gameState.difficulty.name} MODE",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            gameState = gameState.copy(
                                status = GameStatus.PLAYING,
                                currentScore = 0,
                                playerY = screenHeight / 2,
                                playerVelocity = 0f,
                                obstacles = emptyList()
                            )
                        },
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .heightIn(min = 48.dp)
                            .widthIn(min = 200.dp)
                    ) {
                        Text(text = "Restart", fontSize = 24.sp)
                    }
                    Button(
                        onClick = {
                            gameState = gameState.copy(
                                status = GameStatus.START,
                                currentScore = 0,
                                playerY = screenHeight / 2,
                                playerVelocity = 0f,
                                obstacles = emptyList()
                            )
                        },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .widthIn(min = 200.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(text = "Main Menu", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
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
