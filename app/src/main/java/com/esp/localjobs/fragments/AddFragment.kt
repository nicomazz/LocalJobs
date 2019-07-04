package com.esp.localjobs.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.esafirm.imagepicker.features.ImagePicker
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.models.Localizable
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.fragments.map.LocationPickerFragment
import com.esp.localjobs.utils.AnimationsUtils
import com.esp.localjobs.utils.BitmapUtils
import com.esp.localjobs.utils.LoadingViewDialog
import com.esp.localjobs.viewModels.AddViewModel
import com.esp.localjobs.viewModels.LoginViewModel
import com.esp.localjobs.viewModels.LoginViewModel.AuthenticationState.AUTHENTICATED
import com.esp.localjobs.viewModels.LoginViewModel.AuthenticationState.INVALID_AUTHENTICATION
import com.esp.localjobs.viewModels.LoginViewModel.AuthenticationState.UNAUTHENTICATED
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.delete_button
import kotlinx.android.synthetic.main.fragment_add.description_edit_text
import kotlinx.android.synthetic.main.fragment_add.location_edit_text
import kotlinx.android.synthetic.main.fragment_add.range_seekbar
import kotlinx.android.synthetic.main.fragment_add.salary_edit_text
import kotlinx.android.synthetic.main.fragment_add.title_edit_text
import kotlinx.android.synthetic.main.fragment_edit.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Fragment used to push a job/proposal to remote db
 * Fragment used to create or edit a job. If a job is provided in the args then the fragment will be in 'edit mode'
 */
class AddFragment : Fragment(), LocationPickerFragment.OnLocationPickedListener, CoroutineScope {
    private lateinit var mJob: kotlinx.coroutines.Job
    override val coroutineContext: kotlin.coroutines.CoroutineContext
        get() = mJob + Dispatchers.Main

    private val IMAGE_REQUEST_CODE: Int = 42
    private val addViewModel: AddViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val args: AddFragmentArgs by navArgs()

    private var selectedLocation: Location? = null
    private var selectedImage: String? = null
    private lateinit var viewDialog: LoadingViewDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_add, container, false).also {
            setHasOptionsMenu(true)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mJob = kotlinx.coroutines.Job()
        postponeEnterTransition()
        setupBackAnimations()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.forEach { it.isVisible = false }
        if (args.job != null) {
            inflater.inflate(R.menu.menu_save, menu)
            val saveItem = menu.findItem(R.id.menu_save_item)
            saveItem.setOnMenuItemClickListener {
                onSaveClick()
                true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ensureLogin()

        args.job?.let {
            setupEditMode(it)
        } ?: setupAddMode()

        setupDistanceSeekbarUI()
        submit_button.setOnClickListener { onSubmit() }
        setupLocationEditTextUI()
        setupRadioButton()
        setupImagePicker()
        viewDialog = LoadingViewDialog(activity!!)
        AnimationsUtils.popup(submit_button, 400)
        startPostponedEnterTransition()
    }

    /**
     * Show and set delete and save button, set job data
     */
    private fun setupEditMode(job: Job) = with(job) {
        selectedLocation = Location(latitude(), longitude(), city)
        if (itIsJob == true) {
            type_radio_group.check(R.id.radio_job)
        } else {
            type_radio_group.check(R.id.radio_proposal)
            range_div.visibility = View.VISIBLE
        }

        title_edit_text.setText(title)
        description_edit_text.setText(description)
        range?.let { range_seekbar.progress = it }
        location_edit_text.setText(city)
        salary_edit_text.setText(salary)

        delete_button.apply {
            visibility = View.VISIBLE
            setOnClickListener { onDeleteClick() }
        }
    }

    /**
     * Show and set publish button
     */
    private fun setupAddMode() {
        submit_button.visibility = View.VISIBLE
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
        AnimationsUtils.popout(submit_button) {
            findNavController().popBackStack()
        }
    }

    private fun setupImagePicker() {
        image_edit_text.setOnClickListener {
            if (missingPermissions()) {
                askStoragePermissions()
                return@setOnClickListener
            }
            ImagePicker.create(this)
                .includeVideo(false) // Show video on image picker
                .single() // single mode
                .showCamera(true) // show camera or not (true by default)
                .start() // start image picker activity with request code
        }
    }

    private fun askStoragePermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }

    private fun missingPermissions() =
        ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val image = ImagePicker.getFirstImageOrNull(data)
            image_edit_text.setText(image.name)
            selectedImage = image.path
            Log.d("imagePicker", "mSelected: ${image.path}")
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

        val imageUploadedUri = selectedImage?.let { uploadImageToFirestore(it) } ?: ""
        if (!isActive) return@launch

        val job = parseJobFromView(location = location, uploadedImageUri = imageUploadedUri)

        // called after completion of add task
        val onItemPushSuccess: () -> Unit = {
            hideProgressDialogAndShowSnackbar(getString(R.string.add_job_success))
            findNavController().popBackStack()
        }
        val onItemPushFailure = {
            hideProgressDialogAndShowSnackbar(getString(R.string.add_job_failure))
        }
        addViewModel.addJobToRepository(job, onSuccess = onItemPushSuccess, onFailure = onItemPushFailure)
    }

    private fun hideProgressDialogAndShowSnackbar(text: String) {
        viewDialog.hideDialog()
        Snackbar.make(
            activity!!.findViewById<View>(android.R.id.content),
            text,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private suspend fun uploadImageToFirestore(path: String): String? =
        suspendCoroutine { continuation ->

            // Create a storage reference from our app
            val storageRef = FirebaseStorage.getInstance().reference
            // todo put images in a path with the tag of the job. Otherwise they can be replaced
            val imageRef = storageRef.child("images/${File(path).name}")
            val toImage = Compressor(context).compressToBitmap(File(path))
            val bytes = BitmapUtils.bitmapToByteArray(toImage)
            val uploadTask = imageRef.putBytes(bytes)

            if (!isActive) return@suspendCoroutine

            // Register observers to listen for when the download is done or if it fails
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation imageRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    continuation.resume(downloadUri?.toString())
                } else {
                    Log.e(TAG, "Error in getting the downlaod link")
                    continuation.resume(null)
                    // Handle failures
                    // ...
                }
            }
        }

    /**
     * Called when apply button is pressed in LocationPickerFragment
     */
    override fun onLocationPicked(location: Location, distance: Int?) {
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
    private fun parseJobFromView(location: Localizable, uploadedImageUri: String): Job = Job().apply {
        val userSelectedJob = type_radio_group.checkedRadioButtonId == R.id.radio_job

        title = title_edit_text.text.toString()
        description = description_edit_text.text.toString()
        l = location.latLng().toList()
        city = location_edit_text.text.toString()
        salary = salary_edit_text.text.toString()
        active = true
        itIsJob = userSelectedJob
        uid = loginViewModel.getUserId()
        creationMillis = System.currentTimeMillis()
        if (!userSelectedJob) { // if it's a proposal set range
            range = range_seekbar.progress
        }
        if (uploadedImageUri.isNotBlank())
            imagesUri = listOf(uploadedImageUri)

        return this
    }

    /**
     * Update job's fields
     */
    private fun onSaveClick() = launch {
        val location = selectedLocation
        if (!validateForm() || location == null)
            return@launch

        viewDialog.showDialog()

        val imageUploadedUri = if (selectedImage.isNullOrEmpty()) {
            // use previous image // todo update to accept > 1 images when needed
            args.job?.imagesUri?.first() ?: ""
        } else {
            uploadImageToFirestore(selectedImage as String) ?: ""
        }
        if (!isActive) return@launch

        val newJob = parseJobFromView(location, imageUploadedUri)
        newJob.id = args.job?.id ?: return@launch

        if (newJob == args.job) {
            Snackbar.make(
                activity!!.findViewById<View>(android.R.id.content),
                getString(R.string.edit_no_changes_detected),
                Snackbar.LENGTH_SHORT
            ).show()
            return@launch
        }

        // called after completion of add task
        val onItemEditSuccess: () -> Unit = {
            hideProgressDialogAndShowSnackbar(getString(R.string.edit_job_success))
            val action = AddFragmentDirections.actionDestinationAddToDestinationJobDetails(
                job = newJob,
                mustBeFetched = false
            )
            findNavController().navigate(action)
        }

        val onItemEditFailure = { e: Exception ->
            hideProgressDialogAndShowSnackbar(getString(R.string.edit_job_failure))
            viewDialog.hideDialog()
        }
        Log.d(TAG, "old job: ${args.job}\nnew job: $newJob")

        addViewModel.update(
            id = newJob.id,
            newJob = newJob,
            onSuccess = onItemEditSuccess,
            onFailure = onItemEditFailure
        )
    }

    private fun onDeleteClick() {
        val onItemDeleteSuccess: () -> Unit = {
            hideProgressDialogAndShowSnackbar(getString(R.string.delete_job_success))
            findNavController().navigate(R.id.destination_jobs)
        }

        val onItemDeleteFailure = { e: Exception ->
            hideProgressDialogAndShowSnackbar(getString(R.string.delete_job_failure))
        }
        viewDialog.showDialog()
        args.job?.let {
            addViewModel.delete(it.id, onSuccess = onItemDeleteSuccess, onFailure = onItemDeleteFailure)
        }
    }

    companion object {
        const val TAG = "AddFragment"
        private const val JOB = "job"
        private const val PROPOSAL = "proposal"
    }
}
