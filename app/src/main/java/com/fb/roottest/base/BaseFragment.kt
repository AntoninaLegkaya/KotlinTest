package com.fb.roottest.base

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.fb.roottest.MainActivityViewModel
import com.fb.roottest.R
import com.fb.roottest.ToolbarHandler
import com.google.android.material.snackbar.Snackbar
import com.fb.roottest.util.obtainViewModel as extObtainViewModel

abstract class BaseFragment<B : ViewDataBinding> : Fragment() {

    protected lateinit var binding: B

    abstract val contentLayoutId: Int
        @LayoutRes get

    /** Override this value to show/hide toolbar */
    protected open val showToolbar = true

    /** Override this value to change toolbar title */
    protected open val title: String? = null

    /** Override this value to set options menu */
    @MenuRes
    protected open val optionsMenuRes: Int? = null

    private val viewModel by lazy {
        obtainViewModel(BaseFragmentViewModel::class.java).apply { initSetup(title, showToolbar) }
    }

    fun upadateTitle(title: String) {
        viewModel.updateTitle(title)
    }

    fun updateToolbarVisible(showToolbar: Boolean) {
        viewModel.updateToolbarVisibility(showToolbar)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, contentLayoutId, container, false)
        setupBinding(binding)
        binding.setLifecycleOwner(this)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        optionsMenuRes?.run {
            inflater.inflate(this, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupViews()
    }

    override fun onResume() {
        viewModel.onResume()
        hideKeyboard()
        super.onResume()
    }

    protected open fun setupBinding(binding: B) {}

    protected open fun setupViews() {}

    @Throws(IllegalStateException::class)
    protected fun obtainToolbarHandler(): ToolbarHandler = activity?.run {
        ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
    } ?: throw IllegalStateException("Tried to obtain MainActivity's view model while activity was NULL")

    protected fun setupSnackBar(@StringRes id: Int, view: CoordinatorLayout): Snackbar {
        val snackbar = Snackbar.make(view, getString(id), Snackbar.LENGTH_LONG)
        snackbar.view.setBackgroundResource(R.drawable.shape_snack_bar)
        return snackbar
    }

    fun hideKeyboard() {
        context?.let {
            val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val v = activity?.getCurrentFocus()
            v?.let {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                v.clearFocus()
            }
        }
    }

    protected fun <T : ViewModel> obtainViewModel(clazz: Class<T>): T = extObtainViewModel(clazz)
}