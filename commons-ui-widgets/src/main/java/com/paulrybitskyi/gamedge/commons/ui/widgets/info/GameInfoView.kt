/*
 * Copyright 2020 Paul Rybitskyi, paul.rybitskyi.work@gmail.com
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

package com.paulrybitskyi.gamedge.commons.ui.widgets.info

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.paulrybitskyi.commons.ktx.*
import com.paulrybitskyi.commons.recyclerview.decorators.spacing.SpacingItemDecorator
import com.paulrybitskyi.commons.recyclerview.decorators.spacing.policies.LastItemExclusionPolicy
import com.paulrybitskyi.commons.recyclerview.utils.disableChangeAnimations
import com.paulrybitskyi.commons.utils.observeChanges
import com.paulrybitskyi.gamedge.commons.ui.widgets.R
import com.paulrybitskyi.gamedge.commons.ui.widgets.base.Item
import com.paulrybitskyi.gamedge.commons.ui.widgets.base.NoDependencies
import com.paulrybitskyi.gamedge.commons.ui.widgets.databinding.ViewGameInfoBinding
import com.paulrybitskyi.gamedge.commons.ui.widgets.info.items.*
import com.paulrybitskyi.gamedge.commons.ui.widgets.info.items.GameInfoDetailsItem
import com.paulrybitskyi.gamedge.commons.ui.widgets.info.items.GameInfoHeaderItem
import com.paulrybitskyi.gamedge.commons.ui.widgets.info.items.GameInfoRelatedGamesItem
import com.paulrybitskyi.gamedge.commons.ui.widgets.info.items.GameInfoScreenshotsItem
import com.paulrybitskyi.gamedge.commons.ui.widgets.info.items.GameInfoSummaryItem
import com.paulrybitskyi.gamedge.commons.ui.widgets.info.items.GameInfoVideosItem
import com.paulrybitskyi.gamedge.commons.ui.widgets.info.model.GameInfoCompanyModel
import com.paulrybitskyi.gamedge.commons.ui.widgets.info.model.GameInfoLinkModel
import com.paulrybitskyi.gamedge.commons.ui.widgets.info.model.GameInfoModel
import com.paulrybitskyi.gamedge.commons.ui.widgets.info.model.GameInfoVideoModel
import com.paulrybitskyi.gamedge.commons.ui.widgets.info.model.games.GameInfoRelatedGameModel
import com.paulrybitskyi.gamedge.commons.ui.widgets.utils.fadeIn
import com.paulrybitskyi.gamedge.commons.ui.widgets.utils.resetAnimation

class GameInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {


    private val binding = ViewGameInfoBinding.inflate(context.layoutInflater, this)

    private lateinit var infoAdapter: GameInfoAdapter

    private var adapterItems by observeChanges<List<Item<*, NoDependencies>>>(emptyList()) { _, newItems ->
        infoAdapter.submitList(newItems)
    }

    var uiState by observeChanges<GameInfoUiState>(GameInfoUiState.Empty) { _, newState ->
        handleUiStateChange(newState)
    }

    var onBackButtonClickListener: (() -> Unit)? = null
    var onLikeButtonClickListener: ((Int) -> Unit)? = null
    var onVideoClickListener: ((GameInfoVideoModel) -> Unit)? = null
    var onLinkClickListener: ((GameInfoLinkModel) -> Unit)? = null
    var onCompanyClickListener: ((GameInfoCompanyModel) -> Unit)? = null
    var onRelatedGameClickListener: ((GameInfoRelatedGameModel) -> Unit)? = null


    init {
        initRecyclerView(context)
        initInfoView()
        initDefaults()
    }


    private fun initRecyclerView(context: Context) = with(binding.recyclerView) {
        disableChangeAnimations()
        layoutManager = initLayoutManager(context)
        adapter = initAdapter(context)
        addItemDecoration(initItemDecorator())
    }


    private fun initLayoutManager(context: Context): LinearLayoutManager {
        return object : LinearLayoutManager(context) {

            override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
                return RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            }

        }
    }


    private fun initAdapter(context: Context): GameInfoAdapter {
        return GameInfoAdapter(context)
            .apply { listenerBinder = ::bindListener }
            .also { infoAdapter = it }
    }


    private fun bindListener(item: Item<*, NoDependencies>, viewHolder: RecyclerView.ViewHolder) {
        when(viewHolder) {

            is GameInfoHeaderItem.ViewHolder -> with(viewHolder) {
                setOnBackButtonClickListener { onBackButtonClickListener?.invoke() }
                setOnLikeButtonClickListener {
                    val gameId = (uiState as? GameInfoUiState.Result)?.model?.id
                        ?: return@setOnLikeButtonClickListener

                    onLikeButtonClickListener?.invoke(gameId)
                }
            }

            is GameInfoVideosItem.ViewHolder -> with(viewHolder) {
                setOnVideoClickListener { onVideoClickListener?.invoke(it) }
            }

            is GameInfoLinksItem.ViewHolder -> with(viewHolder) {
                setOnLinkClickListener { onLinkClickListener?.invoke(it) }
            }

            is GameInfoCompaniesItem.ViewHolder -> with(viewHolder) {
                setOnCompanyClickListener { onCompanyClickListener?.invoke(it) }
            }

            is GameInfoRelatedGamesItem.ViewHolder -> with(viewHolder) {
                setOnGameClickListener { onRelatedGameClickListener?.invoke(it) }
            }

        }
    }


    private fun initItemDecorator(): SpacingItemDecorator {
        return SpacingItemDecorator(
            spacing = getDimensionPixelSize(R.dimen.game_info_decorator_spacing),
            sideFlags = SpacingItemDecorator.SIDE_BOTTOM,
            itemExclusionPolicy = LastItemExclusionPolicy()
        )
    }


    private fun initInfoView() = with(binding.infoView) {
        isDescriptionTextVisible = false
    }


    private fun initDefaults() {
        uiState = uiState
    }


    private fun handleUiStateChange(newState: GameInfoUiState) {
        when(newState) {
            is GameInfoUiState.Empty -> onEmptyStateSelected()
            is GameInfoUiState.Loading -> onLoadingStateSelected()
            is GameInfoUiState.Result -> onResultStateSelected(newState)
        }
    }


    private fun onEmptyStateSelected() {
        showInfoView()
        hideProgressBar()
        hideRecyclerView()
    }


    private fun onLoadingStateSelected() {
        showProgressBar()
        hideInfoView()
        hideRecyclerView()
    }


    private fun onResultStateSelected(uiState: GameInfoUiState.Result) {
        adapterItems = uiState.model.toAdapterItems()

        showRecyclerView()
        hideInfoView()
        hideProgressBar()
    }


    private fun showInfoView() = with(binding.infoView) {
        if(isVisible) return

        makeVisible()
        fadeIn()
    }


    private fun hideInfoView() = with(binding.infoView) {
        makeGone()
        resetAnimation()
    }


    private fun showProgressBar() = with(binding.progressBar) {
        makeVisible()
    }


    private fun hideProgressBar() = with(binding.progressBar) {
        makeGone()
    }


    private fun showRecyclerView() = with(binding.recyclerView) {
        if(isVisible) return

        makeVisible()
        fadeIn()
    }


    private fun hideRecyclerView() = with(binding.recyclerView) {
        makeInvisible()
        resetAnimation()
    }


    private fun GameInfoModel.toAdapterItems(): List<Item<*, NoDependencies>> {
        return buildList {
            add(GameInfoHeaderItem(headerModel))

            if(hasVideoModels) add(GameInfoVideosItem(videoModels))
            if(hasScreenshotUrls) add(GameInfoScreenshotsItem(screenshotUrls))
            if(hasSummary) add(GameInfoSummaryItem(checkNotNull(summary)))
            if(hasDetailsModel) add(GameInfoDetailsItem(checkNotNull(detailsModel)))
            if(hasLinkModels) add(GameInfoLinksItem(linkModels))
            if(hasCompanyModels) add(GameInfoCompaniesItem(companyModels))
            if(hasOtherCompanyGames) add(GameInfoRelatedGamesItem(checkNotNull(otherCompanyGames)))
            if(hasSimilarGames) add(GameInfoRelatedGamesItem(checkNotNull(similarGames)))
        }
    }


}