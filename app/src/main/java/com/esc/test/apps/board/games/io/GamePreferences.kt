package com.esc.test.apps.board.games.io

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.datastore.rxjava3.RxDataStore
import com.esc.test.apps.R
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class GameDetails(
    val winner: String = "", val winnerLine: Set<String> = emptySet(),
    val starter: String = "cross", val circleScore: String = "0",
    val crossScore: String = "0", val id: String = "deleted",
    val setId: String = ""
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_details")

@Singleton
class GamePreferences @Inject constructor(@ApplicationContext val context: Context){

    private val dataStore: RxDataStore<Preferences> = RxPreferenceDataStoreBuilder(context, "game_details").build()

    private val gameData: DataStore<Preferences> = context.dataStore
    private val gameDataJava: RxDataStore<Preferences> = dataStore

    val gamePref = gameData.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else throw it
        }.map { preferences -> getData(preferences) }

    val gamePreference = gameDataJava.data().map { preferences -> getData(preferences) }
        .doOnError {
        if (it is IOException) {
            it.printStackTrace()
            emptyPreferences()
        } else throw it
    }

    private fun getData(preferences: Preferences): GameDetails {
        val winner = preferences[PreferenceKeys.GAME_WINNER] ?: ""
        val winnerLine = preferences[PreferenceKeys.GAME_WINNER_LINE] ?: emptySet()
        val starter = preferences[PreferenceKeys.GAME_STARTER] ?: ""
        val circleScore = preferences[PreferenceKeys.GAME_CIRCLE_SCORE] ?: "0"
        val crossScore = preferences[PreferenceKeys.GAME_CROSS_SCORE] ?: "0"
        val gameId = preferences[PreferenceKeys.GAME_ID] ?: "deleted"
        val gameSetId = preferences[PreferenceKeys.GAME_SET_ID] ?: ""
        return GameDetails(winner, winnerLine, starter, circleScore, crossScore, gameId, gameSetId)
    }

    fun clearWinnerLineJava() = gameDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs[PreferenceKeys.GAME_WINNER_LINE] = emptySet()
        prefs[PreferenceKeys.GAME_WINNER] = ""
        Single.just(prefs)
    }

    fun newOnlineGameJava() = gameDataJava.updateDataAsync {
        val prefs = it.toMutablePreferences()
        prefs[PreferenceKeys.GAME_WINNER] = ""
        prefs[PreferenceKeys.GAME_WINNER_LINE] = emptySet()
        val gameId = prefs[PreferenceKeys.GAME_ID]!!
        val gameSetId = prefs[PreferenceKeys.GAME_SET_ID]!!
        emptyPreferences()
        prefs[PreferenceKeys.GAME_ID] = gameId
        prefs[PreferenceKeys.GAME_SET_ID] = gameSetId
        Single.just(prefs)
    }

    fun newLocalGameJava() = gameDataJava.updateDataAsync {
        val prefs = it.toMutablePreferences()
        prefs[PreferenceKeys.GAME_WINNER] = ""
        prefs[PreferenceKeys.GAME_WINNER_LINE] = emptySet()
        val starter =
            if (prefs[PreferenceKeys.GAME_STARTER].equals(context.getString(R.string.circle))) context.getString(R.string.cross)
            else context.getString(R.string.circle)
        val circle = prefs[PreferenceKeys.GAME_CIRCLE_SCORE]!!
        val cross = prefs[PreferenceKeys.GAME_CROSS_SCORE]!!

        emptyPreferences()
        prefs[PreferenceKeys.GAME_CIRCLE_SCORE] = circle
        prefs[PreferenceKeys.GAME_CROSS_SCORE] = cross
        prefs[PreferenceKeys.GAME_STARTER] = starter
        Single.just(prefs)
    }

    fun newSetJava() = gameDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs.clear()
        Single.just(prefs)
    }

    fun updateWinnerJava(winner: String) = gameDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs[PreferenceKeys.GAME_WINNER] = winner
        Single.just(prefs)
    }

    fun updateWinnerLineJava(winnerLine: List<String>) = gameDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs[PreferenceKeys.GAME_WINNER_LINE] = HashSet(winnerLine)
        Single.just(prefs)
    }

    fun updateStarterJava(starter: String) = gameDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs[PreferenceKeys.GAME_STARTER] = starter
        Single.just(prefs)
    }

    fun updateCircleScoreJava(circleScore: String) = gameDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs[PreferenceKeys.GAME_CIRCLE_SCORE] = circleScore
        Single.just(prefs)
    }

    fun updateCrossScoreJava(crossScore: String) = gameDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs[PreferenceKeys.GAME_CROSS_SCORE] = crossScore
        Single.just(prefs)
    }

    fun updateGameIdJava(gameId: String) = gameDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs[PreferenceKeys.GAME_ID] = gameId
        Single.just(prefs)
    }

    fun updateGameSetIdJava(gameSetId: String) = gameDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs[PreferenceKeys.GAME_SET_ID] = gameSetId
        Single.just(prefs)
    }

    private object PreferenceKeys {
        val GAME_WINNER = stringPreferencesKey("game_winner")
        val GAME_WINNER_LINE = stringSetPreferencesKey("game_winner_line")
        val GAME_STARTER = stringPreferencesKey("game_starter")
        val GAME_CIRCLE_SCORE = stringPreferencesKey("game_circle_score")
        val GAME_CROSS_SCORE = stringPreferencesKey("game_cross_score")
        val GAME_ID = stringPreferencesKey("game_id")
        val GAME_SET_ID = stringPreferencesKey("game_set_id")
    }

}