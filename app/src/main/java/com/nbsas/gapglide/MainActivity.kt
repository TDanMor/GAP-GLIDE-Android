package com.nbsas.gapglide

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.nbsas.gapglide.ui.GameScreen
import com.nbsas.gapglide.ui.theme.GapGlideTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {

    private val highScoreKey = intPreferencesKey("high_score")
    private val playerNameKey = stringPreferencesKey("player_name")
    private val selectedSceneKey = stringPreferencesKey("selected_scene")
    private val selectedAvatarKey = stringPreferencesKey("selected_avatar")
    private val vibrationEnabledKey = booleanPreferencesKey("vibration_enabled")
    private val soundEnabledKey = booleanPreferencesKey("sound_enabled")
    private val graceModeKey = booleanPreferencesKey("grace_mode")
    private val leaderboardKey = stringPreferencesKey("leaderboard")

    private val settingsFlow by lazy {
        dataStore.data.map { preferences ->
            val highScore = preferences[highScoreKey] ?: 0
            val playerName = preferences[playerNameKey] ?: "Hero"
            val sceneName = preferences[selectedSceneKey] ?: SceneType.TAJ_MAHAL.name
            val avatarName = preferences[selectedAvatarKey] ?: AvatarType.NOVA.name
            val vibrationEnabled = preferences[vibrationEnabledKey] ?: true
            val soundEnabled = preferences[soundEnabledKey] ?: true
            val graceMode = preferences[graceModeKey] ?: false
            val leaderboardStr = preferences[leaderboardKey] ?: ""
            
            val leaderboard = leaderboardStr.split("|")
                .filter { it.isNotEmpty() }
                .map { 
                    val parts = it.split(":")
                    ScoreEntry(parts[0], parts[1].toInt(), "") 
                }

            Settings(
                highScore,
                playerName,
                try { SceneType.valueOf(sceneName) } catch (e: Exception) { SceneType.TAJ_MAHAL },
                try { AvatarType.valueOf(avatarName) } catch (e: Exception) { AvatarType.NOVA },
                vibrationEnabled,
                soundEnabled,
                graceMode,
                leaderboard
            )
        }
    }

    data class Settings(
        val highScore: Int, 
        val playerName: String, 
        val scene: SceneType, 
        val avatar: AvatarType,
        val vibrationEnabled: Boolean,
        val soundEnabled: Boolean,
        val graceMode: Boolean,
        val leaderboard: List<ScoreEntry>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by settingsFlow.collectAsState(initial = Settings(0, "Hero", SceneType.TAJ_MAHAL, AvatarType.NOVA, true, true, false, emptyList()))
            GapGlideTheme {
                GameScreen(
                    highScore = settings.highScore,
                    initialName = settings.playerName,
                    initialScene = settings.scene,
                    initialAvatar = settings.avatar,
                    initialVibration = settings.vibrationEnabled,
                    initialSound = settings.soundEnabled,
                    initialGraceMode = settings.graceMode,
                    leaderboard = settings.leaderboard,
                    onNewHighScore = { newScore, name ->
                        saveHighScore(name, newScore)
                    },
                    onNameChanged = { name ->
                        saveSetting(playerNameKey, name)
                    },
                    onSceneChanged = { scene ->
                        saveSetting(selectedSceneKey, scene.name)
                    },
                    onAvatarChanged = { avatar ->
                        saveSetting(selectedAvatarKey, avatar.name)
                    },
                    onVibrationChanged = { enabled ->
                        saveSetting(vibrationEnabledKey, enabled)
                    },
                    onSoundChanged = { enabled ->
                        saveSetting(soundEnabledKey, enabled)
                    },
                    onGraceModeChanged = { enabled ->
                        saveSetting(graceModeKey, enabled)
                    }
                )
            }
        }
    }

    private fun saveHighScore(name: String, score: Int) {
        lifecycleScope.launch {
            dataStore.edit { settings ->
                // Global high score
                val currentHigh = settings[highScoreKey] ?: 0
                if (score > currentHigh) {
                    settings[highScoreKey] = score
                }

                // Leaderboard
                val currentBoardStr = settings[leaderboardKey] ?: ""
                val currentBoard = currentBoardStr.split("|")
                    .filter { it.isNotEmpty() }
                    .map { 
                        val parts = it.split(":")
                        ScoreEntry(parts[0], parts[1].toInt(), "") 
                    }.toMutableList()
                
                currentBoard.add(ScoreEntry(name, score, ""))
                val newBoard = currentBoard.sortedByDescending { it.score }.take(5)
                settings[leaderboardKey] = newBoard.joinToString("|") { "${it.name}:${it.score}" }
            }
        }
    }

    private fun <T> saveSetting(key: Preferences.Key<T>, value: T) {
        lifecycleScope.launch {
            dataStore.edit { settings ->
                settings[key] = value
            }
        }
    }
}
