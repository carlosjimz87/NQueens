package com.carlosjimz87.nqueens.ui.screens

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carlosjimz87.nqueens.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end test verifying a basic user interaction flow in the real activity.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityE2ETest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun placeQueenOnBoard() {

        composeRule
            .onNodeWithTag("cell_0_0")
            .performClick()
    }
}
