package com.esp.localjobs.fragments.map

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.daasuu.bl.BubbleLayout
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.fragments.JobsFragmentDirections
import com.esp.localjobs.utils.BitmapUtils
import com.esp.localjobs.viewModels.FilterViewModel
import com.esp.localjobs.viewModels.JobsViewModel
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.eq
import com.mapbox.mapboxsdk.style.expressions.Expression.get
import com.mapbox.mapboxsdk.style.expressions.Expression.literal
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * A fragment to display a map showing the locations of the  loaded jobs.
 * @author Francesco Pham
 */
open class JobsMapFragment : MapFragment(), MapboxMap.OnMapClickListener, CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Dispatchers.Default

    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val filterViewModel: FilterViewModel by activityViewModels()

    private lateinit var jobs: List<Job>
    private var featureCollection: FeatureCollection? = null

    private companion object Map {
        const val MARKER_SOURCE = "MARKER_SOURCE"
        const val MARKER_IMAGE = "MARKER_IMAGE"
        const val MARKER_LAYER = "MARKER_LAYER"
        const val PROPERTY_JOB_ID = "PROPERTY_JOB_ID"
        const val CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID"
        const val PROPERTY_SELECTED = "PROPERTY_SELECTED"
        const val PROPERTY_NAME = "PROPERTY_NAME"
        const val PROPERTY_SALARY = "PROPERTY_SALARY"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        startLocation = provideStartLocation()
        jobs = getJobsToDisplay()
    }

    open fun provideStartLocation() = filterViewModel.location

    open fun getJobsToDisplay(): List<Job> {
        return jobsViewModel.jobs.value ?: listOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxMap.removeOnMapClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_navigation, menu)
        for (i in 0.until(menu.size()))
            menu.getItem(i).isVisible = false
    }

    override fun onMapReady(map: MapboxMap) {
        super.onMapReady(map)
        setupMap()
    }

    private fun setupMap() = mapboxMap.run {
        setStyle(Style.MAPBOX_STREETS) { style ->

            setupSource(style)
            setupMarkerImage(style)
            setupMarkerLayer(style)
            setupInfoWindowLayer(style)

            launch {
                generateViewIcon(featureCollection!!, false)
            }

            removeOnMapClickListener(this@JobsMapFragment)
            addOnMapClickListener(this@JobsMapFragment)
        }
    }

    private fun setupSource(loadedStyle: Style) = with(loadedStyle) {
        featureCollection = generateJsonSourceFromJobs()
        addSource(GeoJsonSource(MARKER_SOURCE, featureCollection))
    }

    private fun updateSource() {
        mapboxMap.style?.getSource(MARKER_SOURCE)?.let { source ->
            if (source is GeoJsonSource)
                source.setGeoJson(featureCollection)
        }
    }

    private fun generateJsonSourceFromJobs(): FeatureCollection {
        val featureList = jobs.map { job ->
            Feature.fromGeometry(
                Point.fromLngLat(job.longitude(), job.latitude())
            ).apply {
                addStringProperty(PROPERTY_JOB_ID, job.id)
                addStringProperty(PROPERTY_NAME, job.title)
                addStringProperty(PROPERTY_SALARY, job.salary.toString())
                addBooleanProperty(PROPERTY_SELECTED, false)
            }
        }
        return FeatureCollection.fromFeatures(featureList)
    }

    private fun setupMarkerImage(loadedStyle: Style) = with(loadedStyle) {
        val image = BitmapUtils.drawableToBitmap(
            ContextCompat.getDrawable(context!!, R.drawable.ic_location_on_blue_900_36dp)!!
        )
        addImage(MARKER_IMAGE, image)
    }

    private fun setupMarkerLayer(loadedStyle: Style) = with(loadedStyle) {
        // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
        // middle of the icon being fixed to the coordinate point.
        addLayer(
            SymbolLayer(
                MARKER_LAYER,
                MARKER_SOURCE
            ).withProperties(
                iconImage(MARKER_IMAGE),
                iconOffset(arrayOf(0f, -9f))
            )
        )
    }

    private fun setupInfoWindowLayer(loadedStyle: Style) {
        loadedStyle.addLayer(
            SymbolLayer(CALLOUT_LAYER_ID, MARKER_SOURCE)
                .withProperties(
                    /* show image with id title based on the value of the id feature property */
                    iconImage("{PROPERTY_JOB_ID}"),

                    /* set anchor of icon to bottom-left */
                    iconAnchor(ICON_ANCHOR_BOTTOM),

                    /* all info window and marker image to appear at the same time*/
                    iconAllowOverlap(true),

                    /* offset the info window to be above the marker */
                    iconOffset(arrayOf(-2f, -32f))
                )
                /* add a filter to show only when selected feature property is true */
                .withFilter(eq(get(PROPERTY_SELECTED), literal(true)))
        )
    }

    /**
     * When a marker is clicked select it
     */
    override fun onMapClick(point: LatLng): Boolean = mapboxMap.run {
        style?.let { _ ->
            val pixel = projection.toScreenLocation(point)
            val markerFeatures = queryRenderedFeatures(
                pixel,
                MARKER_LAYER
            )

            if (markerFeatures.isNotEmpty()) markerFeatures.first().let { selectedFeature ->
                val jobIdSelected = selectedFeature.getStringProperty(PROPERTY_JOB_ID)

                // search feature inside collection and toggle selection
                featureCollection?.features()?.first {
                    jobIdSelected == it.getStringProperty(PROPERTY_JOB_ID)
                }?.let {
                    val isSelected = it.getBooleanProperty(PROPERTY_SELECTED)
                    it.properties()?.addProperty(PROPERTY_SELECTED, !isSelected)
                    updateSource()
                }
            }

            val bubbleFeatures = queryRenderedFeatures(
                pixel,
                CALLOUT_LAYER_ID
            )

            if (bubbleFeatures.isNotEmpty()) bubbleFeatures.first().let { feature ->
                val jobIdSelected = feature.getStringProperty(PROPERTY_JOB_ID)

                // navigate to JodDetailsFragment
                val selectedJob = jobs.first { it.id == jobIdSelected }
                val action =
                    JobsFragmentDirections.actionDestinationJobsToDestinationJobDetails(selectedJob)
                try {
                    findNavController().navigate(
                        R.id.action_destination_map_to_destination_job_details,
                        action.arguments
                    )
                } catch (e: IllegalArgumentException) {
                    // it happens when called from singlejobmap
                    e.printStackTrace()
                }
            }
        }
        false
    }

    /**
     * Invoked when the bitmaps have been generated from a view.
     */
    private fun setImageGenResults(imageMap: HashMap<String, Bitmap>) {
        mapboxMap.getStyle { it.addImages(imageMap) }
    }

    private fun generateViewIcon(featureCollection: FeatureCollection, refreshSource: Boolean) {
        val imagesMap = HashMap<String, Bitmap>()

        val inflater = LayoutInflater.from(context)
        val bubbleView = inflater.inflate(R.layout.map_info_bubble, null) as BubbleLayout

        for (feature in featureCollection.features()!!) {
            val id = feature.getStringProperty(PROPERTY_JOB_ID)
            val name = feature.getStringProperty(PROPERTY_NAME)
            val salary = feature.getStringProperty(PROPERTY_SALARY)
            val bitmap = generateBubbleBitmap(bubbleView, name, salary)
            imagesMap[id] = bitmap
        }
        CoroutineScope(Dispatchers.Main).launch {
            setImageGenResults(imagesMap)
            if (refreshSource) {
                updateSource()
            }
        }
    }

    private fun generateBubbleBitmap(bubbleView: BubbleLayout, name: String, salary: String): Bitmap {
        val titleTextView = bubbleView.findViewById(R.id.info_window_title) as TextView
        titleTextView.text = name

        val descriptionTextView = bubbleView.findViewById(R.id.info_window_description) as TextView
        if (salary.isNotEmpty())
            descriptionTextView.text = getString(R.string.salary, salary)
        else
            descriptionTextView.visibility = View.GONE

        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        bubbleView.measure(measureSpec, measureSpec)

        val measuredWidth = bubbleView.measuredWidth

        bubbleView.arrowPosition = (measuredWidth / 2 - 5).toFloat()

        return BitmapUtils.viewToBitmap(bubbleView)
    }
}