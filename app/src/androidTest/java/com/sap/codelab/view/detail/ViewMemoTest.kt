package com.sap.codelab.view.detail

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sap.codelab.R
import com.sap.codelab.core.model.Memo
import com.sap.codelab.core.repository.Repository
import com.sap.codelab.core.utils.KEY_MEMO_ID
import com.sap.codelab.detail.ViewMemo
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.containsString
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewMemoTest {

    // Verify ViewMemo reads and renders location
    @Test
    fun locationCoordinates_displayedForMemoWithLocation() {
        val lat = 52.52
        val lng = 13.40
        val memoId = runBlocking {
            Repository.saveMemo(
                Memo(
                    id = 0,
                    title = "Test Memo",
                    description = "Some description",
                    reminderDate = 0L,
                    reminderLatitude = lat,
                    reminderLongitude = lng
                )
            )
        }

        val intent = Intent(ApplicationProvider.getApplicationContext(), ViewMemo::class.java)
            .putExtra(KEY_MEMO_ID, memoId)

        ActivityScenario.launch<ViewMemo>(intent).use {
            // ViewMemoViewModel loads the memo via Dispatchers.Default. A production-grade
            // test would register a coroutine IdlingResource; a short sleep is sufficient here.
            Thread.sleep(500)

            onView(withId(R.id.location_selected_container))
                .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
            onView(withId(R.id.location_coordinates))
                .check(matches(withText(containsString("52.52000"))))
            // ViewMemo hides the edit controls — they only belong in CreateMemo
            onView(withId(R.id.change_location_button))
                .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
            onView(withId(R.id.clear_location_button))
                .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        }
    }
}