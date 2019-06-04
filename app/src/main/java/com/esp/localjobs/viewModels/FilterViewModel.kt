package com.esp.localjobs.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.utils.PositionManager
import com.esp.localjobs.utils.Utils

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
    var filteringJobs: Boolean = true // used to load jobs or proposal

    init {
        setDefaultValues()
    }

    fun setDefaultValues() {
        range = MAX_RANGE_KM // -1 is interpreted as +inf
        query = ""
    }

    /**
     * If current location is null (e.g. user didn't select a location) then retrieve last known position and return it.
     * Last known position can be null in edge cases, like after a factory reset.
     */
    fun getLocation(context: Context): Location? {
        if (location == null) {
            PositionManager.getLastKnownPosition(context)?.let {
                val city = Utils.coordinatesToCity(context, it.latitude, it.longitude)
                location = Location(it.latitude, it.longitude, city)
            }
        }
        return location
    }
}