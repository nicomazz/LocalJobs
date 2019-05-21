package com.esp.localjobs.adapters

import com.esp.localjobs.R
import com.esp.localjobs.databinding.ItemJobBinding
import com.esp.localjobs.data.models.Job
import com.squareup.picasso.Picasso
import com.xwray.groupie.databinding.BindableItem

class JobItem(val job: Job) : BindableItem<ItemJobBinding>() {
    override fun getId() = job.uid.hashCode().toLong()

    override fun bind(viewBinding: ItemJobBinding, position: Int) {
        viewBinding.job = job
        Picasso.get().load("https://picsum.photos/200").into(viewBinding.imageView)
    }

    override fun getLayout() = R.layout.item_job
}