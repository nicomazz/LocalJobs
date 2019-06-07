package com.esp.localjobs.adapters

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.databinding.ItemJobBinding
import com.esp.localjobs.fragments.JobsFragmentDirections
import com.squareup.picasso.Picasso
import com.xwray.groupie.databinding.BindableItem

class JobItem(val job: Job) : BindableItem<ItemJobBinding>() {

    override fun getId() = job.uid.hashCode().toLong()

    override fun bind(viewBinding: ItemJobBinding, position: Int) = with(viewBinding) {
        job = this@JobItem.job
        Picasso.get().load("https://picsum.photos/200").into(imageView)
        cardView.setOnClickListener {
            val extras = FragmentNavigatorExtras(
                imageView to "image",
                title to "title",
                description to "description"
            )
            val action =
                JobsFragmentDirections.actionDestinationJobsToDestinationJobDetails(this@JobItem.job)
            findNavController(imageView)
                .navigate(
                    R.id.action_destination_jobs_to_destination_job_details,
                    action.arguments, null, extras
                )
        }
    }

    override fun getLayout() = R.layout.item_job
}

@BindingAdapter("salary")
fun TextView.setSalary(salary: Float?) {
    salary?.let {
        if (it > 0)
            "You will earn $salary £"
        else
            "You have to pay $salary £"
    } ?: resources.getString(R.string.no_price_info)
}

@BindingAdapter("salary")
fun View.setSalary(salary: Float?) {
    setBackgroundColor(
        context.getColor(
            if (salary == null || salary > 0)
                android.R.color.holo_green_dark
            else
                android.R.color.holo_red_dark
        )
    )
}