package com.nbsas.gapglide

import org.junit.Test
import org.junit.Assert.*
import java.lang.reflect.Method

class GameEngineTest {

    @Test
    fun testCircleRectCollisionRobustness() {
        // We use reflection to test the private circleRectCollision method
        val method: Method = GameEngine::class.java.getDeclaredMethod(
            "circleRectCollision",
            Float::class.javaPrimitiveType, // cx
            Float::class.javaPrimitiveType, // cy
            Float::class.javaPrimitiveType, // cr
            Float::class.javaPrimitiveType, // left
            Float::class.javaPrimitiveType, // top
            Float::class.javaPrimitiveType, // right
            Float::class.javaPrimitiveType  // bottom
        )
        method.isAccessible = true

        // 1. Valid range: Normal case
        // Circle at (50, 50) radius 10, Rect (0, 0, 100, 100) -> Collides
        val result1 = method.invoke(GameEngine, 50f, 50f, 10f, 0f, 0f, 100f, 100f) as Boolean
        assertTrue("Should collide in normal case", result1)

        // 2. Inverted range: left > right
        // Circle at (50, 50) radius 10, Rect (100, 0, 0, 100) -> Should handle correctly and collide
        val result2 = method.invoke(GameEngine, 50f, 50f, 10f, 100f, 0f, 0f, 100f) as Boolean
        assertTrue("Should collide with inverted X range", result2)

        // 3. Inverted range: top > bottom
        // Circle at (50, 50) radius 10, Rect (0, 100, 100, 0) -> Should handle correctly and collide
        val result3 = method.invoke(GameEngine, 50f, 50f, 10f, 0f, 100f, 100f, 0f) as Boolean
        assertTrue("Should collide with inverted Y range", result3)

        // 4. Empty range: left == right
        // Circle at (50, 50) radius 10, Line at X=50 from Y=0 to 100 -> Should collide
        val result4 = method.invoke(GameEngine, 50f, 50f, 10f, 50f, 0f, 50f, 100f) as Boolean
        assertTrue("Should collide with zero-width rect (line)", result4)
        
        // 5. No collision case
        val result5 = method.invoke(GameEngine, 200f, 200f, 10f, 0f, 0f, 100f, 100f) as Boolean
        assertFalse("Should not collide when far away", result5)
    }

    @Test
    fun testUpdateObstaclesSmallScreen() {
        // Test with a very small screen where minGapY > maxGapY
        val initialState = GameState(
            status = GameStatus.PLAYING,
            difficulty = Difficulty.EASY // gapHeight = 500f
        )
        
        // screenHeight = 400f. 
        // minGapY = 500/2 + 100 = 350f
        // maxGapY = 400 - 350 = 50f
        // This should not crash.
        val newState = GameEngine.updateObstacles(initialState, 0.1f, 1000f, 400f)
        
        assertNotNull(newState)
        assertTrue(newState.obstacles.isNotEmpty())
    }
}
