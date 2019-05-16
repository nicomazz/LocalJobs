package com.esp.localjobs.adapters

import com.esp.localjobs.R
import com.esp.localjobs.models.Job
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_job.view.*

class JobItem(private val job: Job) : Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) = with(viewHolder.itemView) {
        viewHolder.itemView.title.text = job.title
        viewHolder.itemView.description.text = job.desc
    }

    override fun getLayout() = R.layout.item_job

}