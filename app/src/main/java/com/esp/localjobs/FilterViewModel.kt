package com.esp.localjobs

import com.esp.localjobs.data.models.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Shared view model between filter, jobs and proposals fragment.
 * When the user asks to filter results @variable userRequestedFilterResults is set to true.
 *
 * Will probably be replaced with something lighter
 */
class FilterViewModel : ViewModel() {
    val MAX_RANGE_KM = 50
    var range: Int = MAX_RANGE_KM
    var query: String = ""
    var location: Location? = null
    var minSalary: Int = 0
    val userRequestedFilteredResults = MutableLiveData<Boolean>()

    init {
        setDefaultValues()
    }

    fun setDefaultValues() {
        range = MAX_RANGE_KM // -1 is interpreted as +inf
        query = ""
        userRequestedFilteredResults.value = false
    }
}