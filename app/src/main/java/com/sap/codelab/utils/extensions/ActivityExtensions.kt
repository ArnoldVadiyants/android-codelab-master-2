package com.sap.codelab.utils.extensions

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Enables edge-to-edge display and applies system-bar insets to [root] and [appBar].
 *
 * @param root   the full-screen root view of the layout.
 * @param appBar the toolbar or app-bar view positioned at the top of the screen.
 */
internal fun AppCompatActivity.applyWindowInsets(root: View, appBar: View) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.updatePadding(left = insets.left, right = insets.right, bottom = insets.bottom)
        windowInsets
    }
    ViewCompat.setOnApplyWindowInsetsListener(appBar) { view, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.updatePadding(top = insets.top)
        WindowInsetsCompat.CONSUMED
    }
}
