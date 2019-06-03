package com.esp.localjobs.fragments.map

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.utils.drawableToBitmap
import com.esp.localjobs.viewModels.FilterViewModel
import com.esp.localjobs.viewModels.JobsViewModel
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM
import com.mapbox.mapboxsdk.style.expressions.Expression.eq
import com.mapbox.mapboxsdk.style.expressions.Expression.get
import com.mapbox.mapboxsdk.style.expressions.Expression.literal
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.os.AsyncTask
import android.widget.TextView
import androidx.navigation.Navigation
import com.daasuu.bl.BubbleLayout
import com.esp.localjobs.fragments.JobsFragmentDirections
import java.lang.ref.WeakReference

/**
 * A fragment to display a map showing the locations of the  loaded jobs.
 * To make this I followed this example: https://docs.mapbox.com/android/maps/examples/icon-size-change-on-click/
 * @author Francesco Pham
 */
class JobsMapFragment : MapFragment(), MapboxMap.OnMapClickListener {
    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val filterViewModel: FilterViewModel by activityViewModels()

    private var jobs: List<Job> = listOf()
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
        startLocation = filterViewModel.getLocation(context)
        observeJobs()
    }

    private fun observeJobs() {
        jobsViewModel.jobs.observe(this, Observer { jobs ->
            this.jobs = jobs ?: listOf()
            setJobsInMap()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapContainer?.getMapAsync(this)
    }

    override fun onMapReady(map: MapboxMap) {
        super.onMapReady(map)
        setJobsInMap()
    }

    private fun setJobsInMap() {
        if (jobsPresentInMap()) {
            featureCollection = generateJsonSourceFromJobs()
            GenerateViewIconTask(this@JobsMapFragment, true).execute(featureCollection)
        } else {
            setupMap()
        }
    }

    private fun jobsPresentInMap() = mapboxMap?.style != null

    private fun updateSource() {
        mapboxMap?.style?.getSource(MARKER_SOURCE)?.let { source ->
            if (source is GeoJsonSource)
                source.setGeoJson(featureCollection)
        }
    }

    private fun setupMap() = mapboxMap?.run {
        setStyle(Style.MAPBOX_STREETS) { style ->

            setupSource(style)
            setupMarkerImage(style)
            setupMarkerLayer(style)
            setupInfoWindowLayer(style)

            GenerateViewIconTask(this@JobsMapFragment).execute(featureCollection)

            removeOnMapClickListener(this@JobsMapFragment)
            addOnMapClickListener(this@JobsMapFragment)
        }
    }

    private fun setupSource(loadedStyle: Style) = with(loadedStyle) {
        if (getSource(MARKER_SOURCE) == null) {
            featureCollection = generateJsonSourceFromJobs()
            addSource(GeoJsonSource(MARKER_SOURCE, featureCollection))
        }
    }

    private fun setupMarkerImage(loadedStyle: Style) = with(loadedStyle) {
        val image = drawableToBitmap(
            ContextCompat.getDrawable(context!!, R.drawable.ic_location_on_blue_900_36dp)!!
        )
        addImage(MARKER_IMAGE, image)
    }

    private fun setupMarkerLayer(loadedStyle: Style) = with(loadedStyle) {
        // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
        // middle of the icon being fixed to the coordinate point.
        if (getLayer(MARKER_LAYER) == null) {
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
                    iconOffset(arrayOf(-2f, -35f))
                )
                /* add a filter to show only when selected feature property is true */
                .withFilter(eq(get(PROPERTY_SELECTED), literal(true)))
        )
    }

    private fun generateJsonSourceFromJobs(): FeatureCollection {
        val features = FeatureCollection.fromFeatures(generateCoordinatesFeatureList(jobs))
        features.features()?.forEach {
            it.addBooleanProperty(PROPERTY_SELECTED, false)
        }
        return features
    }

    /**
     * Generate coordinates feature collection given a list of jobs
     */
    private fun generateCoordinatesFeatureList(jobs: List<Job>): List<Feature> =
        jobs.map { job ->
            Feature.fromGeometry(
                Point.fromLngLat(job.getLongitude(), job.getLatitude())
            ).apply {
                addStringProperty(PROPERTY_JOB_ID, job.id)
                addStringProperty(PROPERTY_NAME, job.title)
                addStringProperty(PROPERTY_SALARY, job.salary)
            }
        }

    /**
     * When a marker is clicked select it
     */
    override fun onMapClick(point: LatLng): Boolean = mapboxMap?.run {
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
                Navigation.findNavController(view!!)
                    .navigate(
                        R.id.action_destination_map_to_destination_job_details,
                        action.arguments
                    )
            }
        }
        true
    } ?: false

    /**
     * Invoked when the bitmaps have been generated from a view.
     */
    private fun setImageGenResults(imageMap: HashMap<String, Bitmap>) {
        mapboxMap?.getStyle { it.addImages(imageMap) }
    }

    private class GenerateViewIconTask internal constructor(
        context: JobsMapFragment,
        private val refreshSource: Boolean = false
    ) : AsyncTask<FeatureCollection, Void, HashMap<String, Bitmap>>() {

        private val contextRef: WeakReference<JobsMapFragment> = WeakReference(context)

        override fun doInBackground(vararg params: FeatureCollection): HashMap<String, Bitmap>? {
            val activity = contextRef.get()
            if (activity != null) {
                val imagesMap = HashMap<String, Bitmap>()

                val featureCollection = params[0]

                for (feature in featureCollection.features()!!) {
                    val id = feature.getStringProperty(PROPERTY_JOB_ID)
                    val name = feature.getStringProperty(PROPERTY_NAME)
                    val salary = feature.getStringProperty(PROPERTY_SALARY)
                    val bitmap = activity.generateBubbleBitmap(name, salary)
                    imagesMap[id] = bitmap
                }

                return imagesMap
            } else {
                return null
            }
        }

        override fun onPostExecute(bitmapHashMap: HashMap<String, Bitmap>?) {
            super.onPostExecute(bitmapHashMap)
            val context = contextRef.get()
            if (context != null && bitmapHashMap != null) {
                context.setImageGenResults(bitmapHashMap)
                if (refreshSource) {
                    context.updateSource()
                }
            }
        }
    }

    private fun generateBubbleBitmap(name: String, salary: String): Bitmap {
        val inflater = LayoutInflater.from(context)
        val bubbleLayout = inflater.inflate(R.layout.map_info_bubble, null) as BubbleLayout

        val titleTextView = bubbleLayout.findViewById(R.id.info_window_title) as TextView
        titleTextView.text = name

        val descriptionTextView = bubbleLayout.findViewById(R.id.info_window_description) as TextView
        if (salary.isNotEmpty())
            descriptionTextView.text = getString(R.string.salary, salary)
        else
            descriptionTextView.visibility = View.GONE

        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        bubbleLayout.measure(measureSpec, measureSpec)

        val measuredWidth = bubbleLayout.measuredWidth

        bubbleLayout.arrowPosition = (measuredWidth / 2 - 5).toFloat()

        return viewToBitmap(bubbleLayout)
    }

    /**
     * Generate a Bitmap from an Android SDK View.
     *
     * @param view the View to be drawn to a Bitmap
     * @return the generated bitmap
     */
    private fun viewToBitmap(view: View): Bitmap {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(measureSpec, measureSpec)

        val measuredWidth = view.measuredWidth
        val measuredHeight = view.measuredHeight

        view.layout(0, 0, measuredWidth, measuredHeight)
        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxMap?.removeOnMapClickListener(this)
    }
}