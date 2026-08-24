package com.nbsas.gapglide.ui

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import com.nbsas.gapglide.*
import com.nbsas.gapglide.ui.PixelArtLibrary.drawAvatar
import com.nbsas.gapglide.ui.PixelArtLibrary.drawHeritageBackground
import com.nbsas.gapglide.ui.PixelArtLibrary.getAvatarSprite
import com.nbsas.gapglide.ui.PixelArtLibrary.getBackgroundLayer
import com.nbsas.gapglide.ui.PixelArtLibrary.getObstacleSprite
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
    initialSound: Boolean = true,
    initialGraceMode: Boolean = false,
    leaderboard: List<ScoreEntry> = emptyList(),
    onNewHighScore: (Int, String) -> Unit = { _, _ -> },
    onNameChanged: (String) -> Unit = {},
    onSceneChanged: (SceneType) -> Unit = {},
    onAvatarChanged: (AvatarType) -> Unit = {},
    onVibrationChanged: (Boolean) -> Unit = {},
    onSoundChanged: (Boolean) -> Unit = {},
    onGraceModeChanged: (Boolean) -> Unit = {}
) {
    var gameState by remember { 
        mutableStateOf(GameState(
            highScore = highScore,
            playerName = initialName,
            selectedScene = initialScene,
            selectedAvatar = initialAvatar,
            vibrationEnabled = initialVibration,
            soundEnabled = initialSound,
            graceModeEnabled = initialGraceMode
        )) 
    }

    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }
    var isScenePickerOpen by remember { mutableStateOf(false) }
    var isAvatarPickerOpen by remember { mutableStateOf(false) }
    var isLeaderboardOpen by remember { mutableStateOf(false) }
    var milestoneMessage by remember { mutableStateOf<String?>(null) }
    var countdownValue by remember { mutableStateOf(3) }
    var menuAnimOffset by remember { mutableStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }

    val scoreScale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    val namePaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 40f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    val context = LocalContext.current
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current

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

    val avatarSprite = remember(gameState.selectedAvatar) {
        getAvatarSprite(gameState.selectedAvatar, GameState.PLAYER_RADIUS, density)
    }
    
    val topObstacleSprite = remember(gameState.selectedScene, screenHeight) {
        getObstacleSprite(gameState.selectedScene, GameState.OBSTACLE_WIDTH, if(screenHeight > 0) screenHeight else 2000f, true, density)
    }
    val bottomObstacleSprite = remember(gameState.selectedScene, screenHeight) {
        getObstacleSprite(gameState.selectedScene, GameState.OBSTACLE_WIDTH, if(screenHeight > 0) screenHeight else 2000f, false, density)
    }

    val bgSky = remember(gameState.selectedScene, screenWidth, screenHeight, gameState.timeOfDay) {
        if (screenWidth > 0) getBackgroundLayer(gameState.selectedScene, Size(screenWidth, screenHeight), density, 0, gameState.timeOfDay) else null
    }
    val bgLandmark = remember(gameState.selectedScene, screenWidth, screenHeight) {
        if (screenWidth > 0) getBackgroundLayer(gameState.selectedScene, Size(screenWidth, screenHeight), density, 1, 0f) else null
    }
    val bgGround = remember(gameState.selectedScene, screenWidth, screenHeight) {
        if (screenWidth > 0) getBackgroundLayer(gameState.selectedScene, Size(screenWidth, screenHeight), density, 2, 0f) else null
    }

    fun vibrate() {
        if (!gameState.vibrationEnabled) return
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

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
                    
                    if (nextState.currentScore > gameState.currentScore) {
                        if (nextState.soundEnabled) GameAudioManager.playScoreSound()
                        scope.launch {
                            scoreScale.snapTo(1.5f)
                            scoreScale.animateTo(1f, spring())
                        }
                        
                        val newlyUnlocked = AvatarType.entries.find { it.unlockScore == nextState.currentScore }
                        if (newlyUnlocked != null) {
                            milestoneMessage = "NEW CHARACTER UNLOCKED: ${newlyUnlocked.displayName}! 🐥"
                        } else if (nextState.currentScore % 10 == 0) {
                            milestoneMessage = if (nextState.selectedScene == SceneType.MIZORAM) {
                                "Congratulations! Mizoram: Clean & Peaceful"
                            } else {
                                "Great Job! ${nextState.currentScore} Points!"
                            }
                        }
                    }

                    if (GameEngine.checkCollisions(nextState, screenWidth, screenHeight)) {
                        if (nextState.soundEnabled) GameAudioManager.playCrashSound()
                        // Screen Shake and Explosion
                        nextState = GameEngine.createExplosion(nextState, screenWidth / 2, nextState.playerY)
                        
                        if (nextState.graceModeEnabled && nextState.lives > 1) {
                            vibrate()
                            nextState = nextState.copy(
                                lives = nextState.lives - 1,
                                invincibilityTimer = 2.0f
                            )
                        } else {
                            vibrate()
                            nextState = nextState.copy(
                                status = GameStatus.GAME_OVER,
                                lives = if (nextState.graceModeEnabled) 0 else nextState.lives
                            )
                            onNewHighScore(nextState.currentScore, nextState.playerName)
                        }
                    }
                    gameState = nextState
                }
            }
        }
    }

    LaunchedEffect(milestoneMessage) {
        if (milestoneMessage != null) {
            kotlinx.coroutines.delay(2000)
            milestoneMessage = null
        }
    }

    BackHandler(enabled = (gameState.status == GameStatus.GAME_OVER || isScenePickerOpen || isAvatarPickerOpen || isPaused || isLeaderboardOpen) && gameState.status != GameStatus.PLAYING) {
        if (isPaused) {
            isPaused = false
        } else if (isScenePickerOpen) {
            isScenePickerOpen = false
        } else if (isAvatarPickerOpen) {
            isAvatarPickerOpen = false
        } else if (isLeaderboardOpen) {
            isLeaderboardOpen = false
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

    LaunchedEffect(gameState.status) {
        var lastTime = withFrameNanos { it }
        while (gameState.status == GameStatus.START) {
            withFrameNanos { time ->
                val dt = (time - lastTime) / 1_000_000_000f
                lastTime = time
                menuAnimOffset += 100f * dt
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .onGloballyPositioned {
                screenWidth = it.size.width.toFloat()
                screenHeight = it.size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    if (gameState.status == GameStatus.PLAYING) {
                        gameState = gameState.copy(playerVelocity = GameEngine.JUMP_IMPULSE)
                        if (gameState.soundEnabled) GameAudioManager.playFlapSound()
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Apply Camera Shake
            withTransform({
                translate(left = gameState.cameraShake.x, top = gameState.cameraShake.y)
            }) {
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
                    gameState.obstacles.forEach { obstacle ->
                        val topHeight = obstacle.gapY - gapHeight / 2
                        drawImage(
                            image = topObstacleSprite,
                            srcOffset = IntOffset(20, (screenHeight - topHeight).toInt()),
                            srcSize = IntSize(GameState.OBSTACLE_WIDTH.toInt(), topHeight.toInt()),
                            dstOffset = IntOffset(obstacle.x.toInt(), 0),
                            dstSize = IntSize(GameState.OBSTACLE_WIDTH.toInt(), topHeight.toInt())
                        )
                        
                        val bottomHeight = size.height - (obstacle.gapY + gapHeight / 2)
                        drawImage(
                            image = bottomObstacleSprite,
                            srcOffset = IntOffset(20, 0),
                            srcSize = IntSize(GameState.OBSTACLE_WIDTH.toInt(), bottomHeight.toInt()),
                            dstOffset = IntOffset(obstacle.x.toInt(), (obstacle.gapY + gapHeight / 2).toInt()),
                            dstSize = IntSize(GameState.OBSTACLE_WIDTH.toInt(), bottomHeight.toInt())
                        )
                    }

                    withTransform({
                        val tilt = (gameState.playerVelocity / 15f).coerceIn(-30f, 30f)
                        rotate(tilt, Offset(size.width / 2, gameState.playerY))
                    }) {
                        val alpha = if (gameState.invincibilityTimer > 0) {
                            if ((gameState.invincibilityTimer * 10).toInt() % 2 == 0) 0.3f else 1.0f
                        } else 1.0f

                        drawImage(
                            image = avatarSprite,
                            dstOffset = IntOffset(
                                (size.width / 2 - GameState.PLAYER_RADIUS).toInt(),
                                (gameState.playerY - GameState.PLAYER_RADIUS).toInt()
                            ),
                            alpha = alpha
                        )
                    }

                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText(
                            gameState.playerName,
                            size.width / 2,
                            gameState.playerY - GameState.PLAYER_RADIUS - 20f,
                            namePaint
                        )
                    }

                    gameState.particles.forEach { particle ->
                        drawCircle(
                            color = particle.color.copy(alpha = particle.life),
                            radius = particle.size * particle.life,
                            center = Offset(particle.x, particle.y)
                        )
                    }
                }
            }
        }

        if (gameState.status == GameStatus.PLAYING || gameState.status == GameStatus.COUNTDOWN || gameState.status == GameStatus.GAME_OVER) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val scoreColor = when {
                    gameState.currentScore >= 100 -> Color(0xFFFFD700)
                    gameState.currentScore >= 50 -> Color(0xFFE91E63)
                    gameState.currentScore >= 10 -> Color(0xFF03A9F4)
                    else -> Color.White
                }
                Text(
                    text = "${gameState.currentScore}",
                    modifier = Modifier.graphicsLayer(scaleX = scoreScale.value, scaleY = scoreScale.value),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 80.sp
                    ),
                    color = scoreColor.copy(alpha = 0.5f)
                )
                
                if (gameState.graceModeEnabled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawAvatar(gameState.selectedAvatar, size.width / 2, size.height / 2, size.width / 2.2f)
                        }
                        Text(
                            text = " x ${gameState.lives}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        if (gameState.status == GameStatus.PLAYING) {
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
        }

        milestoneMessage?.let {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Surface(color = Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(16.dp)) {
                    Text(it, color = Color.White, modifier = Modifier.padding(16.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (gameState.status == GameStatus.START) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .verticalScroll(rememberScrollState())
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
                        .padding(20.dp)
                ) {
                    Text(text = "Gap Glide", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text(text = "Best: ${gameState.highScore} 🏆", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        BasicTextField(
                            value = gameState.playerName,
                            onValueChange = { 
                                val filtered = it.filter { char -> char.isLetterOrDigit() }
                                if (filtered.length <= 10) {
                                    gameState = gameState.copy(playerName = filtered)
                                    onNameChanged(filtered)
                                }
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White)
                        )
                        if (gameState.playerName.isEmpty()) Text("Type Name...", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "DIFFICULTY", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Difficulty.entries.forEach { d ->
                            val sel = gameState.difficulty == d
                            Button(
                                onClick = { gameState = gameState.copy(difficulty = d) }, 
                                modifier = Modifier.weight(1f).height(48.dp), 
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (sel) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = d.name, 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.ExtraBold, 
                                    color = if (sel) Color.Black else Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { isScenePickerOpen = true }, modifier = Modifier.height(48.dp).weight(1f), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48FB1))) {
                            Text(text = "🌍 SCENE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Button(onClick = { isAvatarPickerOpen = true }, modifier = Modifier.height(48.dp).weight(1f), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9575CD))) {
                            Text(text = "🐥 HERO", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { isLeaderboardOpen = true }, modifier = Modifier.height(48.dp).fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03A9F4))) {
                        Text(text = "🏆 LEADERBOARD", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Vibration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(checked = gameState.vibrationEnabled, onCheckedChange = { gameState = gameState.copy(vibrationEnabled = it); onVibrationChanged(it) }, modifier = Modifier.scale(0.7f))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "Sound", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(checked = gameState.soundEnabled, onCheckedChange = { gameState = gameState.copy(soundEnabled = it); onSoundChanged(it) }, modifier = Modifier.scale(0.7f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Grace Mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(checked = gameState.graceModeEnabled, onCheckedChange = { gameState = gameState.copy(graceModeEnabled = it); onGraceModeChanged(it) }, modifier = Modifier.scale(0.7f))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (screenWidth > 0 && screenHeight > 0) {
                                gameState = gameState.copy(
                                    status = GameStatus.COUNTDOWN,
                                    currentScore = 0,
                                    playerY = screenHeight / 2,
                                    playerVelocity = 0f,
                                    obstacles = emptyList(),
                                    lives = if (gameState.graceModeEnabled) GameEngine.MAX_LIVES else 1,
                                    invincibilityTimer = 0f
                                )
                            }
                        },
                        modifier = Modifier.height(56.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text(text = "PLAY", fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        if (gameState.status == GameStatus.COUNTDOWN) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "$countdownValue", fontSize = 120.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }

        if (isPaused) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable { isPaused = false }, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "PAUSED", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { isPaused = false }) { Text("RESUME", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }

        if (gameState.status == GameStatus.GAME_OVER) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(Color(0xFF3700B3), RoundedCornerShape(24.dp)).padding(32.dp)) {
                    Text(text = "GAME OVER", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Score: ${gameState.currentScore}", fontSize = 24.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = {
                        gameState = gameState.copy(
                            status = GameStatus.COUNTDOWN,
                            currentScore = 0,
                            playerY = screenHeight / 2,
                            playerVelocity = 0f,
                            obstacles = emptyList(),
                            lives = if (gameState.graceModeEnabled) GameEngine.MAX_LIVES else 1,
                            invincibilityTimer = 0f
                        )
                    }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                        Text("TRY AGAIN", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { gameState = gameState.copy(status = GameStatus.START) }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                        Text("MENU", color = Color.White)
                    }
                }
            }
        }

        AnimatedVisibility(visible = isScenePickerOpen, enter = fadeIn(), exit = fadeOut()) {
            PickerOverlay(title = "Choose Scene", items = SceneType.entries, selectedItem = gameState.selectedScene, userHighScore = gameState.highScore, onItemSelected = {
                gameState = gameState.copy(selectedScene = it)
                onSceneChanged(it)
                isScenePickerOpen = false
            }, onClose = { isScenePickerOpen = false }, itemContent = { scene ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(60.dp)) { drawHeritageBackground(scene) }
                }
            }, itemLabel = { it.displayName })
        }

        AnimatedVisibility(visible = isAvatarPickerOpen, enter = fadeIn(), exit = fadeOut()) {
            PickerOverlay(title = "Choose Character", items = AvatarType.entries, selectedItem = gameState.selectedAvatar, userHighScore = gameState.highScore, onItemSelected = {
                gameState = gameState.copy(selectedAvatar = it)
                onAvatarChanged(it)
                isAvatarPickerOpen = false
            }, onClose = { isAvatarPickerOpen = false }, itemContent = { avatar ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(40.dp)) { drawAvatar(avatar, size.width / 2, size.height / 2, size.width / 2.5f) }
                }
            }, itemLabel = { it.displayName })
        }

        AnimatedVisibility(visible = isLeaderboardOpen, enter = fadeIn(), exit = fadeOut()) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1A1A2E).copy(alpha = 0.95f)) {
                Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("LEADERBOARD 🏆", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                        IconButton(onClick = { isLeaderboardOpen = false }) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(leaderboard.size) { index ->
                            val entry = leaderboard[index]
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("#${index + 1} ${entry.name}", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${entry.score}", color = Color(0xFFFFD700), fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Button(onClick = { isLeaderboardOpen = false }, modifier = Modifier.fillMaxWidth()) { Text("BACK") }
                }
            }
        }
    }
}

@Composable
fun <T> PickerOverlay(title: String, items: List<T>, selectedItem: T, userHighScore: Int = 999, onItemSelected: (T) -> Unit, onClose: () -> Unit, itemContent: @Composable (T) -> Unit, itemLabel: (T) -> String) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF2D1B4D).copy(alpha = 0.95f)) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onClose, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("Close", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 100.dp), contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                items(items) { item ->
                    val isSelected = item == selectedItem
                    val isLocked = if (item is AvatarType) item.unlockScore > userHighScore else false
                    Card(onClick = { if (!isLocked) onItemSelected(item) }, modifier = Modifier.aspectRatio(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (isLocked) Color.Black.copy(alpha = 0.4f) else Color(0xFF3F2B66).copy(alpha = 0.8f)), border = if (isSelected) androidx.compose.foundation.BorderStroke(width = 3.dp, color = Color(0xFFFF4081)) else null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    if (isLocked) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFFFD700).copy(alpha = 0.6f), modifier = Modifier.size(24.dp))
                                            if (item is AvatarType) Text(text = "${item.unlockScore}", fontSize = 10.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                        }
                                    } else itemContent(item)
                                }
                                Text(text = if (isLocked) "Locked" else itemLabel(item), fontSize = 12.sp, color = if (isLocked) Color.Gray else Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
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
        GameScreen(highScore = 42, initialName = "Tester", initialScene = SceneType.MIZORAM, initialAvatar = AvatarType.PIP, initialVibration = true, initialSound = true)
    }
}
