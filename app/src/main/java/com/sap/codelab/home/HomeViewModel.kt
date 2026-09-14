package com.sap.codelab.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.AppDependencies
import com.sap.codelab.core.model.Memo
import com.sap.codelab.core.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home Activity.
 */
internal class HomeViewModel : ViewModel() {

    private var isShowAll = false
    private val _memos: MutableStateFlow<List<Memo>> = MutableStateFlow(listOf())
    val memos: StateFlow<List<Memo>> = _memos

    /**
     * Loads all memos.
     */
    fun loadAllMemos() {
        isShowAll = true
        viewModelScope.launch {
            _memos.value = Repository.getAll()
        }
    }

    /**
     * Loads all open (not done) memos.
     */
    fun loadOpenMemos() {
        isShowAll = false
        viewModelScope.launch {
            _memos.value = Repository.getOpen()
        }
    }

    /**
     * Re-runs the last load (all or open) to pick up any external changes.
     */
    fun refreshMemos() {
        if (isShowAll) {
            loadAllMemos()
        } else {
            loadOpenMemos()
        }
    }

    /**
     * Updates the given memo, marking it as done if isChecked is true.
     *
     * @param memo      - the memo to update.
     * @param isChecked - whether the memo has been checked (marked as done).
     */
    fun updateMemo(memo: Memo, isChecked: Boolean) {
        viewModelScope.launch {
            // We'll only forward the update if the memo has been checked, since we don't offer to uncheck memos right now
            if (isChecked) {
                Repository.saveMemo(memo.copy(isDone = true))
                if (memo.hasLocationReminder) {
                    AppDependencies.locationReminderManager.removeReminder(memo.id)
                }
            }
        }
    }
}
