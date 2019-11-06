package com.fb.roottest.home

interface PurchaseClickListener {
    fun generatedData()
    fun insertData()
    fun clearPurchase()
    fun onNamePurchaseTextChanged(text:String)
    fun onCostPurchaseTextChanged(text:String)
    fun onCountPurchaseTextChanged(text:String)
    fun onBrandPurchaseTextChanged(text:String)
    fun scanCard()
    fun encryptDb()
    fun encryptPhoto()
    fun getEncryptPhoto()
    fun getSharedPhoto()

}