package toplev.com.skyhookaccountability.activity.claim.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_new.*
import toplev.com.skyhookaccountability.R
import toplev.com.skyhookaccountability.adapter.NewActivityItemAdapter
import toplev.com.skyhookaccountability.activity.claim.ClaimDetailActivity
import toplev.com.skyhookaccountability.activity.main.MainActivity
import toplev.com.skyhookaccountability.model.Activity
import toplev.com.skyhookaccountability.model.Claim
import toplev.com.skyhookaccountability.model.SectionItem
import toplev.com.skyhookaccountability.support.App

class AddNewActivity : AppCompatActivity() {

    val activityList = ArrayList<SectionItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new)

        addNewLayout.visibility = View.INVISIBLE

        backButton.setOnClickListener({
            val intent = Intent(this,
                ClaimDetailActivity::class.java)
            startActivity(intent)
        })

        //add activity and section names
        setupSectionItems()

        val activityAdapter = NewActivityItemAdapter(this,activityList)
        activityItemsListView.adapter = activityAdapter
        activityItemsListView.setOnItemClickListener { _, _, position, _ ->

            if (position == activityList.size -1) { // selected 'Other'--> allow for custom input
                //pending

                activityItemsListView.visibility = View.INVISIBLE
                addNewLayout.visibility = View.VISIBLE

            } else {
                addNewActivity(activityList.get(position).title)
            }


        }

    }

    fun onCreate(view:View){
        if(!activityNameEditText.text.toString().equals("")){
            addNewActivity(activityNameEditText.text.toString())
        } else {
            Toast.makeText(this, "Please input an activity name.",Toast.LENGTH_SHORT).show()
        }
    }

    fun setupSectionItems(){

        val sectionItem0 = SectionItem("CLAIMANT",true)
        val s0item0 = SectionItem("First Contact",false)
        val s0item1 = SectionItem("Drive To Destination",false)
        val s0item2 = SectionItem("Met With",false)
        val s0item3 = SectionItem("Return Driving",false)

        val sectionItem1 = SectionItem("INSURED",true)
        val s1item0 = SectionItem("First Contact",false)
        val s1item1 = SectionItem("Drive To Destination",false)
        val s1item2 = SectionItem("Met With",false)
        val s1item3 = SectionItem("Return Driving",false)

        val sectionItem2 = SectionItem("OTHER",true)
        val s2item0 = SectionItem("First Contact",false)
        val s2item1 = SectionItem("Drive To Destination",false)
        val s2item2 = SectionItem("Met With",false)
        val s2item3 = SectionItem("Return Driving",false)
        val s2item4 = SectionItem("Scene Investigation / Documentation of Scene",false)
        val s2item5 = SectionItem("Other",false)

        activityList.add(sectionItem0)
        activityList.add(s0item0)
        activityList.add(s0item1)
        activityList.add(s0item2)
        activityList.add(s0item3)

        activityList.add(sectionItem1)
        activityList.add(s1item0)
        activityList.add(s1item1)
        activityList.add(s1item2)
        activityList.add(s1item3)

        activityList.add(sectionItem2)
        activityList.add(s2item0)
        activityList.add(s2item1)
        activityList.add(s2item2)
        activityList.add(s2item3)
        activityList.add(s2item4)
        activityList.add(s2item5)


    }

    //set to claim, update server with graphql call
    fun addNewActivity(activityName:String){

        Activity().addNewActivity(claimId = App.shared!!.selectedClaim.id, name = activityName) {
            if(it != null){
                App.shared!!.selectedClaim.activities.add(it)
                val intent = Intent(this,
                    ClaimDetailActivity::class.java)
                intent.putExtra("newActivity", activityName)
                startActivity(intent)
            } else {
                Toast.makeText(this,"Unable to create new activity at this time. Please try again later.",Toast.LENGTH_LONG).show()
            }
        }


    }
}
