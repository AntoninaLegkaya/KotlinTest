package com.fb.roottest

import android.content.Context
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.fb.roottest.ViewMatcher.iSeeViewWithId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

@RunWith(AndroidJUnit4::class)
class AddPurchaseBindingTest {
    companion object {
        val purchase1 = "purchase1"
        val purchaseEmpty = ""
        val cost = 10
        val costEmpty = 0
        val count = 10
        val countEmpty = 0
        val brand = "brand"
    }

    @Mock
    lateinit var ctx: Context

    @Rule
    @JvmField
    var activityRule = ActivityTestRule<MainActivity>(
        MainActivity::class.java
    )

    @Test
    @Throws(Exception::class)
    fun addPurchaseSuccess() {
        iSeeViewWithId(R.id.fragmentContainerFrameLayout)
        Thread.sleep(5000)
        onView(withId(R.id.namePurchaseEditText))
            .perform(typeText(purchase1))
        closeSoftKeyboard()
        onView(withId(R.id.countPurchaseEditText))
            .perform(typeText(count.toString()))
        closeSoftKeyboard()
        onView(withId(R.id.costPurchaseEditText))
            .perform(typeText(cost.toString()))
        closeSoftKeyboard()

        onView(withId(R.id.brandPurchaseEditText))
            .perform(typeText(brand))
        closeSoftKeyboard()

        Thread.sleep(250);
        onView(withId(R.id.btn_add_purchase))
            .perform(click())
    }
}