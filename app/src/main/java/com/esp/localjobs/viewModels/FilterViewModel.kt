package com.esp.localjobs.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esp.localjobs.LocalJobsApplication
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.data.repository.JobsRepository.JobFilter
import com.esp.localjobs.data.repository.MAX_RANGE_KM
import com.esp.localjobs.utils.GeocodingUtils
import com.esp.localjobs.utils.PositionManager
import kotlinx.coroutines.launch

/**
 * Shared view model between filter, jobs and proposals fragment.
 * When the user asks to filter results @variable userRequestedFilterResults is set to true.
 *
 * Will probably be replaced with something lighter
 */
class FilterViewModel : ViewModel() {

    private val _activeFilters = MutableLiveData<JobFilter>()
    val activeFilters: LiveData<JobFilter>
        get() = _activeFilters

    val range: Int
        get() = activeFilters.value?.range ?: MAX_RANGE_KM

    val location: Location?
        get() = activeFilters.value?.location

    val salary: Float?
        get() = activeFilters.value?.salary

    val query: String?
        get() = activeFilters.value?.query

    init {
        val context = LocalJobsApplication.applicationContext()
        val filter = retrieveLastUsedFilter(context)
        setFilters(filter)

        if (filter.location == null)
            initializeLocation(context)
    }

    private fun initializeLocation(context: Context) = viewModelScope.launch {
        PositionManager.getLastKnownPosition(context)?.let {
            val city = GeocodingUtils.coordinatesToCity(context, it.latitude, it.longitude)
            val location = Location(it.latitude, it.longitude, city)
            setLocation(location)
        }
    }

    fun setFilters(newFilters: JobFilter) {
        _activeFilters.postValue(newFilters)
        updateLastUsedFilter(LocalJobsApplication.applicationContext(), newFilters)
    }

    fun setQuery(newQuery: String) {
        _activeFilters.postValue(
            activeFilters.value!!.apply {
                query = newQuery
            }
        )
    }

    fun setLocation(newLocation: Location) {
        _activeFilters.postValue(
            activeFilters.value!!.apply {
                location = newLocation
            }
        )
    }

    private fun retrieveLastUsedFilter(context: Context): JobFilter {
        val sharedPref = context.getSharedPreferences("filter", Context.MODE_PRIVATE)
        with(sharedPref) {
            return JobFilter().also {
                it.filteringJobs = getBoolean("filteringJobs", true)
                it.range = getInt("range", MAX_RANGE_KM)
                val latitude = getString("latitude", null)?.toDoubleOrNull()
                val longitude = getString("longitude", null)?.toDoubleOrNull()
                val city = getString("city", context.getString(R.string.unknown_location))
                if (latitude != null && longitude != null)
                    it.location = Location(latitude, longitude, city)
            }
        }
    }

    private fun updateLastUsedFilter(context: Context, newFilter: JobFilter) {
        val sharedPreferences = context.getSharedPreferences("filter", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("filteringJobs", newFilter.filteringJobs)
            putInt("range", newFilter.range)
            newFilter.location?.let {
                val (lat, lng) = it.latLng()
                putString("latitude", lat.toString())
                putString("longitude", lng.toString())
                putString("city", it.city)
            }
            commit()
        }
    }
}
