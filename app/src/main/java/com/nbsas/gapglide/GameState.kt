package com.nbsas.gapglide

enum class GameStatus {
    START,
    COUNTDOWN,
    PLAYING,
    GAME_OVER
}

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD;

    val speed: Float
        get() = when (this) {
            EASY -> 250f
            MEDIUM -> 350f
            HARD -> 480f
        }

    val gapHeight: Float
        get() = when (this) {
            EASY -> 500f
            MEDIUM -> 420f
            HARD -> 340f
        }

    val maxGapDelta: Float
        get() = when (this) {
            EASY -> 250f
            MEDIUM -> 300f
            HARD -> 360f
        }
}

enum class SceneType(val displayName: String) {
    TAJ_MAHAL("Taj Mahal"),
    MIZORAM("Mizoram"),
    GREAT_WALL("Great Wall"),
    PYRAMIDS("Pyramids"),
    EIFFEL_TOWER("Eiffel Tower"),
    COLOSSEUM("Colosseum"),
    STATUE_OF_LIBERTY("Statue of Liberty"),
    CHICHEN_ITZA("Chichen Itza"),
    MACHU_PICCHU("Machu Picchu"),
    STONEHENGE("Stonehenge")
}

enum class AvatarType(val displayName: String, val unlockScore: Int) {
    PIP("Pip", 0),
    MOMO("Momo", 0),
    WAFFLE("Waffle", 0),
    SNUG("Snug", 0),
    EMBER("Ember", 0),
    NIMBUS("Nimbus", 0),
    STARBIT("Star-bit", 10),
    LUNA("Luna", 20),
    BLIP("Blip", 30),
    GIZMO("Gizmo", 40),
    SPROCKET("Sprocket", 50),
    ZEPHYR("Zephyr", 60),
    FIZZ("Fizz", 70),
    PEBBLE("Pebble", 80),
    NOVA("Nova", 90),
    TIGER("Cute Tiger", 100)
}

data class Obstacle(val x: Float, val gapY: Float, val isPassed: Boolean = false)

data class ScoreEntry(val name: String, val score: Int, val date: String)

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val life: Float, // 1.0 down to 0.0
    val color: androidx.compose.ui.graphics.Color,
    val size: Float
)

data class GameState(
    val status: GameStatus = GameStatus.START,
    val currentScore: Int = 0,
    val highScore: Int = 0,
    val playerName: String = "Player",
    val playerY: Float = 0f,
    val playerVelocity: Float = 0f,
    val obstacles: List<Obstacle> = emptyList(),
    val particles: List<Particle> = emptyList(),
    val cameraShake: androidx.compose.ui.geometry.Offset = androidx.compose.ui.geometry.Offset.Zero,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val selectedScene: SceneType = SceneType.TAJ_MAHAL,
    val selectedAvatar: AvatarType = AvatarType.NOVA,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val graceModeEnabled: Boolean = false,
    val lives: Int = 3,
    val invincibilityTimer: Float = 0f,
    val timeOfDay: Float = 0f // 0.0 (Day) -> 0.5 (Sunset) -> 1.0 (Night)
) {
    companion object {
        const val PLAYER_RADIUS = 50f
        const val OBSTACLE_WIDTH = 120f
    }
}
