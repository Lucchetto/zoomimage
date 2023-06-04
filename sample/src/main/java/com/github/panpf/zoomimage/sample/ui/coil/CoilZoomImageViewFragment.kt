/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.zoomimage.sample.ui.coil

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.sample.BuildConfig
import com.github.panpf.zoomimage.sample.databinding.CoilZoomImageViewFragmentBinding
import com.github.panpf.zoomimage.sample.ui.base.BindingFragment
import com.github.panpf.zoomimage.sample.ui.zoomimage.ImageInfoDialogFragment
import com.github.panpf.zoomimage.sample.ui.zoomimage.SettingsEventViewModel
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilUri

class CoilZoomImageViewFragment : BindingFragment<CoilZoomImageViewFragmentBinding>() {

    private val args by navArgs<CoilZoomImageViewFragmentArgs>()
    private val settingsEventViewModel by viewModels<SettingsEventViewModel>()

    override fun onViewCreated(
        binding: CoilZoomImageViewFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        binding.coilZoomImageViewImage.apply {
            zoomAbility.logger.level = if (BuildConfig.DEBUG)
                Logger.Level.DEBUG else Logger.Level.INFO

            settingsEventViewModel.observeZoomSettings(this)

            setOnLongClickListener {
                findNavController().navigate(
                    ImageInfoDialogFragment.createDirectionsFromImageView(this, null)
                )
                true
            }

            load(sketchUri2CoilUri(args.imageUri)) {
                lifecycle(viewLifecycleOwner.lifecycle)
            }
        }

        binding.coilZoomImageViewTileMap.apply {
            setZoomImageView(binding.coilZoomImageViewImage)
            displayImage(args.imageUri) {
                resizeSize(600, 600)
                resizePrecision(Precision.LESS_PIXELS)
            }
        }

        binding.coilZoomImageViewRotate.setOnClickListener {
            binding.coilZoomImageViewImage.zoomAbility.rotateBy(90)
        }

        binding.coilZoomImageViewSettings.setOnClickListener {
//            findNavController().navigate(
//                MainFragmentDirections.actionGlobalSettingsDialogFragment(Page.ZOOM.name)
//            )
        }
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = CoilZoomImageViewFragment().apply {
            arguments = CoilZoomImageViewFragmentArgs(data).toBundle()
        }
    }
}