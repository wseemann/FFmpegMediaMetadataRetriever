/*
 * FFmpegMediaMetadataRetriever: A unified interface for retrieving frame
 * and meta data from an input media file.
 *
 * Copyright 2025 William Seemann
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

package wseemann.media.demo.viewmodel

import android.view.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import wseemann.media.demo.model.MetadataModel
import wseemann.media.demo.model.UiState
import wseemann.media.demo.usecase.GetFrameAtTimeUseCase
import wseemann.media.demo.usecase.RetrieveMetadataUseCase

class MainViewModel(
    private val retrieveMetadataUseCase: RetrieveMetadataUseCase,
    private val getFrameAtTimeUseCase: GetFrameAtTimeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Success(MetadataModel()))
    val uiState = _uiState.asStateFlow()

    fun retrieveMetadata(uri: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val metadataModel = retrieveMetadataUseCase(uri).getOrElse {
                _uiState.value = UiState.Error(it)
                return@launch
            }

            _uiState.value = UiState.Success(metadataModel)
        }
    }

    fun retrieveFrame(uri: String, timeUs: Long, surface: Surface) {
        viewModelScope.launch {
            getFrameAtTimeUseCase(
                uri = uri,
                timeUs = timeUs,
                surface = surface
            )
        }
    }
}