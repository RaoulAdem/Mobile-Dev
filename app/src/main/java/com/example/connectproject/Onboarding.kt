package com.example.connectproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.connectproject.databinding.ActivityOnboardingBinding
import com.google.firebase.auth.FirebaseAuth

class Onboarding : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: PagerAdapter
    private val layouts = intArrayOf(
        R.layout.onboarding1,
        R.layout.onboarding2,
        R.layout.onboarding3,
        R.layout.onboarding4
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Base_Theme_ConnectProject)
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        viewPager = findViewById(R.id.viewPager)
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
                //show or hide back button based on current page
                binding.btnBack.visibility = if (position > 0) View.VISIBLE else View.INVISIBLE
                //center Skip and Next buttons when Back button is invisible
                if (binding.btnBack.visibility == View.INVISIBLE) {
                    val layoutParams = binding.btnSkip.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.marginStart = 0
                    val layoutParamsNext = binding.btnNext.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParamsNext.marginEnd = 0
                } else {
                    //reset margins when Back button is visible
                    val layoutParams = binding.btnSkip.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.marginStart = resources.getDimension(R.dimen.button_margin_start).toInt()
                    val layoutParamsNext = binding.btnNext.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParamsNext.marginEnd = resources.getDimension(R.dimen.button_margin_end).toInt()
                }
            }
        })
    }

    fun onSkipButtonClick(view: View) {
        navigateToLoginActivity()
    }
    fun onNextButtonClick(view: View) {
        if (viewPager.currentItem < layouts.size - 1) {
            viewPager.currentItem += 1
        } else {
            navigateToLoginActivity()
        }
    }
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

    override fun onStart() {
        super.onStart()
        if(firebaseAuth.currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
