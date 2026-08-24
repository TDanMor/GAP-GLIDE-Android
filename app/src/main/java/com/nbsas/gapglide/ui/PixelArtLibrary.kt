package com.nbsas.gapglide.ui

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.nbsas.gapglide.AvatarType
import com.nbsas.gapglide.SceneType

object PixelArtLibrary {

    private val cachedPath = Path()
    private val spriteCache = mutableMapOf<String, ImageBitmap>()

    fun getAvatarSprite(avatar: AvatarType, radius: Float, density: Density): ImageBitmap {
        val key = "avatar_${avatar.name}_$radius"
        return spriteCache.getOrPut(key) {
            val size = (radius * 2.5f).toInt()
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap.asImageBitmap())
            val drawScope = CanvasDrawScope()
            drawScope.draw(density, LayoutDirection.Ltr, canvas, Size(size.f, size.f)) {
                drawAvatarInternal(avatar, size.f / 2, size.f / 2, radius)
            }
            bitmap.asImageBitmap()
        }
    }

    fun getObstacleSprite(scene: SceneType, width: Float, height: Float, isTop: Boolean, density: Density): ImageBitmap {
        val key = "obs_${scene.name}_${width}_${height}_$isTop"
        return spriteCache.getOrPut(key) {
            val bitmap = Bitmap.createBitmap(width.toInt() + 20, height.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap.asImageBitmap())
            val drawScope = CanvasDrawScope()
            drawScope.draw(density, LayoutDirection.Ltr, canvas, Size(width + 20f, height)) {
                drawThemedObstacleInternal(scene, 10f, 0f, width, height, isTop)
            }
            bitmap.asImageBitmap()
        }
    }

    // New: Cache for background layers to stop lag
    fun getBackgroundLayer(scene: SceneType, size: Size, density: Density, layer: Int): ImageBitmap {
        val key = "bg_${scene.name}_${size.width}_${size.height}_$layer"
        return spriteCache.getOrPut(key) {
            val bitmap = Bitmap.createBitmap(size.width.toInt(), size.height.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap.asImageBitmap())
            val drawScope = CanvasDrawScope()
            drawScope.draw(density, LayoutDirection.Ltr, canvas, size) {
                when(layer) {
                    0 -> drawSkyOnly(scene)
                    1 -> drawLandmarksOnly(scene)
                    2 -> drawGroundOnly(scene)
                }
            }
            bitmap.asImageBitmap()
        }
    }

    private val Int.f get() = this.toFloat()

    private fun DrawScope.drawSkyOnly(scene: SceneType) {
        val skyColor = when(scene) {
            SceneType.TAJ_MAHAL -> Color(0xFF1A1A2E)
            SceneType.MIZORAM -> Color(0xFF81D4FA)
            SceneType.PYRAMIDS -> Color(0xFFFFE082)
            else -> Color(0xFFE3F2FD)
        }
        drawRect(skyColor, Offset.Zero, size)
        
        // Sun/Moon
        val celestialColor = if(scene == SceneType.TAJ_MAHAL) Color(0xFFE0E0E0) else Color(0xFFFFEB3B)
        drawCircle(celestialColor, 50f, Offset(size.width * 0.8f, 120f))
    }

    private fun DrawScope.drawLandmarksOnly(scene: SceneType) {
        when(scene) {
            SceneType.TAJ_MAHAL -> {
                val cx = size.width / 2
                val baseY = size.height * 0.92f
                drawRect(Color(0xFFF1F1F1), Offset(cx - 170f, baseY - 110f), Size(340f, 110f))
                drawCircle(Color(0xFFF1F1F1), 80f, Offset(cx, baseY - 310f))
            }
            SceneType.MIZORAM -> {
                cachedPath.reset()
                cachedPath.moveTo(0f, size.height * 0.8f)
                cachedPath.lineTo(size.width * 0.5f, size.height * 0.4f)
                cachedPath.lineTo(size.width, size.height * 0.8f)
                cachedPath.close()
                drawPath(cachedPath, Color(0xFF1E88E5).copy(alpha = 0.2f))
            }
            // Add other simplified landmarks here...
            else -> {}
        }
    }

    private fun DrawScope.drawGroundOnly(scene: SceneType) {
        val groundY = size.height * 0.92f
        val groundColor = when(scene) {
            SceneType.MIZORAM -> Color(0xFF558B2F)
            SceneType.PYRAMIDS -> Color(0xFFD4A373)
            else -> Color(0xFF424242)
        }
        drawRect(groundColor, Offset(0f, groundY), Size(size.width, size.height * 0.08f))
    }

    private fun DrawScope.drawThemedObstacleInternal(scene: SceneType, x: Float, y: Float, width: Float, height: Float, isTop: Boolean) {
        val baseColor = when(scene) {
            SceneType.TAJ_MAHAL -> Color(0xFFF5F5F5)
            SceneType.MIZORAM -> Color(0xFF8BC34A)
            SceneType.PYRAMIDS -> Color(0xFFFFD54F)
            else -> Color(0xFF9E9E9E)
        }
        drawRect(baseColor, Offset(x, y), Size(width, height))
        // Simple bevel
        drawRect(Color.White.copy(alpha = 0.3f), Offset(x, y), Size(10f, height))
        drawRect(Color.Black.copy(alpha = 0.2f), Offset(x + width - 10f, y), Size(10f, height))
        
        // Cap
        val capY = if (isTop) y + height - 30f else y
        drawRect(baseColor, Offset(x - 5f, capY), Size(width + 10f, 30f))
    }

    private fun DrawScope.drawAvatarInternal(avatar: AvatarType, x: Float, y: Float, radius: Float) {
        val color = when(avatar) {
            AvatarType.PIP -> Color(0xFFFF80AB)
            AvatarType.EMBER -> Color(0xFFFF9800)
            AvatarType.BLIP -> Color(0xFF4DB6AC)
            else -> Color(0xFFEFB8C8)
        }
        drawCircle(color, radius, Offset(x, y))
        drawCircle(Color.White, radius * 0.4f, Offset(x + radius * 0.3f, y - radius * 0.2f))
        drawCircle(Color.Black, radius * 0.15f, Offset(x + radius * 0.4f, y - radius * 0.2f))
    }

    // Keep original for menu previews (drawn once)
    fun DrawScope.drawAvatar(avatar: AvatarType, x: Float, y: Float, radius: Float) {
        drawAvatarInternal(avatar, x, y, radius)
    }

    fun DrawScope.drawHeritageBackground(scene: SceneType, offset: Float = 0f, score: Int = 0) {
        // Fallback for menu
        drawSkyOnly(scene)
        drawLandmarksOnly(scene)
        drawGroundOnly(scene)
    }
}
