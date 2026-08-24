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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    var isScenePickerOpen by remember { mutableStateOf(false) }
    var isAvatarPickerOpen by remember { mutableStateOf(false) }

    // Sync highScore from external state
    LaunchedEffect(highScore) {
        gameState = gameState.copy(highScore = highScore)
    }

    // Handle Back button on Game Over
    BackHandler(enabled = gameState.status == GameStatus.GAME_OVER || isScenePickerOpen || isAvatarPickerOpen) {
        if (isScenePickerOpen) {
            isScenePickerOpen = false
        } else if (isAvatarPickerOpen) {
            isAvatarPickerOpen = false
        } else {
            gameState = gameState.copy(
                status = GameStatus.START,
                currentScore = 0,
                playerY = screenHeight / 2,
                playerVelocity = 0f,
                obstacles = emptyList()
            )
        }
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
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
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
                    Button(
                        onClick = { isScenePickerOpen = true },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .widthIn(min = 200.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(text = "Scene: ${gameState.selectedScene.displayName}", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Avatar Selection
                    Button(
                        onClick = { isAvatarPickerOpen = true },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .widthIn(min = 200.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text(text = "Character: ${gameState.selectedAvatar.displayName}", fontSize = 16.sp)
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

        // Scene Picker Overlay
        AnimatedVisibility(
            visible = isScenePickerOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PickerOverlay(
                title = "Choose Scene",
                items = SceneType.entries,
                selectedItem = gameState.selectedScene,
                onItemSelected = {
                    gameState = gameState.copy(selectedScene = it)
                    onSceneChanged(it)
                    isScenePickerOpen = false
                },
                onClose = { isScenePickerOpen = false },
                itemContent = { scene ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(60.dp)) {
                            drawHeritageBackground(scene)
                        }
                    }
                },
                itemLabel = { it.displayName }
            )
        }

        // Avatar Picker Overlay
        AnimatedVisibility(
            visible = isAvatarPickerOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PickerOverlay(
                title = "Choose Character",
                items = AvatarType.entries,
                selectedItem = gameState.selectedAvatar,
                onItemSelected = {
                    gameState = gameState.copy(selectedAvatar = it)
                    onAvatarChanged(it)
                    isAvatarPickerOpen = false
                },
                onClose = { isAvatarPickerOpen = false },
                itemContent = { avatar ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(40.dp)) {
                            drawAvatar(avatar, size.width / 2, size.height / 2, size.width / 2.5f)
                        }
                    }
                },
                itemLabel = { it.displayName }
            )
        }
    }
}

@Composable
fun <T> PickerOverlay(
    title: String,
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    onClose: () -> Unit,
    itemContent: @Composable (T) -> Unit,
    itemLabel: (T) -> String
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Button(onClick = onClose) {
                    Text("Close")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(items) { item ->
                    val isSelected = item == selectedItem
                    Card(
                        onClick = { onItemSelected(item) },
                        modifier = Modifier
                            .aspectRatio(1f)
                            .then(
                                if (isSelected) Modifier.background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                ) else Modifier
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.DarkGray.copy(alpha = 0.5f)
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        ) else null
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                itemContent(item)
                            }
                            Text(
                                text = itemLabel(item),
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
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
