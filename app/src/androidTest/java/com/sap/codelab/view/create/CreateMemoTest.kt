package com.sap.codelab.view.create

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.sap.codelab.EXTRA_LATITUDE
import com.sap.codelab.EXTRA_LONGITUDE
import com.sap.codelab.R
import com.sap.codelab.location.OsmMapLocationPickerActivity
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateMemoTest {

    private val activityRule = ActivityScenarioRule(CreateMemo::class.java)

    // Permissions are granted before the activity starts so that picking a location
    // doesn't trigger the background-location/POST_NOTIFICATIONS rationale dialog.
    @get:Rule
    val rules: RuleChain = RuleChain
        .outerRule(GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        ))
        .around(activityRule)

    @Before fun initIntents() = Intents.init()
    @After fun releaseIntents() = Intents.release()

    //  Verify initial UI state
    @Test
    fun emptyLocationState_shownOnLaunch() {
        onView(withId(R.id.location_empty_container))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withId(R.id.location_selected_container))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    // Verify pick location navigation
    @Test
    fun pickLocationButton_launchesOsmMapPickerActivity() {
        onView(withId(R.id.pick_location_button)).perform(click())
        Intents.intended(hasComponent(OsmMapLocationPickerActivity::class.java.name))
    }

    // ActivityResult handling → UI state transition
    @Test
    fun locationSelectedState_shownAfterMapResult() {
        stubMapPickerResult(lat = 52.52, lng = 13.40)

        onView(withId(R.id.pick_location_button)).perform(click())

        onView(withId(R.id.location_selected_container))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withId(R.id.location_empty_container))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withId(R.id.location_coordinates))
            .check(matches(withText(containsString("52.52000"))))
    }

    // ViewModel state survives configuration change
    @Test
    fun locationSelectedState_survivesScreenRotation() {
        stubMapPickerResult(lat = 48.0, lng = 16.0)
        onView(withId(R.id.pick_location_button)).perform(click())

        activityRule.scenario.recreate()

        onView(withId(R.id.location_selected_container))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withId(R.id.location_coordinates))
            .check(matches(withText(containsString("48.00000"))))
    }

    private fun stubMapPickerResult(lat: Double, lng: Double) {
        Intents.intending(hasComponent(OsmMapLocationPickerActivity::class.java.name))
            .respondWith(ActivityResult(
                RESULT_OK,
                Intent().putExtra(EXTRA_LATITUDE, lat).putExtra(EXTRA_LONGITUDE, lng)
            ))
    }
}
