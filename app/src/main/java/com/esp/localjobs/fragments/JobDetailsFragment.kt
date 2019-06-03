package com.esp.localjobs.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.transition.ChangeBounds
import androidx.transition.TransitionInflater
import com.esp.localjobs.R
import com.esp.localjobs.viewModels.LoginViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_job_details.*
import kotlinx.android.synthetic.main.fragment_job_details.view.*

/**
 * Fragment used to display the details of a job.
 * Params:
 * jobID: String -> the ID of a job
 */

class JobDetailsFragment : Fragment() {
    private val args: JobDetailsFragmentArgs by navArgs()
    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_job_details, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trans = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = ChangeBounds().apply {
            enterTransition = trans
        }
        sharedElementReturnTransition = ChangeBounds().apply {
            enterTransition = trans
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Picasso.get().load("https://picsum.photos/200").into(view.imageView)
        view.title.text = args.job.title
        view.description.text = args.job.description

        setupFabButton()
    }
    //  TODO Se la persona ha già inviato la disponibilità, il testo dev'essere "contacted"
    // TODO check if, rather than hiding the button, the visibility can be set to "disabled" (like grey button)
    /**
     * Setup contact fab button.
     * If the user isn't logged or the user owns the job, the button is set to invisible.
     */
    private fun setupFabButton() {
        val currentUserId = loginViewModel.getUserId()
        val jobOwner = args.job.uid
        if (currentUserId == null) {
            return
        }
        /*
         //commented for testing
         if (currentUserId == null || jobOwner == currentUserId) {
             contact_fab.visibility = View.GONE
             return
         }*/

        contact_fab.setOnClickListener {
            val document = mapOf(
                "job_publisher_id" to jobOwner,
                "name" to loginViewModel.getUserName(),
                "interested_user_id" to currentUserId,
                "job_id" to args.job.id // used to retrieve job body
                // todo aggiungere un breve messaggio
            )
            FirebaseFirestore.getInstance().collection("jobs")
                .document(args.job.id).collection("requests")
                .document(currentUserId).set(document)
        }
    }
}
