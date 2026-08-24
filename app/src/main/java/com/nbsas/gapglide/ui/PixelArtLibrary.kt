package com.nbsas.gapglide.ui

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
            val size = (radius * 3.5f).toInt()
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap.asImageBitmap())
            val drawScope = CanvasDrawScope()
            drawScope.draw(density, LayoutDirection.Ltr, canvas, Size(size.toFloat(), size.toFloat())) {
                drawAvatarInternal(avatar, size.toFloat() / 2, size.toFloat() / 2, radius)
            }
            bitmap.asImageBitmap()
        }
    }

    fun getObstacleSprite(scene: SceneType, width: Float, height: Float, isTop: Boolean, density: Density): ImageBitmap {
        val key = "obs_${scene.name}_${width}_${height}_$isTop"
        return spriteCache.getOrPut(key) {
            val bitmap = Bitmap.createBitmap(width.toInt() + 40, height.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap.asImageBitmap())
            val drawScope = CanvasDrawScope()
            drawScope.draw(density, LayoutDirection.Ltr, canvas, Size(width + 40f, height)) {
                drawThemedObstacleInternal(scene, 20f, 0f, width, height, isTop)
            }
            bitmap.asImageBitmap()
        }
    }

    fun getBackgroundLayer(scene: SceneType, size: Size, density: Density, layer: Int, timeOfDay: Float = 0f): ImageBitmap {
        // Round timeOfDay to steps to avoid over-caching (e.g. 0.0, 0.1, 0.2...)
        val timeStep = (timeOfDay * 10).toInt() / 10f
        val key = "bg_${scene.name}_${size.width}_${size.height}_${layer}_$timeStep"
        return spriteCache.getOrPut(key) {
            val bitmap = Bitmap.createBitmap(size.width.toInt(), size.height.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap.asImageBitmap())
            val drawScope = CanvasDrawScope()
            drawScope.draw(density, LayoutDirection.Ltr, canvas, size) {
                when(layer) {
                    0 -> drawSkyDetailed(scene, timeOfDay)
                    1 -> drawLandmarksDetailed(scene)
                    2 -> drawGroundDetailed(scene)
                }
            }
            bitmap.asImageBitmap()
        }
    }

    private fun lerpColor(c1: Color, c2: Color, t: Float): Color {
        return Color(
            red = c1.red + (c2.red - c1.red) * t,
            green = c1.green + (c2.green - c1.green) * t,
            blue = c1.blue + (c2.blue - c1.blue) * t,
            alpha = c1.alpha + (c2.alpha - c1.alpha) * t
        )
    }

    private fun DrawScope.drawSkyDetailed(scene: SceneType, timeOfDay: Float) {
        val dayColor = when(scene) {
            SceneType.TAJ_MAHAL -> Color(0xFF2196F3) 
            SceneType.PYRAMIDS -> Color(0xFFFFA726)
            else -> Color(0xFF81D4FA)
        }
        val sunsetColor = Color(0xFFFF7043)
        val nightColor = Color(0xFF1A237E)

        val skyColor = if (timeOfDay < 0.5f) {
            lerpColor(dayColor, sunsetColor, timeOfDay * 2f)
        } else {
            lerpColor(sunsetColor, nightColor, (timeOfDay - 0.5f) * 2f)
        }
        
        drawRect(skyColor, Offset.Zero, size)
        
        // Sun/Moon Transition
        if (timeOfDay < 0.8f) {
            val sunY = 120f + timeOfDay * 200f
            drawCircle(Color(0xFFFFEB3B), 50f, Offset(size.width * 0.15f, sunY))
        } else {
            drawCircle(Color(0xFFFFF9C4), 40f, Offset(size.width * 0.85f, 100f))
        }

        drawPixelCloud(size.width * 0.25f, 140f, true)
        drawPixelCloud(size.width * 0.75f, 260f, false)
    }

    private fun DrawScope.drawPixelCloud(x: Float, y: Float, withFace: Boolean) {
        val cloudColor = Color.White
        val outlineColor = Color.Black.copy(alpha = 0.25f)
        drawRect(cloudColor, Offset(x, y), Size(110f, 45f))
        drawRect(cloudColor, Offset(x + 25f, y - 25f), Size(65f, 35f))
        drawRect(outlineColor, Offset(x, y), Size(110f, 45f), style = Stroke(width = 4f))
        drawRect(outlineColor, Offset(x + 25f, y - 25f), Size(65f, 35f), style = Stroke(width = 4f))
        if (withFace) {
            drawRect(Color.Black, Offset(x + 40f, y + 12f), Size(6f, 14f))
            drawRect(Color.Black, Offset(x + 65f, y + 12f), Size(6f, 14f))
        }
    }

    private fun DrawScope.drawLandmarksDetailed(scene: SceneType) {
        val baseY = size.height * 0.92f
        val cx = size.width / 2
        val outline = Color.Black.copy(alpha = 0.6f)
        
        when(scene) {
            SceneType.TAJ_MAHAL -> {
                val white = Color(0xFFFFFFFF)
                drawRect(white, Offset(cx - 130f, baseY - 70f), Size(260f, 70f))
                drawRect(outline, Offset(cx - 130f, baseY - 70f), Size(260f, 70f), style = Stroke(width = 5f))
                drawCircle(white, 80f, Offset(cx, baseY - 130f))
                drawCircle(outline, 80f, Offset(cx, baseY - 130f), style = Stroke(width = 5f))
                drawRect(white, Offset(cx - 80f, baseY - 130f), Size(160f, 65f))
                fun minaret(x: Float) {
                    drawRect(white, Offset(x - 12f, baseY - 160f), Size(24f, 160f))
                    drawRect(outline, Offset(x - 12f, baseY - 160f), Size(24f, 160f), style = Stroke(width = 5f))
                    drawCircle(Color(0xFFFFD700), 10f, Offset(x, baseY - 165f))
                }
                minaret(cx - 160f)
                minaret(cx + 160f)
            }
            SceneType.MIZORAM -> {
                cachedPath.reset()
                cachedPath.moveTo(0f, baseY)
                cachedPath.lineTo(size.width * 0.25f, baseY - 250f)
                cachedPath.lineTo(size.width * 0.5f, baseY - 400f)
                cachedPath.lineTo(size.width * 0.75f, baseY - 250f)
                cachedPath.lineTo(size.width, baseY)
                cachedPath.close()
                drawPath(cachedPath, Color(0xFF43A047))
                drawPath(cachedPath, outline, style = Stroke(width = 6f))
            }
            SceneType.GREAT_WALL -> {
                val wallColor = Color(0xFF9E9E9E)
                drawRect(wallColor, Offset(0f, baseY - 120f), Size(size.width, 120f))
                drawRect(outline, Offset(0f, baseY - 120f), Size(size.width, 120f), style = Stroke(width = 5f))
                for (x in -20..size.width.toInt() + 20 step 70) {
                    drawRect(Color(0xFF757575), Offset(x.toFloat(), baseY - 160f), Size(40f, 40f))
                    drawRect(outline, Offset(x.toFloat(), baseY - 160f), Size(40f, 40f), style = Stroke(width = 4f))
                }
            }
            SceneType.PYRAMIDS -> {
                val sand = Color(0xFFD4A373)
                fun drawP(x: Float, h: Float, w: Float) {
                    cachedPath.reset()
                    cachedPath.moveTo(x, baseY - h)
                    cachedPath.lineTo(x - w / 2, baseY)
                    cachedPath.lineTo(x + w / 2, baseY)
                    cachedPath.close()
                    drawPath(cachedPath, sand)
                    drawPath(cachedPath, outline, style = Stroke(width = 5f))
                }
                drawP(cx, 300f, 400f)
                drawP(cx - 200f, 150f, 250f)
                drawP(cx + 200f, 200f, 300f)
            }
            SceneType.EIFFEL_TOWER -> {
                val metal = Color(0xFF546E7A)
                cachedPath.reset()
                cachedPath.moveTo(cx, baseY - 500f)
                cachedPath.lineTo(cx - 150f, baseY)
                cachedPath.lineTo(cx + 150f, baseY)
                cachedPath.close()
                drawPath(cachedPath, metal)
                drawPath(cachedPath, outline, style = Stroke(width = 5f))
                drawRect(outline, Offset(cx - 60f, baseY - 150f), Size(120f, 8f))
                drawRect(outline, Offset(cx - 30f, baseY - 300f), Size(60f, 8f))
            }
            SceneType.COLOSSEUM -> {
                val stone = Color(0xFFD7CCC8)
                drawRect(stone, Offset(cx - 200f, baseY - 180f), Size(400f, 180f))
                drawRect(outline, Offset(cx - 200f, baseY - 180f), Size(400f, 180f), style = Stroke(width = 5f))
                for (i in -3..3) {
                    drawCircle(Color.Black.copy(alpha = 0.4f), 20f, Offset(cx + i * 50f, baseY - 50f))
                    drawCircle(Color.Black.copy(alpha = 0.4f), 20f, Offset(cx + i * 50f, baseY - 110f))
                }
            }
            SceneType.STATUE_OF_LIBERTY -> {
                val copper = Color(0xFF4DB6AC)
                drawRect(Color(0xFF455A64), Offset(cx - 60f, baseY - 80f), Size(120f, 80f))
                drawRect(outline, Offset(cx - 60f, baseY - 80f), Size(120f, 80f), style = Stroke(width = 4f))
                drawRect(copper, Offset(cx - 30f, baseY - 300f), Size(60f, 220f))
                drawRect(outline, Offset(cx - 30f, baseY - 300f), Size(60f, 220f), style = Stroke(width = 4f))
                drawCircle(copper, 30f, Offset(cx, baseY - 320f))
                drawCircle(outline, 30f, Offset(cx, baseY - 320f), style = Stroke(width = 4f))
            }
            SceneType.CHICHEN_ITZA -> {
                val stone = Color(0xFFB0BEC5)
                for (i in 0..4) {
                    val w = 350f - i * 70f
                    drawRect(stone, Offset(cx - w/2, baseY - (i+1)*40f), Size(w, 40f))
                    drawRect(outline, Offset(cx - w/2, baseY - (i+1)*40f), Size(w, 40f), style = Stroke(width = 4f))
                }
                drawRect(stone, Offset(cx - 30f, baseY - 240f), Size(60f, 40f))
                drawRect(outline, Offset(cx - 30f, baseY - 240f), Size(60f, 40f), style = Stroke(width = 4f))
            }
            SceneType.MACHU_PICCHU -> {
                val grass = Color(0xFF4CAF50)
                cachedPath.reset()
                cachedPath.moveTo(0f, baseY)
                cachedPath.lineTo(cx, baseY - 350f)
                cachedPath.lineTo(size.width, baseY)
                cachedPath.close()
                drawPath(cachedPath, grass)
                drawPath(cachedPath, outline, style = Stroke(width = 5f))
                for (i in -2..2) {
                    drawRect(Color.LightGray, Offset(cx + i * 80f - 20f, baseY - 60f), Size(40f, 30f))
                    drawRect(outline, Offset(cx + i * 80f - 20f, baseY - 60f), Size(40f, 30f), style = Stroke(width = 3f))
                }
            }
            SceneType.STONEHENGE -> {
                val gray = Color(0xFF78909C)
                fun pillar(x: Float) {
                    drawRect(gray, Offset(x - 20f, baseY - 120f), Size(40f, 120f))
                    drawRect(outline, Offset(x - 20f, baseY - 120f), Size(40f, 120f), style = Stroke(width = 4f))
                }
                pillar(cx - 100f)
                pillar(cx - 40f)
                drawRect(gray, Offset(cx - 110f, baseY - 150f), Size(90f, 30f))
                drawRect(outline, Offset(cx - 110f, baseY - 150f), Size(90f, 30f), style = Stroke(width = 4f))
                pillar(cx + 60f)
                pillar(cx + 120f)
                drawRect(gray, Offset(cx + 50f, baseY - 150f), Size(90f, 30f))
                drawRect(outline, Offset(cx + 50f, baseY - 150f), Size(90f, 30f), style = Stroke(width = 4f))
            }
        }
    }

    private fun DrawScope.drawGroundDetailed(scene: SceneType) {
        val groundY = size.height * 0.92f
        val groundColor = when(scene) {
            SceneType.MIZORAM -> Color(0xFF795548) 
            SceneType.PYRAMIDS -> Color(0xFFE9C46A)
            SceneType.STATUE_OF_LIBERTY -> Color(0xFF0288D1)
            else -> Color(0xFF5D4037)
        }
        drawRect(groundColor, Offset(0f, groundY), Size(size.width, size.height * 0.08f))
        val grassColor = when(scene) {
            SceneType.MIZORAM -> Color(0xFF4CAF50)
            SceneType.PYRAMIDS -> Color(0xFFE9C46A)
            SceneType.STATUE_OF_LIBERTY -> Color(0xFF03A9F4)
            SceneType.CHICHEN_ITZA -> Color(0xFF689F38)
            else -> Color(0xFF8BC34A)
        }
        drawRect(grassColor, Offset(0f, groundY), Size(size.width, 25f))
        drawRect(Color.Black.copy(alpha = 0.4f), Offset(0f, groundY), Size(size.width, 25f), style = Stroke(width = 3f))
        for (x in -20..size.width.toInt() + 20 step 90) {
            drawRect(Color.Black.copy(alpha = 0.15f), Offset(x.toFloat(), groundY + 25f), Size(80f, 45f), style = Stroke(width = 4f))
        }
    }

    private fun DrawScope.drawThemedObstacleInternal(scene: SceneType, x: Float, y: Float, width: Float, height: Float, isTop: Boolean) {
        val pipeColor = when(scene) {
            SceneType.MIZORAM -> Color(0xFF4CAF50) 
            SceneType.PYRAMIDS -> Color(0xFFFFD54F)
            SceneType.TAJ_MAHAL -> Color(0xFFF5F5F5)
            SceneType.STATUE_OF_LIBERTY -> Color(0xFFB2DFDB)
            else -> Color(0xFF8BC34A)
        }
        drawRect(pipeColor, Offset(x, y), Size(width, height))
        drawRect(Color.Black, Offset(x, y), Size(width, height), style = Stroke(width = 7f))
        drawRect(Color.White.copy(alpha = 0.5f), Offset(x + 12f, y), Size(18f, height))
        val capHeight = 55f
        val capY = if (isTop) y + height - capHeight else y
        drawRect(pipeColor, Offset(x - 12f, capY), Size(width + 24f, capHeight))
        drawRect(Color.Black, Offset(x - 12f, capY), Size(width + 24f, capHeight), style = Stroke(width = 7f))
        val lineY = if (isTop) capY + 12f else capY + capHeight - 12f
        drawRect(Color.Black.copy(alpha = 0.2f), Offset(x - 6f, lineY), Size(width + 12f, 5f))
    }

    private fun DrawScope.drawAvatarInternal(avatar: AvatarType, x: Float, y: Float, radius: Float) {
        val color = when(avatar) {
            AvatarType.PIP -> Color(0xFFFF4081) 
            AvatarType.EMBER -> Color(0xFFFF9800)
            AvatarType.BLIP -> Color(0xFF00BCD4)
            AvatarType.ZEPHYR -> Color(0xFFFFEB3B)
            else -> Color(0xFFEC407A)
        }
        drawCircle(Color.Black, radius + 3f, Offset(x, y))
        drawCircle(color, radius, Offset(x, y))
        drawCircle(Color.White, radius * 0.5f, Offset(x + radius * 0.35f, y - radius * 0.25f))
        drawCircle(Color.Black, radius * 0.25f, Offset(x + radius * 0.5f, y - radius * 0.25f))
        drawCircle(Color.White.copy(alpha = 0.4f), radius * 0.25f, Offset(x - radius * 0.45f, y - radius * 0.45f))
    }

    fun DrawScope.drawAvatar(avatar: AvatarType, x: Float, y: Float, radius: Float) {
        drawAvatarInternal(avatar, x, y, radius)
    }

    fun DrawScope.drawHeritageBackground(scene: SceneType, offset: Float = 0f, score: Int = 0) {
        drawSkyDetailed(scene, 0f)
        drawLandmarksDetailed(scene)
        drawGroundDetailed(scene)
    }
}
