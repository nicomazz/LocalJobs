package com.esp.localjobs.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esp.localjobs.data.models.Location

class MapViewModel : ViewModel() {

    private val _location = MutableLiveData<Location?>()
    private val _metersPerPixel = MutableLiveData<Double?>()

    val location: LiveData<Location?>
        get() = _location

    val metersPerPixel: LiveData<Double?>
        get() = _metersPerPixel

    fun setLocation(newLocation: Location) = _location.postValue(newLocation)

    fun setMetersPerPixel(metersPerPixel: Double) = _metersPerPixel.postValue(metersPerPixel)
}