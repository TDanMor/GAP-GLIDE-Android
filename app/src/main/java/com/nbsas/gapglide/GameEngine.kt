package com.nbsas.gapglide

object GameEngine {
    const val GRAVITY = 1500f
    const val JUMP_IMPULSE = -600f

    fun updatePlayer(state: GameState, dt: Float, screenHeight: Float): GameState {
        if (state.status != GameStatus.PLAYING) return state

        val newVelocity = state.playerVelocity + GRAVITY * dt
        val newY = state.playerY + newVelocity * dt

        // Collision with top or bottom
        return if (newY < 0 || newY > screenHeight) {
            state.copy(
                status = GameStatus.GAME_OVER,
                playerY = newY.coerceIn(0f, screenHeight),
                playerVelocity = 0f
            )
        } else {
            state.copy(
                playerY = newY,
                playerVelocity = newVelocity
            )
        }
    }

    fun updateObstacles(state: GameState, dt: Float, screenWidth: Float, screenHeight: Float): GameState {
        if (state.status != GameStatus.PLAYING) return state

        val speed = state.difficulty.speed
        val gapHeight = state.difficulty.gapHeight

        // Move all obstacles left
        var movedObstacles = state.obstacles.map { it.copy(x = it.x - speed * dt) }

        // Scoring logic
        val playerX = screenWidth / 2
        var newScore = state.currentScore
        movedObstacles = movedObstacles.map { obstacle ->
            if (!obstacle.isPassed && obstacle.x + GameState.OBSTACLE_WIDTH < playerX) {
                newScore++
                obstacle.copy(isPassed = true)
            } else {
                obstacle
            }
        }

        // Remove off-screen obstacles
        val visibleObstacles = movedObstacles.filter { it.x + GameState.OBSTACLE_WIDTH > 0 }

        // Spawn a new obstacle if the last one has moved a certain distance
        val updatedObstacles = visibleObstacles.toMutableList()
        val lastObstacle = visibleObstacles.lastOrNull()
        val lastObstacleX = lastObstacle?.x ?: 0f
        
        // If no obstacles, or if the last obstacle has moved far enough from the right edge
        if (updatedObstacles.isEmpty() || screenWidth - lastObstacleX >= screenWidth * 0.6f) {
            val absMinY = gapHeight / 2 + 100f
            val absMaxY = screenHeight - (gapHeight / 2 + 100f)
            
            val newGapY = if (absMaxY > absMinY) {
                if (lastObstacle == null) {
                    // First obstacle: unrestricted safe range
                    (absMinY.toInt()..absMaxY.toInt()).random().toFloat()
                } else {
                    // Following obstacles: clamp to maxGapDelta
                    val rangeMin = maxOf(absMinY, lastObstacle.gapY - state.difficulty.maxGapDelta)
                    val rangeMax = minOf(absMaxY, lastObstacle.gapY + state.difficulty.maxGapDelta)
                    (rangeMin.toInt()..rangeMax.toInt()).random().toFloat()
                }
            } else {
                screenHeight / 2
            }
            updatedObstacles.add(Obstacle(x = screenWidth, gapY = newGapY))
        }

        return state.copy(obstacles = updatedObstacles, currentScore = newScore)
    }

    fun checkCollisions(state: GameState, screenWidth: Float, screenHeight: Float): Boolean {
        if (state.status != GameStatus.PLAYING) return false

        val playerX = screenWidth / 2
        val playerY = state.playerY
        val radius = GameState.PLAYER_RADIUS
        val gapHeight = state.difficulty.gapHeight

        state.obstacles.forEach { obstacle ->
            // Top pipe
            val topRectLeft = obstacle.x
            val topRectTop = 0f
            val topRectRight = obstacle.x + GameState.OBSTACLE_WIDTH
            val topRectBottom = obstacle.gapY - gapHeight / 2

            // Bottom pipe
            val bottomRectLeft = obstacle.x
            val bottomRectTop = obstacle.gapY + gapHeight / 2
            val bottomRectRight = obstacle.x + GameState.OBSTACLE_WIDTH
            val bottomRectBottom = screenHeight // Use dynamic screen height instead of hardcoded value

            if (circleRectCollision(playerX, playerY, radius, topRectLeft, topRectTop, topRectRight, topRectBottom) ||
                circleRectCollision(playerX, playerY, radius, bottomRectLeft, bottomRectTop, bottomRectRight, bottomRectBottom)) {
                return true
            }
        }
        return false
    }

    private fun circleRectCollision(cx: Float, cy: Float, cr: Float, left: Float, top: Float, right: Float, bottom: Float): Boolean {
        val minX = minOf(left, right)
        val maxX = maxOf(left, right)
        val minY = minOf(top, bottom)
        val maxY = maxOf(top, bottom)

        val closestX = cx.coerceIn(minX, maxX)
        val closestY = cy.coerceIn(minY, maxY)
        val dx = cx - closestX
        val dy = cy - closestY
        return (dx * dx + dy * dy) < (cr * cr)
    }
}
