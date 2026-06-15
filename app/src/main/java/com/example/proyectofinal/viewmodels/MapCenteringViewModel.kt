package com.example.proyectofinal.viewmodels

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MapCenteringViewModel : ViewModel() {
    private val _centerEvent = MutableSharedFlow<LatLng>(extraBufferCapacity = 1)
    val centerEvent: SharedFlow<LatLng> = _centerEvent.asSharedFlow()

    fun emitCenter(latLng: LatLng) {
        _centerEvent.tryEmit(latLng)
    }
}
