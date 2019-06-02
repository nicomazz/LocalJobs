package com.esp.localjobs.adapters

import android.graphics.Color
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.databinding.ItemJobBinding
import com.esp.localjobs.fragments.JobsFragmentDirections
import com.squareup.picasso.Picasso
import com.xwray.groupie.databinding.BindableItem

class JobItem(val job: Job, private val isSelected: Boolean) : BindableItem<ItemJobBinding>() {
    override fun getId() = job.uid.hashCode().toLong()

    override fun bind(viewBinding: ItemJobBinding, position: Int) {
        viewBinding.job = job
        Picasso.get().load("https://picsum.photos/200").into(viewBinding.imageView)
        viewBinding.imageView.setOnClickListener {
            val extras = FragmentNavigatorExtras(
                viewBinding.imageView to "image",
                viewBinding.title to "title",
                viewBinding.description to "description"
            )
            val action = JobsFragmentDirections.actionDestinationJobsToDestinationJobDetails(job)
            findNavController(viewBinding.imageView)
                .navigate(R.id.action_destination_jobs_to_destination_job_details, action.arguments, null, extras)
        }

        // highlight card if is selected
        if (isSelected) viewBinding.cardView.setBackgroundColor(Color.CYAN)
        else viewBinding.cardView.setBackgroundColor(Color.WHITE)
    }

    override fun getLayout() = R.layout.item_job
}