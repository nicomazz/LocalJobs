package com.esp.localjobs

import com.esp.localjobs.models.Location
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SearchView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

/**
 * Fragment used to set filter params (longitude, latitude, range, text)
 * TODO dialog on back button pressed: discard filter options?
 * TODO add place picker
 */
class FilterResultsFragment : Fragment() {
    private val args: FilterResultsFragmentArgs by navArgs()
    private lateinit var rangeTextView: TextView
    private lateinit var rangeSeekBar: SeekBar
    private lateinit var searchView: SearchView
    private lateinit var mapBoxMap: MapboxMap
    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        Mapbox.getInstance(activity!!.applicationContext, "pk.eyJ1IjoibHVjYW1vcm8iLCJhIjoiY2p2aWZpMjF3MDUydDQ4cnV1aXNoajg3aCJ9.oHLaI83cBQT1TzikU6oA8Q")
        return inflater.inflate(R.layout.fragment_filter_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rangeTextView = view.findViewById(R.id.range_value)
        rangeSeekBar = view.findViewById(R.id.range_seek_bar)

        // I'm not observing values to avoid loosing changes on screen rotation
        updateView()

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { onSearchClick() }

        rangeSeekBar.max = filterViewModel.MAX_RANGE_KM
        rangeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                rangeTextView.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val resetDefaultButton = view.findViewById<Button>(R.id.reset_default_button)
        resetDefaultButton.setOnClickListener {
            filterViewModel.setDefaultValues()
            updateView()
        }

        val mapView = view.findViewById<MapView>(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        setupMapView(mapView)
    }

    private fun updateView() {
        rangeTextView.text = filterViewModel.range.value.toString()
        rangeSeekBar.progress = filterViewModel.range.value ?: -1
    }

    private fun updateViewModel() {
        // get location coordinates of the center of the map-view
        val mapTargetLatLng = mapBoxMap.cameraPosition.target
        filterViewModel.location.value = Location(mapTargetLatLng.latitude, mapTargetLatLng.longitude)
        filterViewModel.query.value = searchView.query.toString()
        filterViewModel.range.value = rangeTextView.text.toString().toInt()
        filterViewModel.location.value = Location(0.0, 0.0)
    }

    /**
     * Update filter viewmodel and navigate back to the calling fragment.
     * Set filterViewModel.userRequestedFilteredResults to true to notify the fragments that the user requested
     * a filtered search.
     */
    private fun onSearchClick() {
        updateViewModel()
        filterViewModel.userRequestedFilteredResults.value = true
        if (args.filteringJobs)
            findNavController().navigate(R.id.action_destination_filter_to_destination_jobs)
        else
            findNavController().navigate(R.id.action_destination_filter_to_destination_proposals)
    }

    /**
     * Get map-view and create an hovering marker at the center of the map.
     */
    private fun setupMapView(mapView: MapView) {
        mapView.getMapAsync { map ->
            mapBoxMap = map

            mapBoxMap.setStyle(Style.MAPBOX_STREETS) {
                // add central position marker
                val hoveringMarker = ImageView(context)
                hoveringMarker.setImageResource(R.drawable.ic_location_on_red_900_36dp)
                val params = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
                hoveringMarker.layoutParams = params
                mapView.addView(hoveringMarker)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_search, menu)
        searchView = menu.findItem(R.id.action_search_item).actionView as SearchView
        setupSearchView()
    }

    /**
     * Setup search view icon.
     * The search view is expanded by default and focused on fragment creation.
     */
    private fun setupSearchView() {
        searchView.setIconifiedByDefault(false) // expand search view
        searchView.requestFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                onSearchClick()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean { return true }
        })
    }
}
