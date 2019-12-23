package toplev.com.skyhookaccountability.activity.claim

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_claim_detail.*
import toplev.com.skyhookaccountability.adapter.ActivityAdapter
import toplev.com.skyhookaccountability.activity.claim.Activity.ActivityDetailActivity
import android.view.View
import kotlinx.android.synthetic.main.view_claim_header.view.*
import toplev.com.skyhookaccountability.activity.claim.Activity.AddNewActivity
import toplev.com.skyhookaccountability.activity.main.MainActivity
import toplev.com.skyhookaccountability.model.Claim
import toplev.com.skyhookaccountability.support.App
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.ColorRes
import toplev.com.skyhookaccountability.support.OnCustomTimerListener
import com.ncorti.slidetoact.SlideToActView
import androidx.annotation.NonNull
import androidx.core.view.isVisible
import toplev.com.skyhookaccountability.R


class ClaimDetailActivity : AppCompatActivity(), OnCustomTimerListener {

    lateinit var claim: Claim

    lateinit  var activityAdapter :ActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(toplev.com.skyhookaccountability.R.layout.activity_claim_detail)

        claim = App.shared!!.selectedClaim

        //set timer listener for activity tick
        App.shared!!.setTimerListener(this)

        //set header subtitle with claim number
        claimNumberTextView.text = claim.claimNumber

        //add new activity
        addNewButton.setOnClickListener( {
            val intent = Intent(this,
                AddNewActivity::class.java)
            startActivity(intent)
        })

        //go back
        backButton.setOnClickListener( {
            val intent = Intent(this,
                MainActivity::class.java)
            startActivity(intent)
        })


        if(App.shared!!.selectedClaim.status.equals("CLOSED")){
            closeClaimButton.visibility = View.INVISIBLE
        }

        // CLOSE CLAIM ACTION
        closeClaimButton.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {
//                    App.shared!!.selectedClaim.closeClaim {
//                        if(it){
//                            //successfully closed
//                        } else{
//                            Handler(Looper.getMainLooper()).post(Runnable {
//                                closeClaimButton.resetSlider()
//                                Toast.makeText(applicationContext,"Failed to close claim. Please complete all your activities first.",Toast.LENGTH_SHORT).show()
//                            })
//
//                        }
//                    }

                }
            }

        //set header data
        val headerView = layoutInflater.inflate(toplev.com.skyhookaccountability.R.layout.view_claim_header, null) as View
        headerView.customerTexView.text = claim.customer.business
        headerView.iaFirmTextView.text = claim.claimant.fullName
        headerView.phoneTextView.text = claim.claimant.phone
        val dueDateString = "Due date: "+claim.dueDate
        headerView.dueDateTextView.text = dueDateString
        headerView.phoneTextView.setOnClickListener({
            try{
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + claim.claimant.phone))
                startActivity(intent)
            }catch (e:SecurityException){
                //permission issue
                System.out.println(e.message)
            }
        })
        headerView.addressTextView.text = claim.claimant.address.formattedString()
        headerView.addressTextView.setOnClickListener({
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q="+claim.claimant.address.formattedString())
            )
            startActivity(intent)
        })
        headerView.infoButton.setOnClickListener({
            val intent = Intent(this,
                ClaimInfoActivity::class.java)
            startActivity(intent)
        })

        claimActivityListView.addHeaderView(headerView)

        if (claim.activities.size != 0) {
            placeHolderLayout.visibility = View.INVISIBLE
        } else {
            closeClaimButton.visibility = View.INVISIBLE
        }

        activityAdapter = ActivityAdapter(claim.activities, this)
        claimActivityListView.adapter = activityAdapter
        claimActivityListView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this,
                ActivityDetailActivity::class.java)
            intent.putExtra("selectedIndex",position-1) //subtract one because header is index 0
            startActivity(intent)
        }

    }

    //first activity will be created :)
    fun createFirst(view:View){
        val intent = Intent(this,
            AddNewActivity::class.java)
        startActivity(intent)
    }


    override fun onTimer() {

        if(App.shared!!.activeActivity != null ){

            for ((index,value) in claim.activities.withIndex()){
                if(value.id.equals(App.shared!!.activeActivity!!.id)){
                    claim.activities.get(index).totalElapsedMillis = App.shared!!.activeActivity!!.totalElapsedMillis
                    activityAdapter.notifyDataSetChanged()
                }
            }
        }
    }


}
