package com.fb.roottest.data.db

import androidx.room.*

const val ID_DB_FIELD: String = "id"

@Entity(
    tableName = "purchases",
    indices = [Index(value = ["brand_id", "purchase"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = Brand::class, parentColumns = [ID_BRAND],
        childColumns = ["brand_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Purchase(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = ID_DB_FIELD) var localId: Long,
    @ColumnInfo(name = "purchase") val purchase: String,
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "cost") val cost: Int,
    @ColumnInfo(name = "image") val image: String,
    @ColumnInfo(name = "brand_id") var brandId: Long,

     var brandName: String
) {


}