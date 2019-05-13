package com.esp.localjobs

import com.esp.localjobs.models.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Shared view model between filter, jobs and proposals fragment.
 * When the user asks to filter results @variable userRequestedFilterResults is set to true.
 */

class FilterViewModel : ViewModel() {
    val MAX_RANGE_KM = 50
    val range = MutableLiveData<Int>()
    val query = MutableLiveData<String>()
    val location = MutableLiveData<Location>()
    val userRequestedFilteredResults = MutableLiveData<Boolean>()

    init {
        setDefaultValues()
    }

    fun setDefaultValues() {
        range.value = MAX_RANGE_KM // -1 is interpreted as +inf
        query.value = ""
        userRequestedFilteredResults.value = false
        // TODO provide current position with a location manager or something else
    }
}