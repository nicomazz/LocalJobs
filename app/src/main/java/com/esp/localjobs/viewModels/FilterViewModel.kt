package com.esp.localjobs.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esp.localjobs.LocalJobsApplication
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.utils.GeocodingUtils
import com.esp.localjobs.utils.PositionManager

/**
 * Shared view model between filter, jobs and proposals fragment.
 * When the user asks to filter results @variable userRequestedFilterResults is set to true.
 *
 * Will probably be replaced with something lighter
 */
class FilterViewModel : ViewModel() {

    private val _activeFilters = MutableLiveData<Filters>()
    val activeFilters: LiveData<Filters>
        get() = _activeFilters

    val range: Int
        get() = activeFilters.value?.range ?: MAX_RANGE_KM

    val location: Location?
        get() = activeFilters.value?.location

    init {
        val context = LocalJobsApplication.applicationContext()
        PositionManager.getLastKnownPosition(context)?.let {
            val city = GeocodingUtils.coordinatesToCity(context, it.latitude, it.longitude)
            val location = Location(it.latitude, it.longitude, city)
            setFilters(Filters(location = location))
        } ?: setFilters(Filters())
    }

    fun setFilters(newfilters: Filters) {
        _activeFilters.postValue(newfilters)
    }

    fun setQuery(newQuery: String) {
        _activeFilters.postValue(
            activeFilters.value!!.apply {
                query = newQuery
            }
        )
    }
}

const val MAX_RANGE_KM = 100

data class Filters(
    var range: Int = MAX_RANGE_KM,
    var query: String = "",
    var location: Location? = null,
    var minSalary: Int = 0,
    var filteringJobs: Boolean = true // used to load jobs or proposal
)