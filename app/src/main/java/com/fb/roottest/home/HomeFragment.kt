package com.fb.roottest.home

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView
import com.bumptech.glide.Glide
import com.camerakit.CameraKitView
import com.fb.roottest.BuildConfig
import com.fb.roottest.MainActivity
import com.fb.roottest.R
import com.fb.roottest.base.BaseFragment
import com.fb.roottest.base.paralax.BottomSheetBehaviorGoogleMapsLike
import com.fb.roottest.base.paralax.MergedAppBarBehavior
import com.fb.roottest.databinding.FragmentHomeBinding
import com.fb.roottest.util.observeCommand
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.util.IOUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.guna.ocrlibrary.OCRCapture
import com.guna.ocrlibrary.OcrCaptureActivity.TextBlockObject
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
import com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE
import kotlinx.android.synthetic.main.layout_bottom_sheet.view.*
import java.io.File
import java.io.IOException

class HomeFragment : BaseFragment<FragmentHomeBinding>(), PurchaseClickListener, CameraKitView.ImageCallback {

    companion object {
        const val CAMERA_SCAN_TEXT = 0
        const val LOAD_IMAGE_RESULTS = 1
        const val REQUEST_PERMISSION_CAMERA = 4
        const val REQUEST_PERMISSION_STORAGE = 5
        const val REQUEST_CAPTURE_IMAGE = 6
        const val DEFAULT_LOGO_STRING = "";
    }

    lateinit var behavior: BottomSheetBehaviorGoogleMapsLike<NestedScrollView>

    override val contentLayoutId: Int
        get() = R.layout.fragment_home

    private val callback = BottomSheetCallback()

    override val showToolbar: Boolean
        get() = false

    private var avatarFile = ""
    var avatarBase64 = DEFAULT_LOGO_STRING;

    override fun setupBinding(binding: FragmentHomeBinding) {
        val toolBar = binding.toolbar
        val viewModel = obtainViewModel(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.listener = this
        binding.inputSheet.listener = this
        binding.inputSheet.viewModel = viewModel
        viewModel.start()
        observeViewModel(viewModel)

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

        val viewPager = binding.pager
        viewPager.adapter = viewModel.carouselAdapter
//        binding.camera.onStart()

        binding.bottomSheet.bottom_app_bar.replaceMenu(R.menu.menu_scan);
        binding.bottomSheet.bottom_app_bar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.actionPhoto -> {
                    onOpenGalleryClicked()
                }
                R.id.actionCamera -> {
                    onCaptureClicked()
                }
            }
            true
        }
    }

    private fun observeViewModel(viewModel: HomeViewModel) {
        with(viewModel) {
            observeCommand(isAddedPurchase) {
                if (it) clearPurchase()
            }
        }
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

    override fun onBrandPurchaseTextChanged(text: String) {
        binding.viewModel?.onBrandChanged(text)
    }

    override fun insertData() {
        val purchase = binding.inputSheet.namePurchaseEditText.text.toString()
        val cost = binding.inputSheet.costPurchaseEditText.text.toString()
        val count = binding.inputSheet.countPurchaseEditText.text.toString()
        val brand = binding.inputSheet.brandPurchaseEditText.text.toString()
        binding.viewModel?.insertBrand(brand, purchase, count.toInt(), cost.toInt(), avatarBase64)
        // binding.viewModel?.insertData(Purchase(0, purchase, count.toInt(), cost.toInt(), avatarBase64,))
    }

    override fun scanCard() {
        scanTextFromCamera()
//        binding.camera.captureImage(this)
    }

    fun onCaptureClicked() {
        context?.let {
            if (ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.CAMERA
                ) !== PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_CAMERA
                )
            } else {
                startCamera()
            }
        }

    }

    fun onOpenGalleryClicked() {
        context?.let {
            if (ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_STORAGE)
            } else {
                openGallery()
            }
        }
    }

    private fun openGallery() {
        context?.let {
            CropImage.activity()
                .setRequestedSize(512, 512)
                .setAspectRatio(1, 1)
                .start(it, this)
        }
    }

    private fun startCamera() {
        val imageCaptureUri: Uri
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activity?.let {
            try {
                val f = createImageFile()
                avatarFile = f.getAbsolutePath()
                if (Build.VERSION.SDK_INT >= 24) {
                    imageCaptureUri = FileProvider.getUriForFile(it, BuildConfig.APPLICATION_ID + ".provider", f)
                } else {
                    imageCaptureUri = Uri.fromFile(f)
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri)
                if (takePictureIntent.resolveActivity(it.getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_CAPTURE_IMAGE)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val imageFileName = "JPEG_" + System.currentTimeMillis() + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            }
        } else if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onImage(cameraView: CameraKitView?, byteArray: ByteArray?) {
        byteArray?.let {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            getCardDetails(bmp)
        }
    }

    private fun scanTextFromCamera() {
        //Scan text from camera.
        OCRCapture.Builder(activity)
            .setUseFlash(true)
            .setAutoFocus(true)
            .buildWithRequestCode(CAMERA_SCAN_TEXT);
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
                Toast.makeText(context, "Sorry, something went wrong! " + it, Toast.LENGTH_SHORT).show()
            }
    }

    override fun clearPurchase() {
        binding.inputSheet.namePurchaseEditText.text?.clear()
        binding.inputSheet.costPurchaseEditText.text?.clear()
        binding.inputSheet.countPurchaseEditText.text?.clear()
        binding.inputSheet.brandPurchaseEditText.text?.clear()
        avatarBase64 = ""
        loadImage(binding.inputSheet.photo, "")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            when (requestCode) {
                CAMERA_SCAN_TEXT -> {
                    if (resultCode == CommonStatusCodes.SUCCESS) {
                        Log.d("devcpp", data.getStringExtra(TextBlockObject))
                        binding.tvScanInfo.setText(data.getStringExtra(TextBlockObject))
                    }
                }
                LOAD_IMAGE_RESULTS -> {
                    val pickedImage = data.getData()
                    val text = OCRCapture.Builder(activity).getTextFromUri(pickedImage)
                    Log.d("devcpp", text)
                    binding.tvScanInfo.setText(text)
                }
                CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    if (resultCode == RESULT_OK) {
                        val uri = result.uri
                        loadImage(binding.bottomSheet.photo, uri.toString())
                        avatarBase64 = convertToBase64(uri)
                    }
                }
                CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                }

                REQUEST_CAPTURE_IMAGE -> {
                    if (resultCode == RESULT_OK) {
                        val logo = File(avatarFile)
                        context?.let {
                            CropImage.activity(Uri.fromFile(logo))
                                .setRequestedSize(512, 512)
                                .setAspectRatio(1, 1)
                                .start(it, this)
                            avatarBase64 = convertToBase64(Uri.fromFile(logo))
                        }
                    }
                }
            }
        }
    }

    private fun convertToBase64(uri: Uri): String {
        var result = DEFAULT_LOGO_STRING
        try {
            val fileInputStream = requireContext().contentResolver.openInputStream(uri)
            if (fileInputStream != null) {
                val bytes = IOUtils.toByteArray(fileInputStream)
                result = Base64.encodeToString(bytes, Base64.DEFAULT)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }

    private fun loadImage(imageView: ImageView, url: String?) {
        Glide.with(imageView.context)
            .load(url)
            .into(imageView)
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