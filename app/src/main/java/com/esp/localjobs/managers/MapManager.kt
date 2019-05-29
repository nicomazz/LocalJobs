package com.esp.localjobs.managers

import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.esp.localjobs.R
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import android.content.Context
import android.widget.Toast
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.drawableToBitmap
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Feature
import java.util.ArrayList
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset
import android.animation.ValueAnimator

class MapManager(private val context: Context, private val mapView: MapView) :
    OnMapReadyCallback, MapboxMap.OnMapClickListener {

    var mapCenterLocation: Location? = null
    private lateinit var jobs: List<Job>
    private lateinit var mapboxMap: MapboxMap
    private var markerSelected = false

    /**
     * Reload map showing the markers of the jobs's locations
     * @param jobs Jobs which locations should be pinned on the map
     */
    fun update(jobs: List<Job>) {
        this.jobs = jobs
        mapView.getMapAsync(this)
    }

    /**
     * Called after getMapAsync to reload the map
     */
    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        // disable tilt and rotate gestures
        mapboxMap.uiSettings.isRotateGesturesEnabled = false
        mapboxMap.uiSettings.isTiltGesturesEnabled = false

        mapboxMap.style?.let { style ->
            // if style is already set update only the markers
            val markerCoordinates = generateCoordinatesFeatureList(jobs)
            val jsonSource = FeatureCollection.fromFeatures(markerCoordinates)
            style.getSource("marker-source")?.let { source ->
                if (source is GeoJsonSource)
                    source.setGeoJson(jsonSource)
            }
        } ?: mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->

            // add coordinates source
            if (style.getSource("marker-source") == null) {
                val markerCoordinates = generateCoordinatesFeatureList(jobs)
                val jsonSource = FeatureCollection.fromFeatures(markerCoordinates)
                style.addSource(GeoJsonSource("marker-source", jsonSource))
            }

            // Add the marker image to map
            // val markerImage = BitmapFactory.decodeResource(
            // context.resources, R.drawable.ic_location_on_blue_900_36dp) <-- this returns null idk why
            val markerDrawable = context.getDrawable(R.drawable.ic_location_on_blue_900_36dp)
            if (markerDrawable != null) {
                val markerImage = drawableToBitmap(markerDrawable)
                style.addImage("my-marker-image", markerImage)
            }

            // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
            // middle of the icon being fixed to the coordinate point.
            if (style.getLayer("marker-layer") == null) {
                style.addLayer(
                    SymbolLayer("marker-layer", "marker-source")
                        .withProperties(
                            PropertyFactory.iconImage("my-marker-image"),
                            iconOffset(arrayOf(0f, -9f))
                        )
                )
            }

            // Add the selected marker source and layer
            if (style.getSource("selected-marker") == null)
                style.addSource(GeoJsonSource("selected-marker"))

            // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
            // middle of the icon being fixed to the coordinate point.
            if (style.getLayer("selected-marker-layer") == null) {
                style.addLayer(
                    SymbolLayer("selected-marker-layer", "selected-marker")
                        .withProperties(
                            PropertyFactory.iconImage("my-marker-image"),
                            iconOffset(arrayOf(0f, -9f))
                        )
                )
            }

            mapboxMap.addOnMapClickListener(this@MapManager)

            centerMap()
        }
    }

    /**
     * Generate coordinates feature collection given a list of jobs
     */
    private fun generateCoordinatesFeatureList(jobs: List<Job>): ArrayList<Feature> {
        val markerCoordinates = ArrayList<Feature>()
        jobs.forEach { job ->
            val latitude = job.l[0]
            val longitude = job.l[1]
            if (latitude != null && longitude != null)
                markerCoordinates.add(
                    Feature.fromGeometry(
                        Point.fromLngLat(longitude, latitude)
                    )
                )
        }
        return markerCoordinates
    }

    /**
     * When a marker is clicked highlight it
     */
    override fun onMapClick(point: LatLng): Boolean {
        mapboxMap.style?.let { style ->
            val selectedMarkerSymbolLayer = style.getLayer("selected-marker-layer") as SymbolLayer

            val pixel = mapboxMap.projection.toScreenLocation(point)
            val features = mapboxMap.queryRenderedFeatures(pixel, "marker-layer")
            val selectedFeature = mapboxMap.queryRenderedFeatures(
                pixel, "selected-marker-layer"
            )

            if (selectedFeature.size > 0 && markerSelected) {
                return false
            }

            if (features.isEmpty()) {
                if (markerSelected) {
                    deselectMarker(selectedMarkerSymbolLayer)
                }
                return false
            }

            val source = style.getSourceAs<GeoJsonSource>("selected-marker")
            source?.setGeoJson(
                FeatureCollection.fromFeatures(
                    arrayOf(Feature.fromGeometry(features[0].geometry()))
                )
            )

            if (markerSelected) {
                deselectMarker(selectedMarkerSymbolLayer)
            }
            if (features.size > 0) {
                selectMarker(selectedMarkerSymbolLayer)
            }
        }
        return true
    }

    /**
     * Highlight a marker
     */
    private fun selectMarker(iconLayer: SymbolLayer) {
        val markerAnimator = ValueAnimator()
        markerAnimator.setObjectValues(1f, 2f)
        markerAnimator.duration = 300
        markerAnimator.addUpdateListener { animator ->
            iconLayer.setProperties(
                PropertyFactory.iconSize(animator.animatedValue as Float)
            )
        }
        markerAnimator.start()
        markerSelected = true
    }

    private fun deselectMarker(iconLayer: SymbolLayer) {
        val markerAnimator = ValueAnimator()
        markerAnimator.setObjectValues(2f, 1f)
        markerAnimator.duration = 300
        markerAnimator.addUpdateListener { animator ->
            iconLayer.setProperties(
                PropertyFactory.iconSize(animator.animatedValue as Float)
            )
        }
        markerAnimator.start()
        markerSelected = false
    }

    /**
     * If the last known position of the device is not null, center the map view on it
     */
    private fun centerMap() {
        var targetLocation = mapCenterLocation
        if (targetLocation == null) {
            val location = PositionManager.getInstance(context).getLastKnownPosition()
            if (location == null) {
                Toast.makeText(context, "Last position unknown", Toast.LENGTH_LONG).show()
                return
            }
            targetLocation = Location(location.latitude, location.longitude)
        }
        navigateToPosition(targetLocation)
    }

    /**
     * Center the map view on given location.
     */
    private fun navigateToPosition(location: Location) {
        // center view on user location
        val cameraPosition = CameraPosition.Builder()
            .target(
                LatLng(
                    location.latitude,
                    location.longitude)
            )
            .zoom(12.0)
            .build()
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000)
    }

    fun onDestroy() {
        mapboxMap.removeOnMapClickListener(this)
    }
}