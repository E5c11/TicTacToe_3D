package com.esc.test.apps.common

import com.esc.test.apps.common.utils.Resource
import kotlinx.coroutines.flow.Flow

interface Repository {
    /**
     * These functions are the source of truth, they read and write to the local database
     * and update the remote database accordingly.
     * @param data is the generic object that will [insert]ed, [delete]d or [update]ed
     * likewise it is the object that will be returned by [fetch] and [fetchAll]
     */
    fun <T> insert(vararg data: T): Flow<Resource<Long>>
    fun <T> delete(data: T, vararg conditions: T): Flow<Resource<Long>>
    fun <T> update(data: T? = null, vararg conditions: T): Flow<Resource<Long>>
    fun <T> fetch(data: T? = null, vararg conditions: T): Flow<Resource<T>>
    fun <T> fetchAll(): Flow<Resource<List<T>>>

    /**
     * These function are associated to the remote source
     * They should not return the object associated to the repo,
     * Since it should be observed from the local source.
     * Once [fetchRemote] and [fetchAllRemote] has returned a value
     * It should update the local source
     */
    fun <T> fetchRemote(data: T): Flow<Resource<Long>>
    fun <T> fetchAllRemote(): Flow<Resource<String>>
}