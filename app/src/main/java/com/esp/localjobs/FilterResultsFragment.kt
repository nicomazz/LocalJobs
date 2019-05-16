package com.esp.localjobs

import android.annotation.SuppressLint
import com.esp.localjobs.models.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SearchView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.esp.localjobs.managers.PositionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.fragment_filter_results.*

/**
 * Fragment used to set filter params (longitude, latitude, range, text)
 * TODO if user touch map stop listening for location
 */
class FilterResultsFragment : Fragment(), View.OnClickListener {
    private val args: FilterResultsFragmentArgs by navArgs()
    private lateinit var rangeTextView: TextView
    private lateinit var rangeSeekBar: SeekBar
    private lateinit var searchView: SearchView
    private var mapBoxMap: MapboxMap? = null
    private lateinit var mapView: MapView
    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        Mapbox.getInstance(activity!!.applicationContext, getString(R.string.mabBoxToken))
        return inflater.inflate(R.layout.fragment_filter_results, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rangeTextView = view.findViewById(R.id.range_value)
        rangeSeekBar = view.findViewById(R.id.range_seek_bar)

        // I'm not observing values to avoid loosing changes on screen rotation
        updateView()

        startObservingPosition()

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener(this)

        rangeSeekBar.max = filterViewModel.MAX_RANGE_KM
        rangeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                rangeTextView.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val resetDefaultButton = view.findViewById<Button>(R.id.reset_default_button)
        resetDefaultButton.setOnClickListener(this)

        val centerUserPositionButton = view.findViewById<ImageView>(R.id.center_user_position_button)
        centerUserPositionButton.setOnClickListener(this)

        mapView = view.findViewById(R.id.map_view)
        mapView.setOnTouchListener { v, _ ->
            this.onClick(v)
            false // false: event not consumed
        }
        mapView.onCreate(savedInstanceState)
        setupMapView()
    }

    private fun updateView() {
        rangeTextView.text = filterViewModel.range.toString()
        rangeSeekBar.progress = filterViewModel.range
    }

    private fun updateViewModel() {
        // get location coordinates of the center of the map-view
        if (mapBoxMap != null) {
            val latLng = (mapBoxMap as MapboxMap).cameraPosition.target
            filterViewModel.location = Location(latLng.latitude, latLng.longitude)
        } else
            filterViewModel.location = null
        filterViewModel.query = searchView.query.toString()
        filterViewModel.range = rangeTextView.text.toString().toInt()
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
     * TODO: add user location marker, see https://docs.mapbox.com/help/interactive-tools/marker-playground/
     */
    private fun setupMapView() {
        mapView.getMapAsync { map ->
            mapBoxMap = map
            map.setStyle(Style.MAPBOX_STREETS) { }
        }
    }

    /**
     * Handle view items click
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab -> onSearchClick()
            R.id.map_view -> stopObservingPosition()
            R.id.center_user_position_button -> startObservingPosition()
            R.id.reset_default_button -> {
                filterViewModel.setDefaultValues()
                updateView()
            }
        }
    }

    private fun stopObservingPosition() {
        hovering_marker.setImageResource(R.drawable.ic_location_on_red_900_36dp)
        PositionManager.getInstance(context!!).currentBestLocation.removeObserver(locationObserver)
    }

    private fun startObservingPosition() {
        hovering_marker.setImageResource(R.drawable.ic_location_on_blue_900_36dp)
        PositionManager.getInstance(context!!).apply {
            startListeningForPosition()
            currentBestLocation.observe(viewLifecycleOwner, locationObserver)
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

    /**
     * Center the map on user location - if location is null hide location marker
     */
    private val locationObserver = Observer<android.location.Location?> { newLocation ->
        hovering_marker.visibility = if (newLocation == null) View.INVISIBLE else View.VISIBLE
        newLocation?.let {
            // center view on user location
            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(
                    newLocation.latitude,
                    newLocation.longitude))
                .zoom(16.0)
                .bearing(180.0)
                .tilt(30.0)
                .build()
            mapBoxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 3000)
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
}
