package toplev.com.skyhookaccountability.activity.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_begin_auth.*
import kotlinx.android.synthetic.main.activity_main.*
import toplev.com.skyhookaccountability.R
import toplev.com.skyhookaccountability.activity.claim.ClaimListFragment
import toplev.com.skyhookaccountability.activity.notification.NotificationListFragment
import toplev.com.skyhookaccountability.activity.profile.ProfileFragment
import toplev.com.skyhookaccountability.activity.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    private var titleTV: TextView? = null

    private val titles = arrayOf(R.string.tab0,R.string.tab1,R.string.tab2,R.string.tab3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        titleTV = toolbarTitleTextView

        when (tabLayout) {
            is TabLayout ->  configureTabLayout()
        }


    }

    private fun configureTabLayout() {

        //default selection
        setTitleText(0)


        tabLayout.addTab(tabLayout.newTab().setText("Claims"))
        tabLayout.addTab(tabLayout.newTab().setText("Notifications"))
        tabLayout.addTab(tabLayout.newTab().setText("Settings"))
        tabLayout.addTab(tabLayout.newTab().setText("Profile"))

        setIcons()

        val adapter = TabPagerAdapter(supportFragmentManager,
            tabLayout.tabCount)
        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                setTitleText(tab.position)

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }

        })

        //default selection
        setTitleText(0)

    }


    fun setIcons(){
        tabLayout.getTabAt(0)!!.setIcon(R.drawable.claim)
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.notifications)
        tabLayout.getTabAt(2)!!.setIcon(R.drawable.settings)
        tabLayout.getTabAt(3)!!.setIcon(R.drawable.account)
    }

    fun setTitleText(position: Int){
        System.out.println("HI"+position)
        try {
            when (position) {
                0 -> titleTV!!.text = getString(titles[0])
                1 -> titleTV!!.text = getString(titles[1])
                2 -> titleTV!!.text = getString(titles[2])
                else -> {
                    titleTV!!.text = getString(titles[3])
                }
            }
        }catch (e:Exception){
            //titleTV is null maybe or index is not supported
        }
    }


    fun showLoading(show:Boolean){
        if (show){
            mainProgressBar.visibility = View.VISIBLE
        } else {
            mainProgressBar.visibility = View.INVISIBLE

        }

    }


    class TabPagerAdapter(fm: FragmentManager, private var tabCount: Int) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {

            when (position) {
                0 -> return ClaimListFragment()
                1 -> return NotificationListFragment()
                2 -> return SettingsFragment()
                3 -> return ProfileFragment()
                else -> {
                    return ClaimListFragment()
                }
            }
        }

        override fun getCount(): Int {
            return tabCount
        }
    }


}
