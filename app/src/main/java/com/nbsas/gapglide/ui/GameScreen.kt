package com.nbsas.gapglide.ui

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import com.nbsas.gapglide.ui.PixelArtLibrary.getAvatarSprite
import com.nbsas.gapglide.ui.PixelArtLibrary.getObstacleSprite
import com.nbsas.gapglide.ui.PixelArtLibrary.getBackgroundLayer
import com.nbsas.gapglide.ui.theme.GapGlideTheme
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    highScore: Int = 0,
    initialName: String = "Hero",
    initialScene: SceneType = SceneType.TAJ_MAHAL,
    initialAvatar: AvatarType = AvatarType.NOVA,
    initialVibration: Boolean = true,
    onNewHighScore: (Int) -> Unit = {},
    onNameChanged: (String) -> Unit = {},
    onSceneChanged: (SceneType) -> Unit = {},
    onAvatarChanged: (AvatarType) -> Unit = {},
    onVibrationChanged: (Boolean) -> Unit = {}
) {
    var gameState by remember { 
        mutableStateOf(GameState(
            highScore = highScore,
            playerName = initialName,
            selectedScene = initialScene,
            selectedAvatar = initialAvatar,
            vibrationEnabled = initialVibration
        )) 
    }
    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }
    var isScenePickerOpen by remember { mutableStateOf(false) }
    var isAvatarPickerOpen by remember { mutableStateOf(false) }
    var milestoneMessage by remember { mutableStateOf<String?>(null) }
    var countdownValue by remember { mutableStateOf(3) }
    var menuAnimOffset by remember { mutableStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }

    val scoreScale = remember { androidx.compose.animation.core.Animatable(1f) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    // Optimization: Cache Paint object for name tag
    val namePaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 40f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    // Optimization: Generate fixed random offsets for background decorations
    val decorationOffsets = remember(gameState.selectedScene) {
        List(20) { Offset(kotlin.random.Random.nextFloat(), kotlin.random.Random.nextFloat()) }
    }

    val context = LocalContext.current
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Lifecycle Observer for Auto-Pause
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE && gameState.status == GameStatus.PLAYING) {
                isPaused = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Optimization: Cache Sprites
    val avatarSprite = remember(gameState.selectedAvatar) {
        getAvatarSprite(gameState.selectedAvatar, GameState.PLAYER_RADIUS, density)
    }
    
    val topObstacleSprite = remember(gameState.selectedScene, screenHeight) {
        getObstacleSprite(gameState.selectedScene, GameState.OBSTACLE_WIDTH, if(screenHeight > 0) screenHeight else 2000f, true, density)
    }
    val bottomObstacleSprite = remember(gameState.selectedScene, screenHeight) {
        getObstacleSprite(gameState.selectedScene, GameState.OBSTACLE_WIDTH, if(screenHeight > 0) screenHeight else 2000f, false, density)
    }

    // Optimization: Pre-render Background Layers
    val bgSky = remember(gameState.selectedScene, screenWidth, screenHeight) {
        if (screenWidth > 0) getBackgroundLayer(gameState.selectedScene, Size(screenWidth, screenHeight), density, 0) else null
    }
    val bgLandmark = remember(gameState.selectedScene, screenWidth, screenHeight) {
        if (screenWidth > 0) getBackgroundLayer(gameState.selectedScene, Size(screenWidth, screenHeight), density, 1) else null
    }
    val bgGround = remember(gameState.selectedScene, screenWidth, screenHeight) {
        if (screenWidth > 0) getBackgroundLayer(gameState.selectedScene, Size(screenWidth, screenHeight), density, 2) else null
    }

    fun vibrate() {
        if (!gameState.vibrationEnabled) return
        
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    // Sync highScore from external state
    LaunchedEffect(highScore) {
        gameState = gameState.copy(highScore = highScore)
    }

    // Handle Back button on Game Over
    BackHandler(enabled = gameState.status == GameStatus.GAME_OVER || isScenePickerOpen || isAvatarPickerOpen || isPaused) {
        if (isPaused) {
            isPaused = false
        } else if (isScenePickerOpen) {
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

    // Menu Animation Loop
    LaunchedEffect(gameState.status) {
        var lastTime = withFrameNanos { it }
        while (true) {
            withFrameNanos { time ->
                val dt = (time - lastTime) / 1_000_000_000f
                lastTime = time
                menuAnimOffset += dt * 100f // Consistent scrolling speed
            }
        }
    }

    // Game Loop
    LaunchedEffect(gameState.status, isPaused) {
        if (isPaused) return@LaunchedEffect

        if (gameState.status == GameStatus.COUNTDOWN) {
            for (i in 3 downTo 1) {
                countdownValue = i
                kotlinx.coroutines.delay(1000)
            }
            gameState = gameState.copy(status = GameStatus.PLAYING)
        }

        if (gameState.status == GameStatus.PLAYING) {
            var lastTime = withFrameNanos { it }
            while (gameState.status == GameStatus.PLAYING && !isPaused) {
                withFrameNanos { time ->
                    val dt = (time - lastTime) / 1_000_000_000f
                    lastTime = time
                    
                    var nextState = GameEngine.updatePlayer(gameState, dt, screenHeight)
                    nextState = GameEngine.updateObstacles(nextState, dt, screenWidth, screenHeight)
                    
                    // Score animation
                    if (nextState.currentScore > gameState.currentScore) {
                        scope.launch {
                            scoreScale.snapTo(1.5f)
                            scoreScale.animateTo(1f, androidx.compose.animation.core.spring())
                        }
                    }

                    // Milestone Logic
                    if (nextState.currentScore > 0 && nextState.currentScore % 10 == 0 && nextState.currentScore != gameState.currentScore) {
                        milestoneMessage = if (nextState.selectedScene == SceneType.MIZORAM) {
                            "Congratulations! Mizoram: Clean & Peaceful"
                        } else {
                            "Great Job! ${nextState.currentScore} Points!"
                        }
                    }

                    if (GameEngine.checkCollisions(nextState, screenWidth, screenHeight)) {
                        vibrate()
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

    // Hide milestone message after a delay
    LaunchedEffect(milestoneMessage) {
        if (milestoneMessage != null) {
            kotlinx.coroutines.delay(2000)
            milestoneMessage = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F)) // Base background
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
            // Draw Pre-rendered Background Layers with Parallax
            bgSky?.let { drawImage(it) }
            
            bgLandmark?.let { 
                val landmarkOffset = (menuAnimOffset * 0.2f) % size.width
                drawImage(it, dstOffset = IntOffset(-landmarkOffset.toInt(), 0))
                drawImage(it, dstOffset = IntOffset((size.width - landmarkOffset).toInt(), 0))
            }
            
            bgGround?.let { 
                val groundOffset = (menuAnimOffset * 0.5f) % size.width
                drawImage(it, dstOffset = IntOffset(-groundOffset.toInt(), 0))
                drawImage(it, dstOffset = IntOffset((size.width - groundOffset).toInt(), 0))
            }

            if (gameState.status != GameStatus.START) {
                val gapHeight = gameState.difficulty.gapHeight
                // Draw Obstacles
                gameState.obstacles.forEach { obstacle ->
                    // Top pipe (Cached)
                    val topHeight = obstacle.gapY - gapHeight / 2
                    drawImage(
                        image = topObstacleSprite,
                        srcOffset = IntOffset(0, (screenHeight - topHeight).toInt()),
                        srcSize = IntSize(GameState.OBSTACLE_WIDTH.toInt(), topHeight.toInt()),
                        dstOffset = IntOffset(obstacle.x.toInt(), 0),
                        dstSize = IntSize(GameState.OBSTACLE_WIDTH.toInt(), topHeight.toInt())
                    )
                    
                    // Bottom pipe (Cached)
                    val bottomHeight = size.height - (obstacle.gapY + gapHeight / 2)
                    drawImage(
                        image = bottomObstacleSprite,
                        srcOffset = IntOffset(0, 0),
                        srcSize = IntSize(GameState.OBSTACLE_WIDTH.toInt(), bottomHeight.toInt()),
                        dstOffset = IntOffset(obstacle.x.toInt(), (obstacle.gapY + gapHeight / 2).toInt()),
                        dstSize = IntSize(GameState.OBSTACLE_WIDTH.toInt(), bottomHeight.toInt())
                    )
                }

                // Draw Player (Cached & Tilted)
                withTransform({
                    val tilt = (gameState.playerVelocity / 15f).coerceIn(-30f, 30f)
                    rotate(tilt, Offset(size.width / 2, gameState.playerY))
                }) {
                    drawImage(
                        image = avatarSprite,
                        dstOffset = IntOffset(
                            (size.width / 2 - GameState.PLAYER_RADIUS).toInt(),
                            (gameState.playerY - GameState.PLAYER_RADIUS).toInt()
                        )
                    )
                }

                // Floating Name Tag
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        gameState.playerName,
                        size.width / 2,
                        gameState.playerY - GameState.PLAYER_RADIUS - 20f,
                        namePaint
                    )
                }
            }
        }

        // Playing UI (Score overlay)
        if (gameState.status == GameStatus.PLAYING) {
            // Difficulty indicator
            Text(
                text = "${gameState.difficulty.name} MODE",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .safeDrawingPadding()
                    .padding(16.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f)
            )

            Text(
                text = "${gameState.currentScore}",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .safeDrawingPadding()
                    .padding(top = 32.dp)
                    .graphicsLayer(scaleX = scoreScale.value, scaleY = scoreScale.value),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 120.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            // Milestone Toast
            milestoneMessage?.let { msg ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = msg,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700), // Golden
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 40.sp
                    )
                }
            }
        }

        // Countdown Overlay
        if (gameState.status == GameStatus.COUNTDOWN) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$countdownValue",
                    fontSize = 150.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        // Pause Overlay
        AnimatedVisibility(
            visible = isPaused,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { isPaused = false },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PAUSED",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { isPaused = false }) {
                        Text("RESUME", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(32.dp)
                        )
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Gap Glide",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Your Best Score: ${gameState.highScore} 🏆",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700) // Golden
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Name Input Box
                    Text(
                        text = "HERO NAME",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(48.dp)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = gameState.playerName,
                            onValueChange = { 
                                if (it.length <= 12) {
                                    gameState = gameState.copy(playerName = it)
                                    onNameChanged(it)
                                }
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            ),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White)
                        )
                        if (gameState.playerName.isEmpty()) {
                            Text("Type Name...", color = Color.White.copy(alpha = 0.3f), fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Difficulty Selection
                    Text(
                        text = "DIFFICULTY",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Difficulty.entries.forEach { difficulty ->
                            val isSelected = gameState.difficulty == difficulty
                            Button(
                                onClick = { gameState = gameState.copy(difficulty = difficulty) },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f)
                                )
                            ) {
                                Text(
                                    text = difficulty.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else Color.White
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Scene Selection
                    Button(
                        onClick = { isScenePickerOpen = true },
                        modifier = Modifier
                            .heightIn(min = 56.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(text = "🌍 SCENE: ${gameState.selectedScene.displayName}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Avatar Selection
                    Button(
                        onClick = { isAvatarPickerOpen = true },
                        modifier = Modifier
                            .heightIn(min = 56.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text(text = "🐥 CHARACTER: ${gameState.selectedAvatar.displayName}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Vibration Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vibration",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        androidx.compose.material3.Switch(
                            checked = gameState.vibrationEnabled,
                            onCheckedChange = {
                                gameState = gameState.copy(vibrationEnabled = it)
                                onVibrationChanged(it)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            gameState = gameState.copy(
                                status = GameStatus.COUNTDOWN,
                                currentScore = 0,
                                playerY = screenHeight / 2,
                                playerVelocity = 0f,
                                obstacles = emptyList()
                            )
                        },
                        modifier = Modifier
                            .heightIn(min = 64.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50) // Vibrant Green
                        )
                    ) {
                        Text(text = "PLAY", fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { (context as? Activity)?.finish() },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .fillMaxWidth(0.6f),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(text = "EXIT", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                        text = "Good Job, ${gameState.playerName}!",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${gameState.difficulty.name} MODE",
                        fontSize = 16.sp,
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
                                status = GameStatus.COUNTDOWN,
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
        color = Color(0xFF2D1B4D).copy(alpha = 0.95f) // Deep purple like the photo
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(items) { item ->
                    val isSelected = item == selectedItem
                    // Visual lock for slots 6 and above to match user photo style
                    val isLocked = items.indexOf(item) > 5 

                    Card(
                        onClick = { if (!isLocked) onItemSelected(item) },
                        modifier = Modifier
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF3F2B66).copy(alpha = 0.8f)
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(
                            width = 3.dp,
                            color = Color(0xFFFF4081) // Vibrant selection border
                        ) else null
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    if (isLocked) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = Color(0xFFFFD700).copy(alpha = 0.6f),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    } else {
                                        itemContent(item)
                                    }
                                }
                                Text(
                                    text = itemLabel(item),
                                    fontSize = 14.sp,
                                    color = if (isLocked) Color.Gray else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
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
        GameScreen(
            highScore = 42,
            initialName = "Tester",
            initialScene = SceneType.MIZORAM,
            initialAvatar = AvatarType.PIP,
            initialVibration = true
        )
    }
}
