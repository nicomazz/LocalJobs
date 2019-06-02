package com.esp.localjobs.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.navigation.fragment.navArgs
import com.esp.localjobs.R
import kotlinx.android.synthetic.main.fragment_edit.*

class EditFragment : Fragment() {
    private val args: EditFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setView()
    }

    private fun setView() {
        val job = args.job
        title_edit_text.setText(job.title)
        location_edit_text.setText(job.city)
        range_seekbar.progress = 2 // TODO once unified use job.range?
        salary_edit_text.setText(job.salary)
        description_edit_text.setText(job.description)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.forEach { it.isVisible = false }
        inflater.inflate(R.menu.menu_save, menu)
        val saveItem = menu.findItem(R.id.menu_save_item)

    }


}
