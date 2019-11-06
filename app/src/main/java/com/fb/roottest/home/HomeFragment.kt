package com.fb.roottest.home

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
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
import com.fb.roottest.data.repository.RepositoryFactory
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
import java.io.*
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.system.measureTimeMillis

class HomeFragment : BaseFragment<FragmentHomeBinding>(), PurchaseClickListener,
    CameraKitView.ImageCallback {
    companion object {
        const val SALT = "salt"
        const val IV = "iv"
        const val ENCRYPTED = "encrypted"
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
            observeCommand(onStartTimerEvent) {
                binding.inputSheet.layoutContent.tv_process.setText(it)
            }
            observeCommand(onInsertPurchaseEvent) {
                binding.viewModel?.updateTime()
                if (it <= 999) binding.viewModel?.generatedData(it + 1)
            }
        }
    }


    override fun encryptDb() {
        binding.viewModel?.encryptBd()
        activity?.let { RepositoryFactory.encryptDataBase(it.applicationContext) }
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


    override fun generatedData() {
        binding.viewModel?.startTimer("Generate data")
        binding.viewModel?.generatedData(0)
    }

    override fun scanCard() {
        scanTextFromCamera()
//        binding.camera.captureImage(this)
    }

    override fun encryptPhoto() {
        if (binding.bottomSheet.passwordEditText.text.toString().isNotEmpty()) {
            val photoBytes = getPhotoBytes(Uri.fromFile(File(avatarFile)))
            val map = encryptBytes(
                photoBytes,
                binding.bottomSheet.passwordEditText.text.toString()
            )
            saveEncryptedPhotoToSharedPref(map)

            context?.openFileOutput("test.dat", Context.MODE_PRIVATE)?.use {
                it.write(map.get(ENCRYPTED))
                it.close()
            }
            context?.openFileOutput("map.dat", Context.MODE_PRIVATE)?.use {
                val oos = ObjectOutputStream(it);
                oos.writeObject(map)
                oos.close()
            }
            loadImage(binding.bottomSheet.photo, null)
        } else {
            Toast.makeText(context, "Enter password for encryption", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getSharedPhoto() {
        binding.viewModel?.startTimer("Get encrypt photo from Shared")
        val map = getEncryptedPhotoFromSharedPref()
        val timer = measureTimeMillis {
            if (binding.bottomSheet.passwordEditText.text.toString().isNotEmpty()) {
                decryptData(map, binding.bottomSheet.passwordEditText.text.toString()).let {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    loadImage(binding.bottomSheet.photo, bitmap)
                }
            } else {
                Toast.makeText(context, "Enter password for decryption", Toast.LENGTH_SHORT).show()
            }
        }
        binding.viewModel?.updateTimer(timer)
    }

    override fun getEncryptPhoto() {
        binding.viewModel?.startTimer("Get encrypt photo from file")
        val timer = measureTimeMillis {
            var restedMap: HashMap<String, ByteArray>? = null
            //Now time to read the family back into memory
            ObjectInputStream(FileInputStream(File(context?.filesDir, "map.dat"))).use { it ->
                //Read the map back from the file
                restedMap = it.readObject() as HashMap<String, ByteArray>
            }
            if (binding.bottomSheet.passwordEditText.text.toString().isNotEmpty()) {
                restedMap?.let {
                    val bytes =
                        decryptData(it, binding.bottomSheet.passwordEditText.text.toString())
                    loadImage(
                        binding.bottomSheet.photo,
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    )
                }
            } else {
                Log.d("devcpp", "Decrypt password  empty")
                Toast.makeText(context, "Enter password for decryption", Toast.LENGTH_SHORT).show()
            }
        }
        binding.viewModel?.updateTimer(timer)
    }

    private fun saveEncryptedPhotoToSharedPref(map: HashMap<String, ByteArray>) {
        val editor = context?.getSharedPreferences("prefs", Context.MODE_PRIVATE)?.edit();
        val keyBase64String = Base64.encodeToString(ENCRYPTED.toByteArray(), Base64.NO_WRAP)
        val valueBase64String = Base64.encodeToString(map.get(ENCRYPTED), Base64.NO_WRAP)
        val ivKeyBase64String = Base64.encodeToString(IV.toByteArray(), Base64.NO_WRAP)
        val ivValueBase64String = Base64.encodeToString(map.get(IV), Base64.NO_WRAP)
        val saltKeyBase64String = Base64.encodeToString(SALT.toByteArray(), Base64.NO_WRAP)
        val saltValueBase64String = Base64.encodeToString(map.get(SALT), Base64.NO_WRAP)
        editor?.let {
            it.putString(keyBase64String, valueBase64String)
            it.putString(saltKeyBase64String, saltValueBase64String)
            it.putString(ivKeyBase64String, ivValueBase64String)
            it.commit()
        }
    }

    private fun getEncryptedPhotoFromSharedPref(): HashMap<String, ByteArray> {
        val map = HashMap<String, ByteArray>()
        val preferences = context?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val photoEncryptedString = preferences?.getString(
            Base64.encodeToString(ENCRYPTED.toByteArray(), Base64.NO_WRAP),
            "default"
        )
        val salt64EncryptedString = preferences?.getString(
            Base64.encodeToString(SALT.toByteArray(), Base64.NO_WRAP),
            "default"
        )
        val iv64EncryptedString = preferences?.getString(
            Base64.encodeToString(IV.toByteArray(), Base64.NO_WRAP),
            "default"
        )
        map.put(ENCRYPTED, Base64.decode(photoEncryptedString, Base64.NO_WRAP))
        map.put(SALT, Base64.decode(salt64EncryptedString, Base64.NO_WRAP))
        map.put(IV, Base64.decode(iv64EncryptedString, Base64.NO_WRAP))
        return map
    }

    private fun encryptBytes(
        plainTextBytes: ByteArray,
        passwordString: String
    ): HashMap<String, ByteArray> {
        val map = HashMap<String, ByteArray>()
        try {
            //Random salt for next step
            val random = SecureRandom();
            val salt = ByteArray(256)
            random.nextBytes(salt)

            // PBKDF2- derive the key from the password, don't use password directly
            val passwordChar = passwordString.toCharArray()
            val pbKeySpec = PBEKeySpec(passwordChar, salt, 1324, 256) //1324 -iterations
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
            val keySpec = SecretKeySpec(keyBytes, "AES")

            //Create initialization vector for AES
            val ivRandom = SecureRandom()
            val iv = ByteArray(16)
            ivRandom.nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            //Encrypt
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encrypted = cipher.doFinal(plainTextBytes)

            map.put(SALT, salt)
            map.put(IV, iv)
            map.put(ENCRYPTED, encrypted)
        } catch (e: Exception) {
            Log.e("devcpp", "encryption exception", e);
        }
        return map
    }

    private fun decryptData(map: HashMap<String, ByteArray>, passwordString: String): ByteArray {
        var decrypted = ByteArray(0)
        try {

            val salt = map.get(SALT);
            val iv = map.get(IV);
            val encrypted = map.get(ENCRYPTED);

            //regenerate key from password
            val passwordChar = passwordString.toCharArray();
            val pbKeySpec = PBEKeySpec(passwordChar, salt, 1324, 256);
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).getEncoded();
            val keySpec = SecretKeySpec(keyBytes, "AES");

            //Decrypt
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            val ivSpec = IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            decrypted = cipher.doFinal(encrypted);
        } catch (e: Exception) {
            Log.e("devcpp", "decryption exception", e);
        }

        return decrypted;
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
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_STORAGE
                )
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
                    imageCaptureUri =
                        FileProvider.getUriForFile(it, BuildConfig.APPLICATION_ID + ".provider", f)
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
                Toast.makeText(context, "Sorry, something went wrong! " + it, Toast.LENGTH_SHORT)
                    .show()
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

    private fun getPhotoBytes(uri: Uri): ByteArray {
        var bytes = ByteArray(0)
        try {
            val fileInputStream = requireContext().contentResolver.openInputStream(uri)
            if (fileInputStream != null) {
                bytes = IOUtils.toByteArray(fileInputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bytes
    }


    private fun loadImage(imageView: ImageView, url: String?) {
        Glide.with(imageView.context)
            .load(url)
            .into(imageView)
    }

    private fun loadImage(imageView: ImageView, bitmap: Bitmap) {
        Glide.with(imageView.context)
            .asBitmap()
            .load(bitmap)
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