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

enum class AvatarType(val displayName: String) {
    NOVA("Nova"),
    BIRD("Pixel Bird"),
    CAT("Funny Cat"),
    ROBOT("Retro Robot"),
    ELEPHANT("Tiny Elephant"),
    TIGER("Cute Tiger"),
    DRAGON("Tiny Dragon"),
    PANDA("Chubby Panda"),
    ALIEN("Space Alien"),
    GHOST("Spooky Ghost"),
    DOG("Happy Dog"),
    BUNNY("Quick Bunny"),
    FROG("Leap Frog"),
    FOX("Cunning Fox"),
    KOALA("Sleepy Koala"),
    MONKEY("Cheeky Monkey")
}

data class Obstacle(val x: Float, val gapY: Float, val isPassed: Boolean = false)

data class GameState(
    val status: GameStatus = GameStatus.START,
    val currentScore: Int = 0,
    val highScore: Int = 0,
    val playerY: Float = 0f,
    val playerVelocity: Float = 0f,
    val obstacles: List<Obstacle> = emptyList(),
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val selectedScene: SceneType = SceneType.TAJ_MAHAL,
    val selectedAvatar: AvatarType = AvatarType.NOVA
) {
    companion object {
        const val PLAYER_RADIUS = 50f
        const val OBSTACLE_WIDTH = 120f
    }
}
