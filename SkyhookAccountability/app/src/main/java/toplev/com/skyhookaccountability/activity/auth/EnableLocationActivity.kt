package toplev.com.skyhookaccountability.activity.auth

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import toplev.com.skyhookaccountability.R
import toplev.com.skyhookaccountability.activity.main.MainActivity
import toplev.com.skyhookaccountability.support.App
import java.util.ArrayList


class EnableLocationActivity : AppCompatActivity() {

    lateinit var lm: LocationManager
    val LOCATION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enable_location)

        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check if location permission turned on.
        if(!App.shared!!.locationPermissionsAllowed()){
            val PERMISSIONS: Array<String>
            PERMISSIONS = arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            val permissionsNeeded = ArrayList<String>()
            for (i in PERMISSIONS.indices) {
                val perm = PERMISSIONS[i]
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        perm
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsNeeded.add(perm)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(permissionsNeeded.size > 0)
                    requestPermissions(permissionsNeeded.toTypedArray(), LOCATION_REQUEST_CODE)
            }
            return
        }

    }

    //Continue button
    fun onContinue(view: View){
        //check location turned on, if not open settings, don't let into app
        if (!App.shared!!.isLocationEnabled(this)){
            //open settings for location
            Toast.makeText(this,"You must turn on your location to use Skyhook.",Toast.LENGTH_LONG).show()
            this.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        else if(!App.shared!!.locationPermissionsAllowed()){
            Toast.makeText(this,"You must enable location permissions to use Skyhook.",Toast.LENGTH_LONG).show()
        }
        else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("0", "Permission has been denied by user")
                } else {
                    Log.i("1", "Permission has been granted by user")
                }
            }
        }
    }
}
