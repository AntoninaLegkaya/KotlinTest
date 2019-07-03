package com.fb.roottest.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import com.camerakit.CameraKitView
import com.fb.roottest.MainActivity
import com.fb.roottest.R
import com.fb.roottest.base.BaseFragment
import com.fb.roottest.base.paralax.BottomSheetBehaviorGoogleMapsLike
import com.fb.roottest.base.paralax.ItemPagerAdapter
import com.fb.roottest.base.paralax.MergedAppBarBehavior
import com.fb.roottest.custom.CarouselAdapter
import com.fb.roottest.data.db.Purchase
import com.fb.roottest.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage


class HomeFragment : BaseFragment<FragmentHomeBinding>(), PurchaseClickListener, CameraKitView.ImageCallback {

    //    private var viewModel: HomeViewModel
    lateinit var behavior: BottomSheetBehaviorGoogleMapsLike<NestedScrollView>



    override val contentLayoutId: Int
        get() = R.layout.fragment_home

    var mDrawables = intArrayOf(
        R.drawable.cheese_3,
        R.drawable.cheese_3,
        R.drawable.cheese_3,
        R.drawable.cheese_3,
        R.drawable.cheese_3,
        R.drawable.cheese_3
    )
    private val callback = BottomSheetCallback()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setupBinding(binding: FragmentHomeBinding) {
        val toolBar = binding.toolbar
        val viewModel = obtainViewModel(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.listener = this
        binding.inputSheet.listener = this
        binding.inputSheet.viewModel = viewModel
        viewModel.start()

        (activity as MainActivity).setSupportActionBar(toolBar)

        val actionBar = (activity as MainActivity).getSupportActionBar()
        actionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(getString(R.string.home_scan_title))
        }
        behavior = BottomSheetBehaviorGoogleMapsLike.from(binding.bottomSheet)
        behavior.addBottomSheetCallback(callback)
        val mergedAppBarBehavior = MergedAppBarBehavior.from(binding.mergedappbarlayout)
        mergedAppBarBehavior.setToolbarTitle(getString(R.string.home_scan_full_added_section_title))
        mergedAppBarBehavior.setNavigationOnClickListener(View.OnClickListener {
            behavior.state = BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT
        })
        behavior.state = BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT

//        val adapter = ItemPagerAdapter(binding.root.context, mDrawables)
        val viewPager = binding.pager
        viewPager.adapter = viewModel.carouselAdapter
        binding.camera.onStart()

    }

    override fun onNamePurchaseTextChanged(text: String) {
        binding.viewModel?.onNameChanged(text)
    }

    override fun onCostPurchaseTextChanged(text: String) {
        binding.viewModel?.onCostChanged(text)
    }

    override fun onCountPurchaseTextChanged(text: String) {
        binding.viewModel?.onCountChanged(text)
    }


    override fun insertPurchase() {
        val purchase = binding.inputSheet.namePurchaseEditText.text.toString()
        val cost = binding.inputSheet.costPurchaseEditText.text.toString()
        val count = binding.inputSheet.countPurchaseEditText.text.toString()
        binding.viewModel?.insertPurchase(Purchase(purchase, cost.toInt(), count.toInt()))
        clearPurchase()
    }

    override fun scanCard() {
        binding.camera.captureImage(this)
    }

    override fun onImage(cameraView: CameraKitView?, byteArray: ByteArray?) {
        byteArray?.let {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            getCardDetails(bmp)
        }

    }

    private fun getCardDetails(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val firebaseVisionTextDetector = FirebaseVision.getInstance().cloudTextRecognizer

        firebaseVisionTextDetector.processImage(image)
            .addOnSuccessListener {
                val words = it.text.split("\n")
                for (word in words) {
                    Log.d("devcpp", word)
                    //REGEX for detecting a credit card
                    if (word.replace(
                            " ",
                            ""
                        ).matches(Regex("^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})\$"))
                    )
                        Toast.makeText(context, word, Toast.LENGTH_SHORT).show()
                    //Find a better way to do this
                    if (word.contains("/")) {
                        for (year in word.split(" ")) {
                            if (year.contains("/"))
                                Toast.makeText(context, year, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
            .addOnFailureListener {
                Log.e("devcpp", it.toString())
                Toast.makeText(context, "Sorry, something went wrong! "+ it, Toast.LENGTH_SHORT).show()
            }
    }


    override fun clearPurchase() {
        binding.inputSheet.namePurchaseEditText.text?.clear()
        binding.inputSheet.costPurchaseEditText.text?.clear()
        binding.inputSheet.countPurchaseEditText.text?.clear()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        binding.camera.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    class BottomSheetCallback() :
        BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            // Nothing impl
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                }
            }
        }
    }
}