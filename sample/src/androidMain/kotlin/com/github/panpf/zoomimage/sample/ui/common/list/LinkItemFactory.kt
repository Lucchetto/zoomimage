/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.zoomimage.sample.ui.common.list

import android.content.Context
import com.github.panpf.zoomimage.sample.databinding.ListItemLinkBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingItemFactory
import com.github.panpf.zoomimage.sample.ui.model.Link

class LinkItemFactory : BaseBindingItemFactory<Link, ListItemLinkBinding>(Link::class) {

    override fun initItem(
        context: Context,
        binding: ListItemLinkBinding,
        item: BindingItem<Link, ListItemLinkBinding>
    ) {
    }

    override fun bindItemData(
        context: Context,
        binding: ListItemLinkBinding,
        item: BindingItem<Link, ListItemLinkBinding>,
        bindingAdapterPosition: Int,
        absoluteAdapterPosition: Int,
        data: Link
    ) {
        binding.linkTitleText.text = data.title
    }
}