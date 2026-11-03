/*
 * Copyright (C) 2026 RisingOS (revived) Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.systemui.qs.panels.data.repository

import android.content.SharedPreferences
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.qs.pipeline.shared.TileSpec
import com.android.systemui.settings.UserFileManager
import com.android.systemui.user.data.repository.UserRepository
import com.android.systemui.util.kotlin.SharedPreferencesExt.observe
import com.android.systemui.util.kotlin.emitOnStart
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
@SysUISingleton
class QqsTilesRepository
@Inject
constructor(
    private val userFileManager: UserFileManager,
    private val userRepository: UserRepository,
    @Background private val backgroundDispatcher: CoroutineDispatcher,
) {

    companion object {
        private const val QQS_TILES_KEY = "qqs_tiles"
        private const val FILE_NAME = "qs_qqs_prefs"

        private val DEFAULT_TILES =
            listOf("internet", "bt", "dnd", "flashlight", "rotation").map { TileSpec.create(it) }
    }

    val qqsTiles: Flow<List<TileSpec>> =
        userRepository.selectedUserInfo
            .flatMapLatest { userInfo ->
                val prefs = getSharedPrefs(userInfo.id)
                prefs.observe().emitOnStart().map { prefs.getQqsTiles() }
            }
            .flowOn(backgroundDispatcher)

    fun setQqsTiles(specs: List<TileSpec>) {
        val prefs = getSharedPrefs(userRepository.getSelectedUserInfo().id)
        prefs.edit().putString(QQS_TILES_KEY, specs.joinToString(",") { it.spec }).apply()
    }

    fun addTile(spec: TileSpec) {
        val current = getSharedPrefs(userRepository.getSelectedUserInfo().id).getQqsTiles()
        if (current.any { it == spec }) return
        setQqsTiles(current + spec)
    }

    fun removeTile(spec: TileSpec) {
        val current = getSharedPrefs(userRepository.getSelectedUserInfo().id).getQqsTiles()
        if (current.none { it == spec }) return
        setQqsTiles(current.filter { it != spec })
    }

    private fun SharedPreferences.getQqsTiles(): List<TileSpec> {
        val saved = getString(QQS_TILES_KEY, null) ?: return DEFAULT_TILES
        return saved
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { TileSpec.create(it) }
    }

    private fun getSharedPrefs(userId: Int): SharedPreferences =
        userFileManager.getSharedPreferences(FILE_NAME, android.content.Context.MODE_PRIVATE, userId)
}
