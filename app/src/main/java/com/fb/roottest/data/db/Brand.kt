package com.fb.roottest.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

const val ID_BRAND="brand_id"
@Entity(tableName = "brands",
    indices = [Index(value = ["brand"], unique = true)])
data class Brand (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name= ID_BRAND) var id: Long,
    @ColumnInfo(name= "brand") val brand:String
)