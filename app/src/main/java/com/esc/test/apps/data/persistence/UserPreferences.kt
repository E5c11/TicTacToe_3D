package com.esc.test.apps.data.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.datastore.rxjava3.RxDataStore
import com.esc.test.apps.common.helpers.move.BotMoveGenerator.NORMAL
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


const val GUEST_EMAIL = "guest@email.com"

data class UserDetails(val name: String = "guest", val email: String = GUEST_EMAIL,
                       val password: String = "password123", val uid: String = "uid",
                       val token: String = "token", val loggedIn: Boolean = false,
                       val didTutorial: Boolean = false, val level: String = NORMAL
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_details")

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext context: Context){

    private val dataStore: RxDataStore<Preferences> = RxPreferenceDataStoreBuilder(context, "user_details").build()

    private val userData: DataStore<Preferences> = context.dataStore
    private val userDataJava: RxDataStore<Preferences> = dataStore

    val userPref = userData.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else throw it
        }.map { preferences -> getData(preferences) }

    val userPreference = userDataJava.data().map { preferences -> getData(preferences) }
        .doOnError {
        if (it is IOException) {
            it.printStackTrace()
            emptyPreferences()
        } else throw it
    }

    private fun getData(preferences: Preferences): UserDetails {
        val name = preferences[PreferenceKeys.USER_NAME] ?: "guest"
        val email = preferences[PreferenceKeys.USER_EMAIL] ?: GUEST_EMAIL
        val password = preferences[PreferenceKeys.USER_PASSWORD] ?: "password123"
        val uid = preferences[PreferenceKeys.USER_UID] ?: "uid"
        val token = preferences[PreferenceKeys.USER_TOKEN] ?: "token"
        val loggedIn = preferences[PreferenceKeys.USER_LOGGED_IN] ?: false
        val didTutorial = preferences[PreferenceKeys.USER_DID_TUTORIAL] ?: false
        return UserDetails(name, email, password, uid, token, loggedIn, didTutorial)
    }

    suspend fun updatePassword(password: String) =
        userData.edit { it[PreferenceKeys.USER_PASSWORD] = password }

    fun updatePasswordJava(password: String) = userDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs[PreferenceKeys.USER_EMAIL] = password
        Single.just(prefs)
    }

    suspend fun updateEmail(email: String) =
        userData.edit { it[PreferenceKeys.USER_PASSWORD] = email }

    fun updateEmailJava(email: String) = userDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs[PreferenceKeys.USER_EMAIL] = email
        Single.just(prefs)
    }

    suspend fun updateDisplayName(displayName: String) =
        userData.edit { it[PreferenceKeys.USER_NAME] = displayName }

    fun updateNameJava(displayName: String) = userDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs[PreferenceKeys.USER_NAME] = displayName
        Single.just(prefs)
    }

    suspend fun updateLoggedIn(loggedIn: Boolean) =
        userData.edit { it[PreferenceKeys.USER_LOGGED_IN] = loggedIn }

    suspend fun updateTutorial(didTutorial: Boolean) =
        userData.edit { it[PreferenceKeys.USER_DID_TUTORIAL] = didTutorial }

    fun updateTutorialJava(didTutorial: Boolean) = userDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs[PreferenceKeys.USER_DID_TUTORIAL] = didTutorial
        Single.just(prefs)
    }

    suspend fun updateToken(token: String) =
        userData.edit { it[PreferenceKeys.USER_TOKEN] = token }

    fun updateTokenJava(token: String) = userDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs[PreferenceKeys.USER_TOKEN] = token
        Single.just(prefs)
    }

    suspend fun updateLevel(level: String) =
        userData.edit { it[PreferenceKeys.USER_LEVEL] = level }

    fun updateLevelJava(level: String) = userDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        prefs[PreferenceKeys.USER_LEVEL] = level
        Single.just(prefs)
    }

    suspend fun updateNewUser(uid: String, email: String, password: String) =
        userData.edit { preferences ->
//            preferences[PreferenceKeys.USER_NAME] = displayName
            preferences[PreferenceKeys.USER_EMAIL] = email
            preferences[PreferenceKeys.USER_PASSWORD] = password
            preferences[PreferenceKeys.USER_UID] = uid
            preferences[PreferenceKeys.USER_LOGGED_IN] = true
        }

    fun updateUserJava(uid: String, email: String, password: String) =
        userDataJava.updateDataAsync{ prefs ->
            val preferences: MutablePreferences = prefs.toMutablePreferences()
//            preferences[PreferenceKeys.USER_NAME] = displayName
            preferences[PreferenceKeys.USER_EMAIL] = email
            preferences[PreferenceKeys.USER_PASSWORD] = password
            preferences[PreferenceKeys.USER_UID] = uid
            preferences[PreferenceKeys.USER_LOGGED_IN] = true
            Single.just(preferences)
        }

    suspend fun clearData() = userData.edit {
        val token = it[PreferenceKeys.USER_TOKEN]
        it.clear()
        it[PreferenceKeys.USER_TOKEN] = token ?: ""
    }

    fun clearDataJava() = userDataJava.updateDataAsync {
        val prefs: MutablePreferences = it.toMutablePreferences()
        val token = prefs[PreferenceKeys.USER_TOKEN]
        prefs.clear()
        prefs[PreferenceKeys.USER_TOKEN] = token ?: ""
        Single.just(prefs)
    }

    private object PreferenceKeys {
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PASSWORD = stringPreferencesKey("user_password")
        val USER_UID = stringPreferencesKey("user_uid")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_TOKEN = stringPreferencesKey("user_token")
        val USER_LOGGED_IN = booleanPreferencesKey("user_logged_in")
        val USER_DID_TUTORIAL = booleanPreferencesKey("user_did_tutorial")
        val USER_LEVEL = stringPreferencesKey("user_level")
    }
}