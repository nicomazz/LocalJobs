package com.esp.localjobs.adapters

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.navigation.Navigation.findNavController
import com.bumptech.glide.Glide
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.databinding.ItemJobBinding
import com.esp.localjobs.fragments.JobDetailsFragment
import com.esp.localjobs.fragments.JobsFragmentDirections
import com.xwray.groupie.databinding.BindableItem
import kotlinx.coroutines.InternalCoroutinesApi

class JobItem(val job: Job) : BindableItem<ItemJobBinding>() {

    override fun getId() = job.uid.hashCode().toLong()

    @InternalCoroutinesApi
    override fun bind(viewBinding: ItemJobBinding, position: Int) = with(viewBinding) {
        job = this@JobItem.job
        this@JobItem.job.imagesUri.firstOrNull()?.let {
            Glide.with(cardView.context).load(it).placeholder(R.drawable.placeholder).into(imageView)
        } ?: Glide.with(cardView.context).load("https://picsum.photos/400").placeholder(R.drawable.placeholder).into(imageView)
        cardView.clipToOutline = false // without this, shared elements are cropped
        cardView.setOnClickListener {
            val extras = JobDetailsFragment.setupTransitionName(
                imageView, title, description, this@JobItem.job
            )
            val action =
                JobsFragmentDirections.actionDestinationJobsToDestinationJobDetails(this@JobItem.job)
            findNavController(it)
                .navigate(
                    R.id.action_destination_jobs_to_destination_job_details,
                    action.arguments, null, extras
                )
        }
    }

    override fun getLayout() = R.layout.item_job
}

@BindingAdapter("salary")
fun TextView.setSalary(salary: String) {
    val value = salary.toIntOrNull()
    text = value?.let {
        if (it > 0)
            "You will earn $value £"
        else
            "You have to pay $value £"
    } ?: resources.getString(R.string.no_price_info)
}

@BindingAdapter("salary")
fun View.setSalary(salary: String) {
    val value = salary.toIntOrNull()
    setBackgroundColor(
        context.getColor(
            if (value == null || value > 0)
                android.R.color.holo_green_dark
            else
                android.R.color.holo_red_dark
        )
    )
}