package com.esc.test.apps.domain.usecases.login

import com.esc.test.apps.common.utils.Resource
import com.esc.test.apps.common.utils.Utils.dispose
import com.esc.test.apps.data.persistence.GUEST_EMAIL
import com.esc.test.apps.data.persistence.UserDetails
import com.esc.test.apps.data.persistence.UserPreferences
import com.esc.test.apps.data.repositories.FbUserRepo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class LoginUsecase @Inject constructor(
    private val userPref: UserPreferences,
    private val fbUserRepo: FbUserRepo
) {

    private lateinit var d: Disposable

    fun invoke(): Observable<Resource<Boolean>> = Observable.create { emitter ->
        emitter.onNext(Resource.loading())
        d = userPref.userPreference.subscribeOn(Schedulers.io())
            .doOnNext { (_, email, password): UserDetails ->
                try {
                    if (email != GUEST_EMAIL) {
                        fbUserRepo.connectLogin(email, password)
                        emitter.onNext(Resource.success(true))
                    } else emitter.onNext(Resource.success(false))
                } catch (e: Exception) {
                    emitter.onNext(Resource.error(e))
                }
                dispose(d)
            }.subscribe()
    }


    fun invoke(email: String, password: String): Observable<Resource<Boolean>> = Observable.create { emitter ->
        emitter.onNext(Resource.loading())
        try {
            fbUserRepo.connectLogin(email, password)
            userPref.clearDataJava()
            emitter.onNext(Resource.success(true))
        } catch (e: Exception) {
            emitter.onNext(Resource.error(e))
        }
    }

}