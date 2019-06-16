package com.esp.localjobs.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.RadioButton
import android.widget.SeekBar
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.models.Localizable
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.fragments.map.LocationPickerFragment
import com.esp.localjobs.utils.LoadingViewDialog
import com.esp.localjobs.viewModels.AddViewModel
import com.esp.localjobs.viewModels.LoginViewModel
import com.esp.localjobs.viewModels.LoginViewModel.AuthenticationState.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.PicassoEngine
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.suspendCoroutine


/**
 * Fragment used to push a job/proposal to remote db
 */
class AddFragment : Fragment(), LocationPickerFragment.OnLocationPickedListener, CoroutineScope {

    private lateinit var mJob: kotlinx.coroutines.Job
    override val coroutineContext: kotlin.coroutines.CoroutineContext
        get() = mJob + Dispatchers.Main

    private val IMAGE_REQUEST_CODE: Int = 42
    private val addViewModel: AddViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()

    private var selectedLocation: Location? = null
    private var selectedImage: Uri? = null
    private lateinit var viewDialog: LoadingViewDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_add, container, false).also {
            setHasOptionsMenu(true)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mJob = kotlinx.coroutines.Job()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_navigation, menu)
        menu.forEach { it.isVisible = false }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ensureLogin()

        setupDistanceSeekbarUI()
        submit_button.setOnClickListener { onSubmit() }
        setupLocationEditTextUI()
        setupRadioButton()
        setupImagePicker()
        viewDialog = LoadingViewDialog(activity!!)
    }

    private fun setupImagePicker() {
        image_picker_view.setOnClickListener {
            Matisse.from(this)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(1)
                .thumbnailScale(0.85f)
                .imageEngine(PicassoEngine())
                .forResult(IMAGE_REQUEST_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            val mSelected = Matisse.obtainResult(data);
            selectedImage = mSelected.firstOrNull()
            image_edit_text.setText(selectedImage?.toString() ?: "")
            Log.d("Matisse", "mSelected: $mSelected");
        }
    }

    private fun ensureLogin() {
        loginViewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                AUTHENTICATED -> {
                }
                UNAUTHENTICATED -> {
                    showUnauthenticatedMessage()
                    findNavController().navigate(R.id.action_destination_add_to_destination_login)
                }
                INVALID_AUTHENTICATION -> TODO()
                else -> TODO()
            }
        })
    }

    private fun showUnauthenticatedMessage() {
        Snackbar.make(
            activity!!.findViewById<View>(android.R.id.content),
            getString(R.string.auth_required),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun setupLocationEditTextUI() {
        location_edit_text.setOnClickListener {
            fragmentManager?.let { fm ->
                LocationPickerFragment.newInstanceShow(this, fm)
            }
        }
    }

    private fun setupDistanceSeekbarUI() {
        setRangeTextView(range_seekbar.progress)
        range_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setRangeTextView(progress)
            }
        })
    }

    private fun setupRadioButton() {
        type_radio_group.setOnCheckedChangeListener { _, checkedId ->
            val type = view?.findViewById<RadioButton>(checkedId)?.tag
            when (type) {
                JOB -> range_div.visibility = View.GONE
                PROPOSAL -> range_div.visibility = View.VISIBLE
                else -> TODO()
            }
        }
    }

    /**
     * Called when submit button is pressed
     */
    private fun onSubmit() = launch {
        val location = selectedLocation
        if (!validateForm() || location == null)
            return@launch
        viewDialog.showDialog()

        if (selectedImage != null) {
            val imageUploadedUri = uploadImageToFirestore()
        }

        val job = parseJobFromView(location)

        // called after completion of add task
        val onItemPushSuccess: () -> Unit = {
            viewDialog.hideDialog()
            Snackbar.make(
                activity!!.findViewById<View>(android.R.id.content),
                getString(R.string.add_job_success),
                Snackbar.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
        val onItemPushFailure = {
            viewDialog.hideDialog()
            Snackbar.make(
                activity!!.findViewById<View>(android.R.id.content),
                getString(R.string.add_job_failure),
                Snackbar.LENGTH_SHORT
            ).show()
        }
        addViewModel.addJobToRepository(job, onSuccess = onItemPushSuccess, onFailure = onItemPushFailure)
    }

    private suspend fun uploadImageToFirestore(uri: Uri): String? =
        suspendCoroutine { continuation ->

            // Create a storage reference from our app
            val storageRef = FirebaseStorage.getInstance().getReference()
            val imageRef = storageRef.child("images/${uri.lastPathSegment}")
            val uploadTask = imageRef.putFile(uri)

            // Register observers to listen for when the download is done or if it fails
            val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation imageRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    continuation.resumeWith(downloadUri?.toString())
                } else {
                    Log.e(TAG, "Error in getting the downlaod link")
                    continuation.resumeWith(null)
                    // Handle failures
                    // ...
                }
            }


        }

    /**
     * Called when apply button is pressed in LocationPickerFragment
     */
    override fun onLocationPicked(location: Location) {
        val locationText =
            if (location.city != null) location.city
            else getString(R.string.coordinates, location.l[0].toString(), location.l[1].toString())
        location_edit_text.setText(locationText)
        selectedLocation = location
    }

    /**
     * For setting the value next to seek bar
     * @param value The value corresponding to the seekbar position
     */
    private fun setRangeTextView(value: Int) {
        range_value.text = getString(R.string.distance, value)
    }

    private fun validateForm(): Boolean {
        var anyError = false
        if (selectedLocation == null) {
            location_view.error = "Please pick a location"
            anyError = true
        }
        if (title_edit_text.text.toString().isEmpty()) {
            title_view.error = "Please insert a title"
            anyError = true
        }
        return !anyError
    }

    /**
     * Create a Job from the view using class variable selectedLocation.
     * @param location position of the job
     * @return Job parsed from the view
     */
    private fun parseJobFromView(location: Localizable): Job = Job().apply {
        val userSelectedJob = type_radio_group.checkedRadioButtonId == R.id.radio_job

        title = title_edit_text.text.toString()
        description = description_edit_text.text.toString()
        l = location.latLng().toList()
        city = location_edit_text.text.toString()
        salary = salary_edit_text.text.toString()
        active = true
        itIsJob = userSelectedJob
        uid = loginViewModel.getUserId()
        if (!userSelectedJob) { // if it's a proposal set range
            range = range_seekbar.progress
        }

        return this
    }

    companion object {
        const val TAG = "AddFragment"
        private const val JOB = "job"
        private const val PROPOSAL = "proposal"
    }
}
