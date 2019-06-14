package com.esp.localjobs

import android.Manifest
import android.os.Bundle
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import android.content.Intent
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.model.SliderPage
import androidx.core.content.ContextCompat

class IntroActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlides()

        askForPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            2 // ask permissions on slide 2
        )

        showSkipButton(false)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        startMainActivity()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        startMainActivity()
    }

    private fun startMainActivity() = startActivity(Intent(this, MainActivity::class.java))

    private fun addSlides() {
        val page1 = SliderPage()
        page1.title = getString(R.string.intro_title)
        page1.description = getString(R.string.intro_description)
        page1.bgColor = ContextCompat.getColor(this, R.color.bgIntroPage1)
        page1.imageDrawable = R.drawable.intro_search_job

        val page2 = SliderPage()
        page2.title = getString(R.string.permission_tab_title)
        page2.description = getString(R.string.permission_tab_description)
        page2.bgColor = ContextCompat.getColor(this, R.color.bgIntroPage2)
        page2.imageDrawable = R.drawable.intro_location_image

        addSlide(AppIntroFragment.newInstance(page1))
        addSlide(AppIntroFragment.newInstance(page2))
    }
}
