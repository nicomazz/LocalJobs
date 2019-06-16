package com.esp.localjobs.adapters

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.esp.localjobs.R
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.models.User
import com.esp.localjobs.data.repository.userFirebaseRepository
import com.esp.localjobs.databinding.ItemJobBinding
import com.esp.localjobs.fragments.JobsFragmentDirections
import com.squareup.picasso.Picasso
import com.xwray.groupie.databinding.BindableItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

@InternalCoroutinesApi
class JobItem(val job: Job) : BindableItem<ItemJobBinding>() {

    override fun getId() = job.uid.hashCode().toLong()

    override fun bind(viewBinding: ItemJobBinding, position: Int) = with(viewBinding) {
        job = this@JobItem.job

        GlobalScope.launch(Dispatchers.Main) {
            val author = this@JobItem.job.uid?.let {
                userFirebaseRepository.getUserDetails(it)
            } ?: User(displayName = "Unknown user")
            setAuthor(author)
        }

        Picasso.get().load("https://picsum.photos/400").into(imageView)

        cardView.clipToOutline = false // without this, shared elements are cropped
        imageView.transitionName = "image_${this@JobItem.job.uid}"
        title.transitionName = "title_${this@JobItem.job.uid}"
        // description.transitionName = "description_${this@JobItem.job.uid}"

        cardView.setOnClickListener {
            val extras = FragmentNavigatorExtras(
                imageView to "image_${this@JobItem.job.uid}",
                title to "title_${this@JobItem.job.uid}"
                // description to "description_${this@JobItem.job.uid}"
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