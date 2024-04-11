package com.example.connectproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class Onboarding : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var btnSkip: Button
    private lateinit var btnNext: Button
    private lateinit var btnBack: Button

    private val layouts = intArrayOf(
        R.layout.onboarding1,
        R.layout.onboarding2,
        R.layout.onboarding3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        btnSkip = findViewById(R.id.btnSkip)
        btnNext = findViewById(R.id.btnNext)
        btnBack = findViewById(R.id.btnBack)

        pagerAdapter = ScreenSlidePagerAdapter()
        viewPager.adapter = pagerAdapter

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}

            override fun onPageSelected(position: Int) {
                // Show or hide back button based on current page
                btnBack.visibility = if (position > 0) View.VISIBLE else View.INVISIBLE
                // Center Skip and Next buttons when Back button is invisible
                if (btnBack.visibility == View.INVISIBLE) {
                    val layoutParams = btnSkip.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.marginStart = 0
                    val layoutParamsNext = btnNext.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParamsNext.marginEnd = 0
                } else {
                    // Reset margins when Back button is visible
                    val layoutParams = btnSkip.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.marginStart = resources.getDimension(R.dimen.button_margin_start).toInt()
                    val layoutParamsNext = btnNext.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParamsNext.marginEnd = resources.getDimension(R.dimen.button_margin_end).toInt()
                }
            }
        })
    }

    // Implement skip button click listener to navigate to LoginActivity
    fun onSkipButtonClick(view: View) {
        navigateToLoginActivity()
    }

    // Implement next button click listener to navigate to next screen or LoginActivity if on last screen
    fun onNextButtonClick(view: View) {
        if (viewPager.currentItem < layouts.size - 1) {
            viewPager.currentItem += 1
        } else {
            navigateToLoginActivity()
        }
    }

    // Implement back button click listener to navigate to previous screen
    fun onBackButtonClick(view: View) {
        viewPager.currentItem -= 1
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private inner class ScreenSlidePagerAdapter : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout = inflater.inflate(layouts[position], container, false)
            container.addView(layout)
            return layout
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }
    }
}
