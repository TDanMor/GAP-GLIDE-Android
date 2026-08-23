package com.nbsas.gapglide

enum class GameStatus {
    START,
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

data class Obstacle(val x: Float, val gapY: Float, val isPassed: Boolean = false)

data class GameState(
    val status: GameStatus = GameStatus.START,
    val currentScore: Int = 0,
    val highScore: Int = 0,
    val playerY: Float = 0f,
    val playerVelocity: Float = 0f,
    val obstacles: List<Obstacle> = emptyList(),
    val difficulty: Difficulty = Difficulty.MEDIUM
) {
    companion object {
        const val PLAYER_RADIUS = 50f
        const val OBSTACLE_WIDTH = 120f
    }
}
