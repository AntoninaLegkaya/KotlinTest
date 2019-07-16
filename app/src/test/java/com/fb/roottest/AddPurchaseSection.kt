package com.fb.roottest

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MediatorLiveData
import com.fb.roottest.data.ResultQuery
import com.fb.roottest.data.db.Purchase
import com.fb.roottest.data.repository.Repository
import com.fb.roottest.home.HomeViewModel
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.util.*

const val purchase1 = " purchase1"
const val purchaseEmpty = ""
const val cost = "10"
const val costEmpty = "0"
const val count = "10"
const val countEmpty = "0"
const val brand = "brand"
const val brandEmpty = ""

@RunWith(Parameterized::class)
class AddPurchaseSection {

    @JvmField
    @Parameterized.Parameter(value = 0)
    var countValue: String = ""

    @JvmField
    @Parameterized.Parameter(value = 1)
    var costValue: String = ""
    @JvmField
    @Parameterized.Parameter(value = 2)
    var brandValue: String = ""

    @JvmField
    @Parameterized.Parameter(value = 3)
    var isCountValid: Boolean = false
    @JvmField
    @Parameterized.Parameter(value = 4)
    var isCostValid: Boolean = false

    @JvmField
    @Parameterized.Parameter(value = 5)
    var isBrandValid: Boolean = false

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun initParameters(): Collection<Array<Any>> {
            return Arrays.asList(
                arrayOf(countEmpty, costEmpty, brandEmpty, false, false, false),
                arrayOf(countEmpty, costEmpty, brand, false, false, true),
                arrayOf(costEmpty, count, brandEmpty, false, true, false),
                arrayOf(countEmpty, cost, brand, false, true, true),
                arrayOf(count, costEmpty, brandEmpty, true, false, false),
                arrayOf(count, costEmpty, brand, true, false, true),
                arrayOf(count, cost, brandEmpty, true, true, false),
                arrayOf(count, cost, brand, true, true, true)
            )
        }
    }

    private val valueLiveData = MediatorLiveData<ResultQuery<List<Purchase>>>()

    private lateinit var viewModel: HomeViewModel
    @Mock
    lateinit var app: Application
    @Mock
    lateinit var repository: Repository

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        //Given
        viewModel = HomeViewModel(app, repository)
    }

    @Test
    public fun insertNewPurchase() {
        //Given
        Mockito.`when`(repository.insertPurchase(Purchase(0, purchase1, countValue.toInt(), costValue.toInt(), "", 0)))
            .thenAnswer {
                return@thenAnswer valueLiveData
            }
        Mockito.`when`(repository.getAllPurchase()).thenAnswer {
            return@thenAnswer valueLiveData
        }

        //When
        viewModel.insertPurchase(Purchase(0, purchase1, countValue.toInt(), costValue.toInt(), "", 0))

        //Then
//        if (isCostValid && isCountValid) {
//            assertEquals(null, viewModel.countInputState.value?.errorMessage)
//            assertEquals(null, viewModel.costInputState.value?.errorMessage)
//        } else if (!isCostValid && !isCountValid) {
//            assertEquals(false, viewModel.validateCost(costValue.toInt()))
//            assertEquals(false, viewModel.validateCount(countValue.toInt()))
//        } else if (!isCostValid) {
//            assertEquals(false, viewModel.validateCost(costValue.toInt()))
//        } else if (!isCountValid) {
//            assertEquals(false, viewModel.validateCount(countValue.toInt()))
//        }

        // 0 0 0
//        if ( !isCountValid&& !isCostValid && !isBrandValid) {
//            assertEquals(false, viewModel.validateCost(costValue.toInt()))
//            assertEquals(false, viewModel.validateCount(countValue.toInt()))
//            assertEquals(false, viewModel.validateBrand(brandValue))
//        }
        //0 0 1
        if (!isCountValid && !isCostValid && isBrandValid) {
        }
        // 0 1 0
        else if (!isCountValid && isCostValid && !isBrandValid) {
        }
        //0 1 1
        else if (!isCountValid && isCostValid && isBrandValid) {
            assertEquals(false, viewModel.validateCount(countValue.toInt()))
        }
        //1 0 0
        else if (isCountValid && !isBrandValid && !isCostValid) {
        }
        // 1 0 1
        else if (isCountValid && !isCostValid && isBrandValid) {
            assertEquals(false, viewModel.validateCost(costValue.toInt()))
        }
        //1 1 0
        else if (isCountValid && isCostValid && !isBrandValid) {
            assertEquals(false, viewModel.validateBrand(brandValue))
        }
        // 1 1 1
       else if (isBrandValid && isCostValid && isCountValid)
        {
            assertEquals(null, viewModel.countInputState.value?.errorMessage)
            assertEquals(null, viewModel.costInputState.value?.errorMessage)
            assertEquals(null, viewModel.brandInputState.value?.errorMessage)
        }
//        else
        // 0 0 0
//        if ( !isCountValid&& !isCostValid && !isBrandValid)
//        {
//            assertEquals(false, viewModel.validateCost(costValue.toInt()))
//            assertEquals(false, viewModel.validateCount(countValue.toInt()))
//            assertEquals(false, viewModel.validateBrand(brandValue))
//        }

    }

}



