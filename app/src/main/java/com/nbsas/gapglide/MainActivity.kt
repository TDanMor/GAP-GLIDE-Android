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
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.nbsas.gapglide.ui.GameScreen
import com.nbsas.gapglide.ui.theme.GapGlideTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {

    private val highScoreKey = intPreferencesKey("high_score")
    private val highScoreFlow by lazy {
        dataStore.data
            .map { preferences ->
                preferences[highScoreKey] ?: 0
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val highScore by highScoreFlow.collectAsState(initial = 0)
            GapGlideTheme {
                GameScreen(
                    highScore = highScore,
                    onNewHighScore = { newScore ->
                        saveHighScore(newScore)
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
}
