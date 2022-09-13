package com.esc.test.apps.data.repositories

import androidx.lifecycle.MutableLiveData
import com.esc.test.apps.common.utils.SingleLiveEvent

interface FbUserRepo {

    fun isEmailValid(viewEmail: String)

    fun connectLogin(email: String, password: String)

    fun createUser(email: String, password: String, displayName: String)

    fun deleteAccount()

    fun updateDisplayName(displayName: String)

    fun checkDisplayNameExist(ds: CharSequence)

    fun updateEmail(email: String)

    fun updatePassword(password: String)

    fun getLoggedIn(): SingleLiveEvent<Boolean>

    fun getEmailError(): MutableLiveData<String>

    fun getDisplayNameExists(): MutableLiveData<String>

    fun getError(): SingleLiveEvent<String>

    fun setToken(s: String)
}