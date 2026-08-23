package com.nbsas.gapglide.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.nbsas.gapglide.AvatarType
import com.nbsas.gapglide.SceneType

object PixelArtLibrary {

    fun DrawScope.drawHeritageBackground(scene: SceneType) {
        when (scene) {
            SceneType.TAJ_MAHAL -> drawTajMahalSky()
            SceneType.MIZORAM -> drawMizoramHills()
            else -> {} // Default background
        }
    }

    private fun DrawScope.drawTajMahalSky() {
        // Deep blue sky with subtle pixel clouds
        drawRect(Color(0xFF1A237E), Offset.Zero, size)
        drawRect(Color.White.copy(alpha = 0.1f), Offset(100f, 200f), Size(200f, 40f))
        drawRect(Color.White.copy(alpha = 0.1f), Offset(400f, 150f), Size(150f, 30f))
    }

    private fun DrawScope.drawMizoramHills() {
        // Lush green hills
        drawRect(Color(0xFFE3F2FD), Offset.Zero, size) // Peaceful sky
        drawRect(Color(0xFF2E7D32), Offset(0f, size.height * 0.7f), Size(size.width, size.height * 0.3f))
        drawRect(Color(0xFF1B5E20), Offset(0f, size.height * 0.85f), Size(size.width, size.height * 0.15f))
    }

    fun DrawScope.drawThemedObstacle(scene: SceneType, x: Float, y: Float, width: Float, height: Float, isTop: Boolean) {
        when (scene) {
            SceneType.TAJ_MAHAL -> drawMarbleMinaret(x, y, width, height, isTop)
            SceneType.MIZORAM -> drawBambooPole(x, y, width, height, isTop)
            else -> drawGenericBlock(x, y, width, height)
        }
    }

    private fun DrawScope.drawMarbleMinaret(x: Float, y: Float, width: Float, height: Float, isTop: Boolean) {
        val marbleColor = Color(0xFFF5F5F5)
        val shadowColor = Color(0xFFE0E0E0)
        val capColor = Color(0xFFFFD700) // Golden dome cap

        // Main pillar
        drawRect(shadowColor, Offset(x, y), Size(width, height))
        drawRect(marbleColor, Offset(x + 10f, y), Size(width - 20f, height))

        // Dome cap at the gap entrance
        val capY = if (isTop) y + height - 30f else y
        drawRect(capColor, Offset(x - 10f, capY), Size(width + 20f, 30f))
    }

    private fun DrawScope.drawBambooPole(x: Float, y: Float, width: Float, height: Float, isTop: Boolean) {
        val bambooColor = Color(0xFF8DB600)
        val jointColor = Color(0xFF4B5320)
        
        drawRect(bambooColor, Offset(x, y), Size(width, height))
        
        // Bamboo joints
        val step = 100f
        var currentY = y
        while (currentY < y + height) {
            drawRect(jointColor, Offset(x, currentY), Size(width, 10f))
            currentY += step
        }

        // Leaf details at the gap entrance
        val leafY = if (isTop) y + height - 20f else y
        drawRect(Color(0xFF2E7D32), Offset(x - 20f, leafY), Size(40f, 20f))
        drawRect(Color(0xFF2E7D32), Offset(x + width - 20f, leafY), Size(40f, 20f))
    }

    private fun DrawScope.drawGenericBlock(x: Float, y: Float, width: Float, height: Float) {
        drawRect(Color(0xFFD0BCFF), Offset(x, y), Size(width, height))
    }

    fun DrawScope.drawAvatar(avatar: AvatarType, x: Float, y: Float, radius: Float) {
        when (avatar) {
            AvatarType.NOVA -> {
                drawCircle(Color(0xFFEFB8C8), radius, Offset(x, y))
                // Simple wings
                drawRect(Color.White.copy(alpha = 0.5f), Offset(x - radius * 1.5f, y - 10f), Size(radius, 20f))
                drawRect(Color.White.copy(alpha = 0.5f), Offset(x + radius * 0.5f, y - 10f), Size(radius, 20f))
            }
            AvatarType.CAT -> {
                drawRect(Color(0xFFFFB74D), Offset(x - radius, y - radius), Size(radius * 2, radius * 2))
                // Ears
                drawRect(Color(0xFFFFB74D), Offset(x - radius, y - radius - 10f), Size(10f, 10f))
                drawRect(Color(0xFFFFB74D), Offset(x + radius - 10f, y - radius - 10f), Size(10f, 10f))
            }
            AvatarType.BIRD -> {
                drawRect(Color.Yellow, Offset(x - radius, y - radius), Size(radius * 2, radius * 2))
                // Beak
                drawRect(Color.Red, Offset(x + radius - 5f, y - 5f), Size(10f, 10f))
            }
            else -> {
                // Default blocky avatar
                drawRect(Color.White, Offset(x - radius, y - radius), Size(radius * 2, radius * 2))
            }
        }
    }
}
