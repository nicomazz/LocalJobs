package com.esp.localjobs.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.core.view.forEach
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.models.Localizable
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.fragments.map.LocationPickerFragment
import com.esp.localjobs.utils.AnimationsUtils
import com.esp.localjobs.utils.LoadingViewDialog
import com.esp.localjobs.viewModels.EditViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_edit.*
import kotlinx.android.synthetic.main.fragment_edit.description_edit_text
import kotlinx.android.synthetic.main.fragment_edit.location_edit_text
import kotlinx.android.synthetic.main.fragment_edit.range_seekbar
import kotlinx.android.synthetic.main.fragment_edit.salary_edit_text
import kotlinx.android.synthetic.main.fragment_edit.title_edit_text
import kotlinx.android.synthetic.main.fragment_job_details.*

class EditFragment : Fragment(), LocationPickerFragment.OnLocationPickedListener {
//    private val args: EditFragmentArgs by navArgs()
    private val editViewModel: EditViewModel by activityViewModels()
    private var selectedLocation: Location? = null
    private lateinit var viewDialog: LoadingViewDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        setupBackAnimations()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        with(args.job) {
//            selectedLocation = Location(latitude(), longitude(), city)
//        }
        viewDialog = LoadingViewDialog(activity!!)
        setupDistanceSeekbarUI()
        setupLocationEditTextUI()
        setupRadioButton()
        setView() // must be called after setups
        delete_button.setOnClickListener { onDeleteClick() }
        AnimationsUtils.popup(delete_button, 400)
        startPostponedEnterTransition()
    }

    private fun setView() {
//        with(args.job) {
        with(Job()) {
            title_edit_text.setText(title)
            location_edit_text.setText(city)
            salary_edit_text.setText(salary?.toString() ?: "")
            description_edit_text.setText(description)

            val isJob = itIsJob
            if (isJob == false)
                edit_type_radio_group.check(R.id.radio_proposal)
            range?.let {
                range_seekbar.progress = it
            }
        }
    }

    private fun setupBackAnimations() {
        requireActivity().onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    prepareUiToGoBack()
                }
                // Handle the back button event
            })
    }

    private fun prepareUiToGoBack() {
        AnimationsUtils.popout(delete_button) {
            findNavController().popBackStack()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.forEach { it.isVisible = false }
        inflater.inflate(R.menu.menu_save, menu)
        val saveItem = menu.findItem(R.id.menu_save_item)
        saveItem.setOnMenuItemClickListener {
            onSaveClick()
            true
        }
    }

    /**
     * Update modified fields of the job
     */
    private fun onSaveClick() {
        val location = selectedLocation
        if (!validateForm() || location == null)
            return

        val newJob = parseJobFromView(location)

//        if (newJob == args.job) {
//            Snackbar.make(
//                activity!!.findViewById<View>(android.R.id.content),
//                getString(R.string.edit_no_changes_detected),
//                Snackbar.LENGTH_SHORT
//            ).show()
//            return
//        }

        // called after completion of add task
        val onItemEditSuccess: () -> Unit = {
            viewDialog.hideDialog()
            Snackbar.make(
                activity!!.findViewById<View>(android.R.id.content),
                getString(R.string.edit_job_success),
                Snackbar.LENGTH_SHORT
            ).show()

            // navigate to jobDetails showing updated job
//            val action = EditFragmentDirections.actionDestinationEditToDestinationJobDetails(newJob)
//            findNavController().navigate(action.actionId, action.arguments)
        }

        val onItemEditFailure = { e: Exception ->
            viewDialog.hideDialog()
            Snackbar.make(
                activity!!.findViewById<View>(android.R.id.content),
                getString(R.string.edit_job_failure) + e.toString(),
                Snackbar.LENGTH_SHORT
            ).show()
        }

//        Log.d(TAG, "old job: ${args.job}\nnew job: $newJob")

        viewDialog.showDialog()

        editViewModel.update(
            id = newJob.id,
            newJob = newJob,
            onSuccess = onItemEditSuccess,
            onFailure = onItemEditFailure
        )
    }

    private fun onDeleteClick() {
        val onItemDeleteSuccess: () -> Unit = {
            viewDialog.hideDialog()
            Snackbar.make(
                activity!!.findViewById<View>(android.R.id.content),
                getString(R.string.delete_job_success),
                Snackbar.LENGTH_SHORT
            ).show()
            findNavController().navigate(R.id.destination_jobs)
        }

        val onItemDeleteFailure = { e: Exception ->
            viewDialog.hideDialog()
            Snackbar.make(
                activity!!.findViewById<View>(android.R.id.content),
                getString(R.string.delete_job_failure) + e.toString(),
                Snackbar.LENGTH_SHORT
            ).show()
        }
        viewDialog.showDialog()
//        editViewModel.delete(args.job.id, onSuccess = onItemDeleteSuccess, onFailure = onItemDeleteFailure)
    }

    private fun setupDistanceSeekbarUI() {
        edit_range_value.text = getString(R.string.distance, range_seekbar.progress)
        range_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                edit_range_value.text = getString(R.string.distance, progress)
            }
        })
    }

    private fun setupRadioButton() {
        edit_type_radio_group.setOnCheckedChangeListener { _, checkedId ->
            val type = view?.findViewById<RadioButton>(checkedId)?.tag
            when (type) {
                JOB -> edit_range_div.visibility = View.GONE
                PROPOSAL -> edit_range_div.visibility = View.VISIBLE
                else -> TODO()
            }
        }
    }

    private fun setupLocationEditTextUI() {
        location_edit_text.setOnClickListener {
            fragmentManager?.let { fm ->
                LocationPickerFragment.newInstanceShow(this, fm)
            }
        }
    }

    override fun onLocationPicked(location: Location, distance: Int?) {
        val locationText =
            if (location.city != null) location.city
            else getString(R.string.coordinates, location.l[0].toString(), location.l[1].toString())
        location_edit_text.setText(locationText)
        selectedLocation = location
    }

    /**
     * Check inserted data and show errors messages if any
     */
    private fun validateForm(): Boolean {
        var anyError = false
        if (selectedLocation == null) {
            edit_location_view.error = "Please pick a location"
            anyError = true
        }
        if (title_edit_text.text.toString().isEmpty()) {
            edit_title_view.error = "Please insert a title"
            anyError = true
        }
        return !anyError
    }

    /**
     * Create a Job from the view using class variable selectedLocation, uses args.job
     * @param location position of the job
     * @return Job parsed from the view
     */
    private fun parseJobFromView(location: Localizable): Job = Job().apply {
        val userSelectedJob = edit_type_radio_group.checkedRadioButtonId == R.id.radio_job

//        id = args.job.id
//        title = title_edit_text.text.toString()
//        description = description_edit_text.text.toString()
//        l = location.latLng().toList()
//        city = location_edit_text.text.toString()
//        salary = salary_edit_text.text.toString()
//        active = true
//        itIsJob = userSelectedJob
//        creationMillis = args.job.creationMillis
//        uid = args.job.uid
//        imagesUri = args.job.imagesUri
//        if (!userSelectedJob) { // if it's a proposal set range
//            range = range_seekbar.progress
//        }

        return this
    }

    companion object {
        const val TAG = "EditFragment"
        private const val JOB = "job"
        private const val PROPOSAL = "proposal"
    }
}
