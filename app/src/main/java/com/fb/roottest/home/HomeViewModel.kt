package com.fb.roottest.home

import android.app.Application
import androidx.annotation.StringRes
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.commonsware.cwac.saferoom.SQLCipherUtils
import com.fb.roottest.R
import com.fb.roottest.base.BaseViewModel
import com.fb.roottest.base.SingleLiveEvent
import com.fb.roottest.base.combineWith
import com.fb.roottest.custom.CarouselAdapter
import com.fb.roottest.data.ResultQuery
import com.fb.roottest.data.db.Brand
import com.fb.roottest.data.db.Purchase
import com.fb.roottest.data.repository.Repository
import java.lang.System.currentTimeMillis

class HomeViewModel(application: Application, repository: Repository) :
    BaseViewModel(application, repository) {

    var isListEmpty = ObservableField<Boolean>(true)
    val _isPurchaseNameValid = MediatorLiveData<Boolean>()
    val _isCostPurchaseValid = MediatorLiveData<Boolean>()
    val _isCountPurchaeValid = MediatorLiveData<Boolean>()
    val _isBrandPurchaeValid = MediatorLiveData<Boolean>()
    val _isAddedPurchase = MediatorLiveData<Boolean>()


    val adapter = PurchaseListAdapter()
    val carouselAdapter = CarouselAdapter()
    lateinit var purchase: Purchase
    lateinit var brand: Brand
    var isObserv: Boolean = false
    var startTimeField = ObservableField<String>()
    var stopTimeField = ObservableField<String>()
    private var startTime = 0L
    private var count: Int = 0

    private val _isInsertButtonEnabled: MutableLiveData<Boolean> =
        _isPurchaseNameValid.combineWith(_isCostPurchaseValid) { nameValid, costValid ->
            (nameValid ?: false) && (costValid ?: false)
        }.combineWith(_isCountPurchaeValid) { combineFirst, countValid ->
            (combineFirst ?: false) && (countValid ?: false)
        }

    val isInsertButtonEnabled: LiveData<Boolean>
        get() = _isInsertButtonEnabled
    val isAddedPurchase: LiveData<Boolean>
        get() = _isAddedPurchase

    private val _purchaseInputState = MediatorLiveData<TextInputState>()
    val purchaseInputState: LiveData<TextInputState>
        get() = _purchaseInputState

    private val _costInputState = MediatorLiveData<TextInputState>()
    val costInputState: LiveData<TextInputState>
        get() = _costInputState
    private val _countInputState = MediatorLiveData<TextInputState>()
    val countInputState: LiveData<TextInputState>
        get() = _countInputState
    val _brandInputState = MediatorLiveData<TextInputState>()
    val brandInputState: LiveData<TextInputState>
        get() = _brandInputState
    private val onStartTimerEventMutable = SingleLiveEvent<String>()
    private val onInsertPurchaseEventMutable = SingleLiveEvent<Int>()
    val onStartTimerEvent: LiveData<String>
        get() = onStartTimerEventMutable
    val onInsertPurchaseEvent: LiveData<Int>
        get() = onInsertPurchaseEventMutable


    private var purchaseData: MediatorLiveData<ResultQuery<List<Purchase>>>? = null
    private var brandData: MediatorLiveData<ResultQuery<List<Brand>>>? = null
    private val purchaseObserver = DefaultObserver<List<Purchase>, ResultQuery<List<Purchase>>>()
        .handleError {
            applyStatesError()
        }
        .handleSuccess {
            it.getResult()?.run {
                if (this.isEmpty()) {
                    isListEmpty.set(true)
                    adapter.setItems(emptyList<Purchase>().toMutableList())
                    adapter.notifyDataSetChanged()
                    carouselAdapter.setItems(emptyList<Purchase>().toMutableList())
                    carouselAdapter.notifyDataSetChanged()
                } else {
                    adapter.setItems(this as MutableList<Purchase>)
                    adapter.notifyDataSetChanged()
                    carouselAdapter.setItems(this)
                    carouselAdapter.notifyDataSetChanged()
                    isListEmpty.set(false)
                    _isAddedPurchase.postValue(true)
                }
            }
        }

    private val brandObserver = DefaultObserver<List<Brand>, ResultQuery<List<Brand>>>()
        .handleError { applyStatesError() }
        .handleSuccess {
            it.getResult()?.run {
                if (this.isNotEmpty()) {
                    var isInserted = false
                    for (entity in this) {
                        if (brand.brand.toLowerCase() == entity.brand.toLowerCase()) {
                            purchase.brandId = entity.id
                            purchase.brandName = entity.brand
                            insertPurchase(purchase)
                            isInserted = true
                        }
                    }
                    if (!isInserted) {
                        insertNewBrand()
                    }

                } else {
                    insertNewBrand()
                }

            }
        }

    private fun insertNewBrand() {
        repository.insertNewBrand(brand)
        purchase.brandId = brand.id
        purchase.brandName = brand.brand
        insertPurchase(purchase)
    }

    fun start() {
        getPurchases()
    }
    fun encryptBd(){
        repository.closeDataBase()
    }



    fun startTimer(proccess: String) {
        onStartTimerEventMutable.value = proccess
        startTime = currentTimeMillis()
        stopTimeField.set(((currentTimeMillis() - startTime) / 1000).toString())
    }

    fun updateTime() {
       stopTimeField.set(((currentTimeMillis() -  startTime)/1000).toString())
    }

    fun updateTimer(value: Long) {
        stopTimeField.set(value.toString())
    }


    fun insertPurchase(purchase: Purchase) {
        if (validateCount(purchase.count) && validateCost(purchase.cost)) {
            repository.insertPurchase(purchase)
        }
        count++
        onInsertPurchaseEventMutable.value = count;
    }

    fun generatedData(i: Int) {
        count = i
        insertBrand("brand_", "purchase_" + i, i, 500 * i, "")
    }

    fun insertBrand(
        brandName: String,
        namePurchase: String,
        count: Int,
        cost: Int,
        avatarBase64: String
    ) {
        purchase = Purchase(0, namePurchase, count, cost, avatarBase64, 0, "")
        brand = Brand(0, brandName)

        brandData = repository.getAllBrands()
        brandData?.observeForever(brandObserver)
    }

    fun getBrandById(id: Long) {
        repository.getBrandById(id)
    }

    fun getPurchases() {
        purchaseData = repository.getAllPurchase()
        purchaseData?.observeForever(purchaseObserver)
    }

    fun onNameChanged(name: String) {
        applyStatesNoError()
        _isPurchaseNameValid.postValue(name.isNotEmpty())
    }

    fun onCostChanged(cost: String) {
        applyStatesNoError()
        _isCostPurchaseValid.postValue(cost.isNotEmpty())
    }

    fun onCountChanged(count: String) {
        applyStatesNoError()
        _isCountPurchaeValid.postValue(count.isNotEmpty())
    }

    fun onBrandChanged(count: String) {
        applyStatesNoError()
        _isBrandPurchaeValid.postValue(count.isNotEmpty())
    }

    fun applyStatesNoError() {
        applyPurchaseInputState(TextInputState())
        applyCostInputState(TextInputState())
        applyCountInputState(TextInputState())
        applyBrandInputState(TextInputState())
    }

    private fun applyStatesError() {
        _isInsertButtonEnabled.postValue(false)
        applyPurchaseInputState(TextInputState(R.string.purchase_error_input_state))
        applyCostInputState(TextInputState(R.string.purchase_cost_error_input_state))
        applyCountInputState(TextInputState(R.string.purchase_count_error_input_state))
        applyBrandInputState(TextInputState(R.string.purchase_brand_error_input_state))
    }

    private fun applyPurchaseInputState(inputState: TextInputState) {
        _purchaseInputState.value = inputState
    }

    fun applyCostInputState(inputState: TextInputState) {
        _costInputState.value = inputState
    }

    fun applyCountInputState(inputState: TextInputState) {
        _countInputState.value = inputState
    }

    fun applyBrandInputState(inputState: TextInputState) {
        _brandInputState.value = inputState
    }

    public fun validateCost(value: Int): Boolean {
        if (value <= 0) {
            applyCostInputState(TextInputState(R.string.purchase_cost_error_input_state))
        } else {
            applyCostInputState(TextInputState())
        }
        return value > 0
    }

    public fun validateCount(value: Int): Boolean {
        if (value <= 0) {
            applyCountInputState(TextInputState(R.string.purchase_count_error_input_state))
        } else {
            applyCountInputState(TextInputState())
        }
        return value > 0
    }

    public fun validateBrand(value: String): Boolean {
        if (value.isEmpty()) {
            applyCountInputState(TextInputState(R.string.purchase_brand_error_input_state))
        } else {
            applyCountInputState(TextInputState())
        }
        return value.isNotEmpty()
    }

    override fun onCleared() {
        super.onCleared()
        purchaseData?.removeObserver(purchaseObserver)
        brandData?.removeObserver(brandObserver)
    }
}

data class TextInputState(@StringRes val errorMessage: Int? = null)