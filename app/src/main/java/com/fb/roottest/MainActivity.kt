package com.fb.roottest

import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.fb.roottest.base.IOnBackPressed
import com.fb.roottest.base.ViewModelFactory
import com.fb.roottest.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    var doubleBackToExitPressedOnce = false
    /** Put here destination IDs of the fragments for which UP-toolbar button need to be hidden */
    private val noUpButtonDestinations = setOf(
        R.id.splashFragment
    )
    private lateinit var appBarConfig: AppBarConfiguration
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val viewModel by lazy {
        ViewModelProviders.of(this, ViewModelFactory.getInstance(application)).get(MainActivityViewModel::class.java)
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = this.viewModel
        binding.setLifecycleOwner(this)
//        setupActionBar(binding.toolbar)
    }

//    private fun setupActionBar(toolbar: Toolbar) {
//        setSupportActionBar(toolbar)
//        appBarConfig = AppBarConfiguration(noUpButtonDestinations)
//        setupActionBarWithNavController(navController, appBarConfig)
//        observeCommand(viewModel.toolbarVisible) {
//            showToolbar(it)
//        }
//    }

//    private fun showToolbar(show: Boolean) {
//        binding.toolbar.visibility = if (show) View.VISIBLE else View.GONE
//    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 4000)
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.let { fragmentManager ->
                val fragment = fragmentManager.primaryNavigationFragment
                val parentFragment = navFragment.fragmentManager?.primaryNavigationFragment
                if (fragment != null) {
                    if (fragment is IOnBackPressed && !doubleBackToExitPressedOnce) {
                        this.doubleBackToExitPressedOnce = true
                        fragment.onBackFragmentPressed()
                    } else {
                        super.onBackPressed()
                    }
                } else {
                    if (parentFragment is NavHostFragment && !doubleBackToExitPressedOnce) {
                        this.doubleBackToExitPressedOnce = true
                        onBackFragmentPressed()
                    } else {
                        super.onBackPressed()
                    }
                }
            }
        }
    }

    fun showConnectionError() {
        val mySnackbar = Snackbar.make(
            binding.fragmentContainerFrameLayout,
            R.string.error_no_internet_connection, Snackbar.LENGTH_SHORT
        )
        mySnackbar.show()
    }

    fun onBackFragmentPressed() {
        val snackbar = Snackbar.make(
            binding.fragmentContainerFrameLayout,
            getString(R.string.main_back_press),
            Snackbar.LENGTH_LONG
        )
        snackbar.view.setBackgroundResource(R.drawable.shape_snack_bar)
        snackbar.show()
    }
}
