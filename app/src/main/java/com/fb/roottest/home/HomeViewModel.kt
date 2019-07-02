package com.fb.roottest.home

import android.app.Application
import android.util.Log
import androidx.annotation.StringRes
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.fb.roottest.R
import com.fb.roottest.base.BaseViewModel
import com.fb.roottest.base.combineWith
import com.fb.roottest.data.ResultQuery
import com.fb.roottest.data.db.Purchase
import com.fb.roottest.data.repository.Repository

class HomeViewModel(application: Application, repository: Repository) : BaseViewModel(application, repository) {

    var isListEmpty = ObservableField<Boolean>(true)
    val _isPurchaseNameValid = MediatorLiveData<Boolean>()
    val _isCostPurchaseValid = MediatorLiveData<Boolean>()
    val _isCountPurchaeValid = MediatorLiveData<Boolean>()

    private val _isInsertButtonEnabled: MutableLiveData<Boolean> =
        _isPurchaseNameValid.combineWith(_isCostPurchaseValid) { nameValid, costValid ->
            (nameValid ?: false) && (costValid ?: false)
        }.combineWith(_isCountPurchaeValid) { combineFirst, countValid ->
            (combineFirst ?: false) && (countValid ?: false)
        }

    val isInsertButtonEnabled: LiveData<Boolean>
        get() = _isInsertButtonEnabled

    private val _purchaseInputState = MediatorLiveData<TextInputState>()
    val purchaseInputState: LiveData<TextInputState>
        get() = _purchaseInputState

    private val _costInputState = MediatorLiveData<TextInputState>()
    val costInputState: LiveData<TextInputState>
        get() = _costInputState
    private val _countInputState = MediatorLiveData<TextInputState>()
    val countInputState: LiveData<TextInputState>
        get() = _costInputState

    private var purchaseData: MediatorLiveData<ResultQuery<List<Purchase>>>? = null
    private val purchaseObserver = DefaultObserver<List<Purchase>, ResultQuery<List<Purchase>>>()
        .handleError { applyStatesError() }
        .handleSuccess {
            it.getResult()?.run {
                if (this.isEmpty()) {
                    Log.d("devcpp","List empty")
                    isListEmpty.set(false)
                } else{
                  for (purchase in this){
                      Log.d("devcpp", "Purchase: "+ purchase.purchase+ "\n"+
                              "Count: "+ purchase.count+ "\n"+
                              "Cost: "+ purchase.cost)
                  }

                }
            }
        }

    fun onNameChanged(name: String) {
        applyStatesNoError()
        _isPurchaseNameValid.postValue(name.isNotEmpty())
    }

    fun onCostChanged(cost: String) {
        applyStatesNoError()
        _isCostPurchaseValid.postValue(cost.isNotEmpty())
    }

    fun onCountChanged(cost: String) {
        applyStatesNoError()
        _isCountPurchaeValid.postValue(cost.isNotEmpty())
    }

    private fun applyStatesNoError() {
        applyPurchaseInputState(TextInputState())
        applyCostInputState(TextInputState())
        applyCountInputState(TextInputState())
    }

    private fun applyStatesError() {
        _isInsertButtonEnabled.postValue(false)
        applyPurchaseInputState(TextInputState(R.string.purchase_error_input_state))
        applyCostInputState(TextInputState(R.string.purchase_cost_error_input_state))
        applyCountInputState(TextInputState(R.string.purchase_count_error_input_state))
    }

    private fun applyPurchaseInputState(inputState: TextInputState) {
        _purchaseInputState.value = inputState
    }

    private fun applyCostInputState(inputState: TextInputState) {
        _costInputState.value = inputState
    }

    private fun applyCountInputState(inputState: TextInputState) {
        _countInputState.value = inputState
    }

    fun start() {
        getPurchases()
        purchaseData?.observeForever(purchaseObserver)
    }

    fun insertPurchase(purchase: Purchase) {
        repository.insertPurchase(purchase)
        getPurchases()
    }

    fun getPurchases() {
        purchaseData= repository.getAllPurchase()
    }

    override fun onCleared() {
        super.onCleared()
        purchaseData?.removeObserver(purchaseObserver)
    }


}

data class TextInputState(@StringRes val errorMessage: Int? = null)