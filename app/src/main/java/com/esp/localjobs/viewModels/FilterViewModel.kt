package com.esp.localjobs.viewModels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.utils.PositionManager

/**
 * Shared view model between filter, jobs and proposals fragment.
 * When the user asks to filter results @variable userRequestedFilterResults is set to true.
 *
 * Will probably be replaced with something lighter
 */
class FilterViewModel : ViewModel() {
    val MAX_RANGE_KM = 100
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

    /**
     * If current location is null (e.g. user didn't select a location) then retrieve last known position and return it.
     * Last known position can be null in edge cases, like after a factory reset.
     */
    fun getLocation(context: Context): Location? {
        if (location == null) {
            val l = PositionManager.getLastKnownPosition(context)
            l?.let { location = Location(it.latitude, it.longitude) }
        }
        return location
    }
}