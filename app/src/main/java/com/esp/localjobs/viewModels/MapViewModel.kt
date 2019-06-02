package com.esp.localjobs.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.models.Location

class MapViewModel : ViewModel() {

    private val _location = MutableLiveData<Location?>()

    val location: LiveData<Location?>
        get() = _location

    fun setLocation(newLocation: Location) = _location.postValue(newLocation)
}