package toplev.com.skyhookaccountability.activity.claim.Activity

import android.app.AlertDialog
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_add_new.*
import kotlinx.android.synthetic.main.activity_add_new.backButton
import kotlinx.android.synthetic.main.activity_detail_activity.*
import toplev.com.skyhookaccountability.R
import toplev.com.skyhookaccountability.activity.claim.ClaimDetailActivity
import toplev.com.skyhookaccountability.model.Activity
import toplev.com.skyhookaccountability.support.App
import toplev.com.skyhookaccountability.support.BackgroundForegroundLocationService
import toplev.com.skyhookaccountability.support.OnCustomTimerListener
import java.lang.Exception
import java.util.jar.Manifest
import android.widget.ImageView.ScaleType
import android.graphics.BitmapFactory
import android.R.interpolator.linear
import android.widget.LinearLayout
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.ImageView
import com.squareup.picasso.Picasso
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class ActivityDetailActivity : AppCompatActivity(), OnCustomTimerListener {

    lateinit var activity: Activity

    var images = arrayListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_activity)


        //set timer listener for activity tick
        App.shared!!.setTimerListener(this)

        backButton.setOnClickListener({
            //update time locally
            App.shared!!.selectedClaim.activities.get((intent.getIntExtra("selectedIndex",0))).totalElapsedMillis = activity.totalElapsedMillis

            if(!notesEditText.text.toString().equals("")){
                //update notes
                activity.updateNotes(notesEditText.text.toString()) {
                    val intent = Intent(this,
                        ClaimDetailActivity::class.java)
                    startActivity(intent)
                }
            } else {
                val intent = Intent(this,
                    ClaimDetailActivity::class.java)
                startActivity(intent)
            }

        })


        activity = App.shared!!.selectedClaim.activities.get((intent.getIntExtra("selectedIndex",0)))
        titleTextView.text = activity.name

        //load image uploads if available
        loadImages()

        //set previously added notes
        if(!activity.notes.equals("")){
            addedNotesLayout.visibility = View.VISIBLE
            addedNotesTextView.text =  activity.notes
        } else{
            addedNotesLayout.visibility = View.INVISIBLE
        }

        //set current tracking activity if this is active
        if(App.shared!!.activeActivity != null && App.shared!!.activeActivity!!.id.equals(activity.id)){
            activity = App.shared!!.activeActivity!!
            actionButton.setBackgroundResource(R.drawable.stop_large)
            timeTextView.text = activity.formatTime(activity.totalElapsedMillis.toLong())

        } else {
            //not currently tracking
            timeTextView.text = activity.formatTime(activity.totalElapsedMillis.toLong())
        }

        //set play/stop button click
        actionButton.setOnClickListener({
            if (App.shared!!.activeActivity == null && !activity.status.equals("STARTED")){ // START ACTIVITY
                actionButton.setBackgroundResource(R.drawable.stop_large)
                activity.startActivity {
                    if(it){
                        App.shared!!.beginGeoTracking(activity)
                        Handler(Looper.getMainLooper()).post(Runnable {
                            if(activity.name.contains("Driv")){
                                AlertDialog.Builder(this)
                                    .setTitle("Open Navigation")
                                    .setMessage("Would you like to open directions to the claimant address?")
                                    .setPositiveButton(android.R.string.ok) { _, _ -> yesClicked(App.shared!!.selectedClaim.claimant.address.formattedString()) }
                                    .setNegativeButton(android.R.string.cancel) { _, _ -> noClicked() }
                                    .show()
                            }
                        })


                    } else{
                        actionButton.setBackgroundResource(R.drawable.play_large)
                    }
                }
            }
            // STOP Activity
            else if (App.shared!!.activeActivity != null && App.shared!!.activeActivity!!.id.equals(activity.id)){
                actionButton.setBackgroundResource(R.drawable.play_large)
                if(App.shared!!.activeActivity != null){
                    if(!notesEditText.text.toString().equals("")) {
                        activity.updateNotes(notesEditText.text.toString()) {
                            activity.endActivity {
                                App.shared!!.stopGeoTracking(this)
                            }

                        }

                    } else {
                        activity.endActivity {
                            App.shared!!.stopGeoTracking(this)
                        }
                    }

                }

            }
        })



        attachPhotoLayout.setOnClickListener({
            //check runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE);
                }
                else{
                    //permission already granted
                    pickImageFromGallery();
                }
            }
            else{
                //system OS is < Marshmallow
                pickImageFromGallery();
            }
        })
    }

    override fun onTimer() {
        //called from global timer in app instance.
        activity.totalElapsedMillis = App.shared!!.activeActivity!!.totalElapsedMillis
        timeTextView.text = activity.formatTime(activity.totalElapsedMillis.toLong())
    }

    private fun yesClicked(address:String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("google.navigation:q="+address)
        )
        startActivity(intent)

    }
    private fun noClicked() {
        //do nothing
    }


    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        System.out.println(resultCode)
        if (resultCode == -1 && requestCode == IMAGE_PICK_CODE){
            setImage(data?.data!!)
        }
    }

    fun setImage(uri:Uri) {
        attachPhotoTextView.visibility = View.INVISIBLE
        val imageView = ImageView(this)
        imageView.setPadding(2, 2, 2, 2)
        imageView.setImageURI(uri)
        imageView.setScaleType(ScaleType.FIT_XY)
        val layoutParams = LinearLayout.LayoutParams(imageLinearLayout.height, imageLinearLayout.height)
        imageView.setLayoutParams(layoutParams)
        imageLinearLayout.addView(imageView)
    }

    fun loadImages() {
        if(images.size > 0){
            attachPhotoTextView.visibility = View.INVISIBLE

            for (image in images){
                val imageView = ImageView(this)
                imageView.setPadding(2, 2, 2, 2)

                Picasso.get()
                    .load(image)
                    .placeholder(R.drawable.attach_photo)
                    .error(R.drawable.attach_photo)
                    .into(imageView);
                imageView.setScaleType(ScaleType.FIT_XY)
                val layoutParams = LinearLayout.LayoutParams(imageLinearLayout.height, imageLinearLayout.height)
                imageView.setLayoutParams(layoutParams)
                imageLinearLayout.addView(imageView)
            }
        }

    }


}
