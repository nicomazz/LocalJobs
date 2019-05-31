package com.esp.localjobs.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.drawableToBitmap
import com.esp.localjobs.viewModels.FilterViewModel
import com.esp.localjobs.viewModels.JobsViewModel
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.util.ArrayList

class JobsMapFragment : MapFragment(), MapboxMap.OnMapClickListener {
    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val filterViewModel: FilterViewModel by activityViewModels()

    private var markerSelected = false
    private lateinit var jobs: List<Job>
    private var mapCenterLocation: Location? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapCenterLocation = filterViewModel.getLocation(context!!)

        jobsViewModel.jobs.observe(viewLifecycleOwner, Observer { jobs ->
            this.jobs = jobs ?: listOf()
            setupMap(::onMapSetup, mapCenterLocation)
        })
    }

    /**
     * Called after getMapAsync to reload the map
     */
    private fun onMapSetup(mapboxMap: MapboxMap) {

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
            val markerDrawable = context!!.getDrawable(R.drawable.ic_location_on_blue_900_36dp)
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
                            PropertyFactory.iconOffset(arrayOf(0f, -9f))
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
                            PropertyFactory.iconOffset(arrayOf(0f, -9f))
                        )
                )
            }

            mapboxMap.addOnMapClickListener(this@JobsMapFragment)
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

            // if feature already selected do nothing
            if (selectedFeature.size > 0 && markerSelected) {
                return false
            }

            // if clicked on an empty space deselect marker
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
        iconLayer.setProperties(
            PropertyFactory.iconSize(2f)
        )
        markerSelected = true
    }

    private fun deselectMarker(iconLayer: SymbolLayer) {
        iconLayer.setProperties(
            PropertyFactory.iconSize(1f)
        )
        markerSelected = false
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxMap.removeOnMapClickListener(this)
    }
}