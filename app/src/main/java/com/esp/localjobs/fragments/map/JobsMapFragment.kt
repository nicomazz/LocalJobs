package com.esp.localjobs.fragments.map

import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.utils.drawableToBitmap
import com.esp.localjobs.viewModels.FilterViewModel
import com.esp.localjobs.viewModels.JobsViewModel
import com.esp.localjobs.viewModels.MapViewModel
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

/**
 * A fragment to display a map showing the locations of the  loaded jobs.
 * To make this I followed this example: https://docs.mapbox.com/android/maps/examples/icon-size-change-on-click/
 * @author Francesco Pham
 */
class JobsMapFragment : MapFragment(), MapboxMap.OnMapClickListener {
    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val filterViewModel: FilterViewModel by activityViewModels()
    private val mapViewModel: MapViewModel by activityViewModels()

    private var markerSelected = false
    private var jobs: List<Job> = listOf()

    private companion object Map {
        const val MARKER_SOURCE = "marker-source"
        const val MARKER_IMAGE = "marker-image"
        const val MARKER_LAYER = "marker-layer"
        const val SELECTED_MARKER = "selected-marker"
        const val SELECTED_MARKER_LAYER = "selected-marker-layer"
        const val JOB_ID_PROPERTY = "job_id"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        startLocation = filterViewModel.getLocation(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        jobsViewModel.jobs.observe(viewLifecycleOwner, Observer { jobs ->
            this.jobs = jobs ?: listOf()
            mapContainer.getMapAsync(this)
        })
    }

    override fun onMapReady(map: MapboxMap) {
        super.onMapReady(map)
        setJobsInMap()
    }

    private fun setJobsInMap() {
        if (jobsPresentInMap()) {
            updateJobsInMap()
        } else {
            setupJobsInMap()
        }
    }

    private fun jobsPresentInMap() = mapboxMap.style != null

    private fun updateJobsInMap() {
        mapboxMap.style?.getSource(MARKER_SOURCE)?.let { source ->
            if (source is GeoJsonSource)
                source.setGeoJson(generateJsonSourceFromJobs())
        }
    }

    private fun setupJobsInMap() = with(mapboxMap) {
        setStyle(Style.MAPBOX_STREETS) { style ->

            // add coordinates source
            if (style.getSource(MARKER_SOURCE) == null) {
                style.addSource(GeoJsonSource(MARKER_SOURCE, generateJsonSourceFromJobs()))
            }

            style.addImage(MARKER_IMAGE, getMarkerIcon())

            // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
            // middle of the icon being fixed to the coordinate point.
            if (style.getLayer(MARKER_LAYER) == null) {
                style.addLayer(
                    SymbolLayer(
                        MARKER_LAYER,
                        MARKER_SOURCE
                    ).withProperties(
                        PropertyFactory.iconImage(MARKER_IMAGE),
                        PropertyFactory.iconOffset(arrayOf(0f, -9f))
                    )
                )
            }

            // Add the selected marker source and layer
            if (style.getSource(SELECTED_MARKER) == null)
                style.addSource(GeoJsonSource(SELECTED_MARKER))

            // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
            // middle of the icon being fixed to the coordinate point.
            if (style.getLayer(SELECTED_MARKER_LAYER) == null) {
                style.addLayer(
                    SymbolLayer(
                        SELECTED_MARKER_LAYER,
                        SELECTED_MARKER
                    )
                        .withProperties(
                            PropertyFactory.iconImage(MARKER_IMAGE),
                            PropertyFactory.iconOffset(arrayOf(0f, -9f))
                        )
                )
            }
            removeOnMapClickListener(this@JobsMapFragment)
            addOnMapClickListener(this@JobsMapFragment)
        }
    }

    private fun generateJsonSourceFromJobs() = FeatureCollection.fromFeatures(generateCoordinatesFeatureList(jobs))

    private fun getMarkerIcon() =
        drawableToBitmap(ContextCompat.getDrawable(context!!, R.drawable.ic_location_on_blue_900_36dp)!!)

    /**
     * Generate coordinates feature collection given a list of jobs
     */
    private fun generateCoordinatesFeatureList(jobs: List<Job>): List<Feature> =
        jobs.map { job ->
            Feature.fromGeometry(
                Point.fromLngLat(job.getLongitude(), job.getLatitude())
            ).apply {
                addStringProperty(JOB_ID_PROPERTY, job.uid)
            }
        }

    /**
     * When a marker is clicked select it
     */
    override fun onMapClick(point: LatLng): Boolean {
        mapboxMap.style?.let { style ->
            val selectedMarkerSymbolLayer = style.getLayer(SELECTED_MARKER_LAYER) as SymbolLayer

            val pixel = mapboxMap.projection.toScreenLocation(point)
            val features = mapboxMap.queryRenderedFeatures(
                pixel,
                MARKER_LAYER
            )
            val selectedFeature = mapboxMap.queryRenderedFeatures(
                pixel, SELECTED_MARKER_LAYER
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

            val source = style.getSourceAs<GeoJsonSource>(SELECTED_MARKER)
            source?.setGeoJson(
                FeatureCollection.fromFeatures(
                    arrayOf(Feature.fromGeometry(features[0].geometry()))
                )
            )

            if (markerSelected) {
                deselectMarker(selectedMarkerSymbolLayer)
            }
            if (features.size > 0) {
                val selectedJobUid = features.first().getStringProperty(JOB_ID_PROPERTY)
                jobs.firstOrNull { it.uid == selectedJobUid }?.let { selected_job ->
                    mapViewModel.setSelectedJob(selected_job)
                }
                selectMarker(selectedMarkerSymbolLayer)
            }
        }
        return true
    }

    /**
     * Select a marker by making it larger
     */
    private fun selectMarker(iconLayer: SymbolLayer) {
        iconLayer.setProperties(
            PropertyFactory.iconSize(1.5f)
        )

        markerSelected = true
    }

    /**
     * Deselect a marker by restoring the original size
     */
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