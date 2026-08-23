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
    private val selectedSceneKey = stringPreferencesKey("selected_scene")
    private val selectedAvatarKey = stringPreferencesKey("selected_avatar")

    private val settingsFlow by lazy {
        dataStore.data.map { preferences ->
            val highScore = preferences[highScoreKey] ?: 0
            val sceneName = preferences[selectedSceneKey] ?: SceneType.TAJ_MAHAL.name
            val avatarName = preferences[selectedAvatarKey] ?: AvatarType.NOVA.name
            
            Triple(
                highScore,
                try { SceneType.valueOf(sceneName) } catch (e: Exception) { SceneType.TAJ_MAHAL },
                try { AvatarType.valueOf(avatarName) } catch (e: Exception) { AvatarType.NOVA }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by settingsFlow.collectAsState(initial = Triple(0, SceneType.TAJ_MAHAL, AvatarType.NOVA))
            GapGlideTheme {
                GameScreen(
                    highScore = settings.first,
                    initialScene = settings.second,
                    initialAvatar = settings.third,
                    onNewHighScore = { newScore ->
                        saveHighScore(newScore)
                    },
                    onSceneChanged = { scene ->
                        saveSetting(selectedSceneKey, scene.name)
                    },
                    onAvatarChanged = { avatar ->
                        saveSetting(selectedAvatarKey, avatar.name)
                    }
                )
            }
        }
    }

    private fun saveHighScore(score: Int) {
        lifecycleScope.launch {
            dataStore.edit { settings ->
                val currentHigh = settings[highScoreKey] ?: 0
                if (score > currentHigh) {
                    settings[highScoreKey] = score
                }
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
