package com.esp.localjobs.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.esp.localjobs.DI.DaggerViewModelInjector
import com.esp.localjobs.LocalJobsApplication
import com.squareup.picasso.Picasso
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
        /* val frag = launchFragmentInContainer<JobsFragment>()
         frag.onFragment {
             flushBackgroundThreadScheduler()
             assertEquals(it.adapter.itemCount, NUMBER_OF_JOBS)
         }*/
    }
}