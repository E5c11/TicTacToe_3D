package com.esc.test.apps.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.datastore.rxjava3.RxDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

const val GUEST_EMAIL = "guest@email.com"

data class UserDetails(val name: String = "guest", val surname: String = "guest",
                       val email: String = GUEST_EMAIL, val password: String = "password123",
                       val uid: String = "uid", val token: String = "token", val loggedIn: Boolean = false,
                       val didTutorial: Boolean = false)

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

    val userPreference = userDataJava.data().map { preferences  -> getData(preferences) }
        .doOnError {
        if (it is IOException) {
            it.printStackTrace()
            emptyPreferences()
        } else throw it
    }

    private fun getData(preferences: Preferences): UserDetails {
        val name = preferences[PreferenceKeys.USER_NAME] ?: "guest"
        val surname = preferences[PreferenceKeys.USER_SURNAME] ?: "guest"
        val email = preferences[PreferenceKeys.USER_EMAIL] ?: GUEST_EMAIL
        val password = preferences[PreferenceKeys.USER_PASSWORD] ?: "password123"
        val uid = preferences[PreferenceKeys.USER_UID] ?: "uid"
        val token = preferences[PreferenceKeys.USER_TOKEN] ?: "token"
        val loggedIn = preferences[PreferenceKeys.USER_LOGGED_IN] ?: false
        val didTutorial = preferences[PreferenceKeys.USER_DID_TUTORIAL] ?: false
        return UserDetails(name, surname, email, password, uid, token, loggedIn, didTutorial)
    }

    suspend fun updateUserData(userDetails: UserDetails) = userData.edit { preferences ->
        preferences[PreferenceKeys.USER_NAME] = userDetails.name
        preferences[PreferenceKeys.USER_SURNAME] = userDetails.surname
        preferences[PreferenceKeys.USER_EMAIL] = userDetails.email
        preferences[PreferenceKeys.USER_PASSWORD] = userDetails.password
        preferences[PreferenceKeys.USER_UID] = userDetails.uid
        preferences[PreferenceKeys.USER_TOKEN] = userDetails.token
        preferences[PreferenceKeys.USER_LOGGED_IN] = userDetails.loggedIn
        preferences[PreferenceKeys.USER_DID_TUTORIAL] = userDetails.didTutorial
    }

    suspend fun updateLoggedIn(userDetails: UserDetails) =
        userData.edit { it[PreferenceKeys.USER_LOGGED_IN] = userDetails.loggedIn }

    suspend fun updateTutorial(userDetails: UserDetails) =
        userData.edit { it[PreferenceKeys.USER_DID_TUTORIAL] = userDetails.didTutorial }

    suspend fun clearData() = userData.edit {
        val token = it[PreferenceKeys.USER_TOKEN]
        it.clear()
        it[PreferenceKeys.USER_TOKEN] = token!!
    }

    private object PreferenceKeys {
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PASSWORD = stringPreferencesKey("user_password")
        val USER_UID = stringPreferencesKey("user_uid")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_SURNAME = stringPreferencesKey("user_name")
        val USER_TOKEN = stringPreferencesKey("user_token")
        val USER_LOGGED_IN = booleanPreferencesKey("user_logged_in")
        val USER_DID_TUTORIAL = booleanPreferencesKey("user_did_tutorial")
    }
}