package com.fb.roottest

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText

object ViewInteractor {

    fun iSwipeLeft(viewId: Int) {
        onView(withId(viewId)).perform(swipeLeft())
    }

    fun iSwipeRight(viewId: Int) {
        onView(withId(viewId)).perform(swipeRight())
    }
    fun iClickOnButtonWithText(text: String) {
        onView(withText(text)).perform(click())
    }
    fun iClickOnButtonWithId(viewId:Int) {
        onView(withId(viewId)).perform(click())
    }
}