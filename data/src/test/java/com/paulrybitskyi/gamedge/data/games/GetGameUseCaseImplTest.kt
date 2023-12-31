/*
 * Copyright 2021 Paul Rybitskyi, paul.rybitskyi.work@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paulrybitskyi.gamedge.data.games

import app.cash.turbine.test
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import com.paulrybitskyi.gamedge.data.commons.DataPagination
import com.paulrybitskyi.gamedge.data.games.datastores.GamesLocalDataStore
import com.paulrybitskyi.gamedge.data.games.usecases.GetGameUseCaseImpl
import com.paulrybitskyi.gamedge.data.games.usecases.commons.GameMapper
import com.paulrybitskyi.gamedge.domain.commons.entities.Error
import com.paulrybitskyi.gamedge.domain.games.usecases.GetGameUseCase
import com.paulrybitskyi.gamedge.commons.testing.DATA_GAME
import com.paulrybitskyi.gamedge.commons.testing.FakeDispatcherProvider
import com.paulrybitskyi.gamedge.commons.testing.GET_GAME_USE_CASE_PARAMS
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.*
import org.junit.Before
import org.junit.Test

internal class GetGameUseCaseImplTest {


    @MockK private lateinit var gamesLocalDataStore: GamesLocalDataStore

    private lateinit var gameMapper: GameMapper
    private lateinit var SUT: GetGameUseCaseImpl


    @Before
    fun setup() {
        MockKAnnotations.init(this)

        gameMapper = GameMapper()
        SUT = GetGameUseCaseImpl(
            gamesLocalDataStore = gamesLocalDataStore,
            dispatcherProvider = FakeDispatcherProvider(),
            gameMapper = gameMapper
        )
    }


    @Test
    fun `Emits game successfully`() {
        runBlockingTest {
            coEvery { gamesLocalDataStore.getGame(any()) } returns DATA_GAME

            SUT.execute(GET_GAME_USE_CASE_PARAMS).test {
                assertThat(expectItem().get()).isEqualTo(gameMapper.mapToDomainGame(DATA_GAME))
                expectComplete()
            }
        }
    }


    @Test
    fun `Emits not found error if game ID does not reference existing game`() {
        runBlockingTest {
            coEvery { gamesLocalDataStore.getGame(any()) } returns null

            SUT.execute(GET_GAME_USE_CASE_PARAMS).test {
                assertThat(expectItem().getError() is Error.NotFound).isTrue
                expectComplete()
            }
        }
    }


}