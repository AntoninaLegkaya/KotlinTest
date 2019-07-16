package com.fb.roottest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.fb.roottest.data.ResultQuery
import com.fb.roottest.data.db.AppDataBase
import com.fb.roottest.data.db.Brand
import com.fb.roottest.data.db.Purchase
import com.fb.roottest.data.repository.Repository
import com.fb.roottest.data.repository.RootRepository
import com.fb.roottest.home.HomeViewModel
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PurchaseDaoTest {

    lateinit var testDataBase: AppDataBase
    lateinit var rootRepository: Repository
    private val PURCHASE = Purchase(0, "testPurchase", 7, 77, "", 0, "")
    private val BRAND = Brand(0, "brand")
    private lateinit var viewModel: HomeViewModel

    @Rule
    @JvmField
    var activityRule = ActivityTestRule(
        MainActivity::class.java
    )
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    @Throws(Exception::class)
    public fun initDB() {
        testDataBase = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            AppDataBase::class.java
        ).build()
        rootRepository = RootRepository(testDataBase)

        //Given
        viewModel = HomeViewModel(activityRule.activity.application, rootRepository)
    }

    @After
    @Throws(Exception::class)
    public fun closeDB() {
        testDataBase.close()
    }

    @Test
    public fun testInsertNewBrand() {
        //When
        rootRepository.insertNewBrand(BRAND)
        val brand = rootRepository.getBrandById(BRAND.id)

        val brandObserver = viewModel.DefaultObserver<Brand, ResultQuery<Brand>>()
            .handleSuccess {
                assertEquals(0, it.getResult()?.id);
            }

    }

    @Test
    fun testgetAllBrands() {
        //When
        testInsertNewBrand()

               val brands= rootRepository.getAllBrands()

        val brandsObserver = viewModel.DefaultObserver<List<Brand>, ResultQuery<List<Brand>>>()
            .handleError { }
            .handleSuccess {
                assertEquals(1, it.getResult()?.size);
                //Then
                it.getResult()?.let {
                    PURCHASE.brandId = it.get(0).id
                    PURCHASE.brandName=it.get(0).brand
                    rootRepository.insertPurchase(PURCHASE)


                    val purchaseObserver = viewModel.DefaultObserver<List<Purchase>, ResultQuery<List<Purchase>>>()
                        .handleError { }
                        .handleSuccess { list ->
                            list.getResult()?.size?.let {
                                assertEquals(true, it > 0)
                                assertEquals(PURCHASE.brandName, list.getResult()?.get(0)?.brandName);
                                assertEquals("testPurchase",list.getResult()?.get(0)?.purchase );
                            }
                        }
                    val purchases = rootRepository.getAllPurchase()
                    purchases.observeForever(purchaseObserver)
                }
            }
        brands.observeForever(brandsObserver)
    }
}

//       val brands= rootRepository.getAllBrands()
//
//        val brandsObserver = viewModel.DefaultObserver<List<Brand>, ResultQuery<List<Brand>>>()
//            .handleError { }
//            .handleSuccess {
//                assertEquals(0, it.getResult()?.size);
//                //Then
//                it.getResult()?.let {
//                    PURCHASE.brandId = it.get(0).id
//                    PURCHASE.brandName=it.get(0).brand
//                    rootRepository.insertPurchase(PURCHASE)
//
//                    val purchaseObserver = viewModel.DefaultObserver<List<Purchase>, ResultQuery<List<Purchase>>>()
//                        .handleError { }
//                        .handleSuccess { list ->
//                            list.getResult()?.size?.let {
//                                assertEquals(true, it > 0)
//                            }
//                            assertEquals(list.getResult()?.get(0)?.localId, PURCHASE.localId);
//                            assertEquals(list.getResult()?.get(0)?.purchase, "testPurchase");
//                        }
//                    val purchases = rootRepository.getAllPurchase()
//                    purchases.observeForever(purchaseObserver)
//
//                }
//
//
//            }
//        brands.observeForever(brandsObserver)

