package com.fb.roottest

import androidx.test.espresso.Espresso.onView

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint


object ViewMatcher {
    fun iSeeViewWithId(viewId: Int){
     onView(withId(viewId)).check(matches(isDisplayed()))
    }
    fun iSeeViewWithHint(text:String){
        onView(withHint(text)).check(matches(isDisplayed()))
    }


}