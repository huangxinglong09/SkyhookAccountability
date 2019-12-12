package toplev.com.skyhookaccountability.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_detail_activity.*
import toplev.com.skyhookaccountability.R
import toplev.com.skyhookaccountability.model.Activity
import toplev.com.skyhookaccountability.model.Claim
import toplev.com.skyhookaccountability.support.App
import toplev.com.skyhookaccountability.support.BackgroundForegroundLocationService
import java.lang.Exception
import android.os.Looper
import android.os.Handler
import kotlinx.android.synthetic.main.activity_claim_info.*
import toplev.com.skyhookaccountability.support.OnCustomTimerListener


class ActivityAdapter(items: ArrayList<Activity>, ctx: Context) :
    ArrayAdapter<Activity>(ctx, R.layout.cell_activity, items) {


    //view holder is used to prevent findViewById calls
    private class ActivityViewHolder {
        internal var action: ImageView? = null
        internal var name: TextView? = null
        internal var time: TextView? = null
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view

        val viewHolder: ActivityViewHolder


        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(toplev.com.skyhookaccountability.R.layout.cell_activity, viewGroup, false)

            viewHolder = ActivityViewHolder()
            viewHolder.name = view!!.findViewById<View>(toplev.com.skyhookaccountability.R.id.nameTextView) as TextView
            viewHolder.time = view.findViewById<View>(toplev.com.skyhookaccountability.R.id.timeTextView) as TextView
            viewHolder.action = view.findViewById<View>(toplev.com.skyhookaccountability.R.id.actionImageView) as ImageView
        } else {
            //no need to call findViewById, can use existing ones from saved view holder
            viewHolder = view.tag as ActivityViewHolder
        }

        val activity = getItem(i)

        if(activity != null)
        {

            viewHolder.name!!.text = activity.name

            viewHolder.time!!.text = activity.formatTime(activity.totalElapsedMillis.toLong())


            viewHolder.action!!.setImageResource(toplev.com.skyhookaccountability.R.drawable.play_small)
            if(App.shared!!.activeActivity != null && App.shared!!.activeActivity!!.id.equals(activity.id)) {
                //already started activity
                viewHolder.action!!.setImageResource(R.drawable.stop_small)
            }

            // *** PLAY/STOP CLICK *** //
            viewHolder.action!!.setOnClickListener({
                if(!activity.status.equals("STARTED")) { //start activity
                    viewHolder.action!!.setImageResource(toplev.com.skyhookaccountability.R.drawable.stop_small)
                    activity.startActivity {
                        //success
                        if(it){
                            //start location service tracking
                            App.shared!!.beginGeoTracking(activity)

                            Handler(Looper.getMainLooper()).post(Runnable {
                                // things to do on the main thread
                                if(activity.name.contains("Driv")){
                                    AlertDialog.Builder(context)
                                        .setTitle("Open Navigation")
                                        .setMessage("Would you like to directions to the claimant address?")
                                        .setPositiveButton(android.R.string.ok) { _, _ -> yesClicked(App.shared!!.selectedClaim.claimant.address.formattedString()) }
                                        .setNegativeButton(android.R.string.cancel) { _, _ -> noClicked() }
                                        .show()
                                }

                            })


                        } else {
                            viewHolder.action!!.setImageResource(toplev.com.skyhookaccountability.R.drawable.play_small)
                        }
                    }

                } else { //activity started, stop it
                    viewHolder.action!!.setImageResource(toplev.com.skyhookaccountability.R.drawable.play_small)
                    if(App.shared!!.activeActivity == null) // dev mode
                        return@setOnClickListener

                    if(activity.id.equals(App.shared!!.activeActivity!!.id)){
                        viewHolder.action!!.setImageResource(toplev.com.skyhookaccountability.R.drawable.play_small)
                        activity.endActivity {
                            //success
                            //stop location service
                            App.shared!!.stopGeoTracking(context)
                            if(it){
                                activity.status = "COMPLETE"
                                //stop location service tracking
                            }

                        }
                    }


                }
            })
        }

        view.tag = viewHolder

        return view

    }


    private fun yesClicked(address:String) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q="+address)
            )
            context.startActivity(intent)

    }
    private fun noClicked() {
        //do nothing
    }


}

