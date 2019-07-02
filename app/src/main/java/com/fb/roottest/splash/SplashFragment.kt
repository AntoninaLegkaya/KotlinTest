package com.fb.roottest.splash

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.fb.roottest.R
import com.fb.roottest.base.BaseFragment
import com.fb.roottest.databinding.FragmentSplashBinding
import com.fb.roottest.util.observeCommand

class SplashFragment : BaseFragment<FragmentSplashBinding>() {

    override val title: String?
        get() = ""

    override val showToolbar: Boolean
        get() = false
    override val contentLayoutId: Int
        get() = R.layout.fragment_splash

    override fun setupBinding(binding: FragmentSplashBinding) {
        (binding.ivSky.background as AnimationDrawable).start()
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = obtainViewModel(SplashViewModel::class.java)
        viewModel.init()
        viewModel.apply {
            observeCommand(navigateHomeFragment) {
                findNavController().navigate(SplashFragmentDirections.moveToHomeFragment())
            }

        }
    }
}
