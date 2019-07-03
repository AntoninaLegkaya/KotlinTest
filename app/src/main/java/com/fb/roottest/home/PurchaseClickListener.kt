package com.fb.roottest.home

interface PurchaseClickListener {
    fun insertPurchase()
    fun clearPurchase()
    fun onNamePurchaseTextChanged(text:String)
    fun onCostPurchaseTextChanged(text:String)
    fun onCountPurchaseTextChanged(text:String)
    fun scanCard()
}