/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.sample.ui.test.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.githb.panpf.zoomimage.images.LocalImages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExifOrientationTestContentViewModel(private val application: Application) :
    AndroidViewModel(application) {

    private val _showContentState = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val showContentState: StateFlow<List<Pair<String, String>>> = _showContentState

    init {
        viewModelScope.launch {
            _showContentState.value = LocalImages.with(application).exifs.map { it.name to it.uri }
        }
    }
}