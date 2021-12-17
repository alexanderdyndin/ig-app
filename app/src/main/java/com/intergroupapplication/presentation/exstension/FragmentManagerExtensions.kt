package com.intergroupapplication.presentation.exstension

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager

fun FragmentManager.setResult(code: String, vararg data: Pair<String, Any?>) {
    setFragmentResult(code, bundleOf(*data))
}
