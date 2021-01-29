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

package com.paulrybitskyi.gamedge.ui.category.mapping

import com.paulrybitskyi.gamedge.commons.ui.widgets.category.GameCategoryModel
import com.paulrybitskyi.gamedge.core.IgdbImageSize
import com.paulrybitskyi.gamedge.core.IgdbImageUrlBuilder
import com.paulrybitskyi.gamedge.domain.games.entities.Game
import com.paulrybitskyi.hiltbinder.BindType
import javax.inject.Inject


internal interface GameCategoryModelMapper {

    fun mapToGameCategoryModel(game: Game): GameCategoryModel

}


@BindType(installIn = BindType.Component.ACTIVITY_RETAINED)
internal class GameCategoryModelMapperImpl @Inject constructor(
    private val igdbImageUrlBuilder: IgdbImageUrlBuilder,
) : GameCategoryModelMapper {


    override fun mapToGameCategoryModel(game: Game): GameCategoryModel {
        return GameCategoryModel(
            id = game.id,
            title = game.name,
            coverUrl = game.cover?.let { cover ->
                igdbImageUrlBuilder.buildUrl(cover, IgdbImageUrlBuilder.Config(IgdbImageSize.BIG_COVER))
            }
        )
    }


}