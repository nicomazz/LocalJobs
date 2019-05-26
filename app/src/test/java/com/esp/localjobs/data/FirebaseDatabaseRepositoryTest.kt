package com.esp.localjobs.data

import android.content.Context
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import com.esp.localjobs.LocalJobsApplication
import com.esp.localjobs.data.MockRepository.Companion.NUMBER_OF_JOBS
import com.esp.localjobs.fragments.JobsFragment
import com.esp.localjobs.viewModels.DaggerViewModelInjector
import com.squareup.picasso.Picasso
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.flushBackgroundThreadScheduler
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FirebaseDatabaseRepositoryTest {
    val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        LocalJobsApplication.components = DaggerViewModelInjector.builder()
            .repositoryModule(MockRepositoryModule())
            .build()
        Picasso.Builder(context).build().let {
            Picasso.setSingletonInstance(it)
        }
    }

    @Test
    fun testJobFragment() {
        val frag = launchFragmentInContainer<JobsFragment>()
        frag.onFragment {
            flushBackgroundThreadScheduler()
            assertEquals(it.adapter.itemCount, NUMBER_OF_JOBS)
        }
    }
}