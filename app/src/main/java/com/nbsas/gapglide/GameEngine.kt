package com.nbsas.gapglide

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object GameEngine {
    const val JUMP_IMPULSE = -700f
    const val MAX_LIVES = 3
    private const val GRAVITY = 2500f

    fun updatePlayer(state: GameState, dt: Float, screenHeight: Float): GameState {
        if (state.status != GameStatus.PLAYING) return state

        val newVelocity = state.playerVelocity + GRAVITY * dt
        val newY = state.playerY + newVelocity * dt
        
        // Update invincibility timer
        val newInvTimer = (state.invincibilityTimer - dt).coerceAtLeast(0f)

        // Trail particles with pooling concept (limiting count)
        var newParticles = state.particles.toMutableList()
        if (Random.nextFloat() > 0.8f && newParticles.size < 50) {
            newParticles.add(Particle(
                x = 100f, 
                y = newY,
                vx = -250f - Random.nextFloat() * 100f,
                vy = Random.nextFloat() * 60f - 30f,
                life = 1.0f,
                color = Color.White.copy(alpha = 0.4f),
                size = 4f + Random.nextFloat() * 6f
            ))
        }

        // Update camera shake decay
        val newShake = if (state.cameraShake != Offset.Zero) {
            val decay = 0.9f
            if (state.cameraShake.getDistance() < 1f) Offset.Zero 
            else Offset(state.cameraShake.x * decay, state.cameraShake.y * decay)
        } else Offset.Zero

        return state.copy(
            playerY = newY,
            playerVelocity = newVelocity,
            invincibilityTimer = newInvTimer,
            cameraShake = newShake,
            particles = updateParticlesInternal(newParticles, dt)
        )
    }

    private fun updateParticlesInternal(particles: List<Particle>, dt: Float): List<Particle> {
        return particles.map { 
            it.copy(
                x = it.x + it.vx * dt,
                y = it.y + it.vy * dt,
                life = it.life - dt * 2.5f 
            )
        }.filter { it.life > 0 }
    }

    fun createExplosion(state: GameState, x: Float, y: Float): GameState {
        val explosion = mutableListOf<Particle>()
        repeat(20) {
            val angle = Random.nextFloat() * 2 * Math.PI
            val speed = 300f + Random.nextFloat() * 400f
            explosion.add(Particle(
                x = x,
                y = y,
                vx = (cos(angle) * speed).toFloat(),
                vy = (sin(angle) * speed).toFloat(),
                life = 1.0f,
                color = if (Random.nextBoolean()) Color(0xFFFFD700) else Color.White, 
                size = 6f + Random.nextFloat() * 10f
            ))
        }
        // Trigger screen shake on explosion
        val shakeIntensity = 25f
        val randomShake = Offset(
            (Random.nextFloat() - 0.5f) * shakeIntensity,
            (Random.nextFloat() - 0.5f) * shakeIntensity
        )
        
        return state.copy(
            particles = state.particles + explosion,
            cameraShake = randomShake
        )
    }

    fun updateObstacles(state: GameState, dt: Float, screenWidth: Float, screenHeight: Float): GameState {
        if (state.status != GameStatus.PLAYING) return state

        val moveSpeed = state.difficulty.speed
        val newObstacles = state.obstacles.map { it.copy(x = it.x - moveSpeed * dt) }.toMutableList()

        var newScore = state.currentScore
        var livesGained = 0
        
        newObstacles.forEachIndexed { index, obstacle ->
            if (!obstacle.isPassed && obstacle.x + GameState.OBSTACLE_WIDTH < screenWidth / 2) {
                newObstacles[index] = obstacle.copy(isPassed = true)
                newScore++
                if (state.graceModeEnabled && state.lives < MAX_LIVES && newScore % 10 == 0) {
                    livesGained = 1
                }
            }
        }

        newObstacles.removeAll { it.x + GameState.OBSTACLE_WIDTH < -50f }

        if (newObstacles.isEmpty() || newObstacles.last().x < screenWidth - 650f) {
            val lastY = if (newObstacles.isEmpty()) screenHeight / 2 else newObstacles.last().gapY
            val nextY = (lastY + Random.nextFloat() * state.difficulty.maxGapDelta * 2 - state.difficulty.maxGapDelta)
                .coerceIn(250f, screenHeight - 250f)
            
            newObstacles.add(Obstacle(x = screenWidth + 50f, gapY = nextY))
        }

        return state.copy(
            obstacles = newObstacles, 
            currentScore = newScore,
            lives = state.lives + livesGained,
            timeOfDay = (newScore % 40) / 40f
        )
    }

    fun checkCollisions(state: GameState, screenWidth: Float, screenHeight: Float): Boolean {
        if (state.invincibilityTimer > 0f) return false
        if (state.playerY < -20f || state.playerY > (screenHeight * 0.92f) + 20f) return true

        val playerX = screenWidth / 2
        val gapHeight = state.difficulty.gapHeight

        state.obstacles.forEach { obs ->
            val playerLeft = playerX - GameState.PLAYER_RADIUS * 0.6f
            val playerRight = playerX + GameState.PLAYER_RADIUS * 0.6f
            val playerTop = state.playerY - GameState.PLAYER_RADIUS * 0.6f
            val playerBottom = state.playerY + GameState.PLAYER_RADIUS * 0.6f

            val obsLeft = obs.x
            val obsRight = obs.x + GameState.OBSTACLE_WIDTH

            if (playerRight > obsLeft && playerLeft < obsRight) {
                val gapTop = obs.gapY - gapHeight / 2
                val gapBottom = obs.gapY + gapHeight / 2
                if (playerTop < gapTop || playerBottom > gapBottom) {
                    return true
                }
            }
        }
        return false
    }
}
