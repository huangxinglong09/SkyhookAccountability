package toplev.com.skyhookaccountability.support

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import toplev.com.skyhookaccountability.model.User
import com.apollographql.apollo.ApolloClient
import com.google.android.gms.location.*
import okhttp3.OkHttpClient
import okhttp3.Request
import toplev.com.skyhookaccountability.activity.main.MainActivity
import toplev.com.skyhookaccountability.model.Claim
import java.util.concurrent.TimeUnit
import android.os.CountDownTimer
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import toplev.com.skyhookaccountability.support.App.CountUpTimer
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import java.lang.Exception
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.content.ComponentName
import android.os.IBinder
import android.content.ServiceConnection
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.apollographql.apollo.skyhookaccountability.type.CustomType


interface OnCustomTimerListener {
    fun onTimer()    //method, which can have parameters
}

//App Launch Instance Class
class App: Application() {

    //app launch instance
    companion object {
        var shared: App? = null
    }

    //graphql
    val BASE_URL = "http://sky-dev.ambrosia-med.com/graphql"
    lateinit var apollo: ApolloClient

    //local storage
    private var PRIVATE_MODE = 0
    private val PREF_NAME = "toplev.com.skyhookaccountability"
    lateinit var sharedPref: SharedPreferences

    //user instance management
    lateinit var user: User

    // *** used for managing currently viewed claim and active activity being tracked
    lateinit var selectedClaim: Claim
    var activeActivity: toplev.com.skyhookaccountability.model.Activity? = null
    lateinit var timer : CountUpTimer
    lateinit var tickListener : OnCustomTimerListener

    // add check for devices with Android 10.
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    var myService: BackgroundForegroundLocationService? = null
    var isBound = false
    val myConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            val binder = service as BackgroundForegroundLocationService.MyLocalBinder
            myService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }


    override fun onCreate() {
        super.onCreate()
        shared = this

        val okHttpClient = OkHttpClient.Builder().addNetworkInterceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .build()
            chain.proceed(request)
        }.build()


        apollo= ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttpClient)
            .addCustomTypeAdapter(CustomType.DATE, DateTimeApolloAdapter)
            .build()


        sharedPref = getSharedPreferences(PREF_NAME, PRIVATE_MODE)


        timer = object : CountUpTimer(100000000) {
            override fun onTick(second: Int) {
                try {
                    activeActivity!!.totalElapsedMillis = second * 1000
                    tickListener.onTimer()

                } catch(e: Exception){
                    //no acitivty?
                }
            }
        }

    }

    fun setTimerListener(mListener: OnCustomTimerListener) {
        this.tickListener = mListener
    }

    fun setHeader(jwt: String){

        val okHttpClient = OkHttpClient.Builder().addNetworkInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer "+jwt)
                .build()
            chain.proceed(request)
        }.build()


        apollo= ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttpClient)
            .addCustomTypeAdapter(CustomType.DATE, DateTimeApolloAdapter)
            .build()

    }


    fun userExists(): Boolean{
        if(sharedPref.getString("email",null) != null){
            return true
        }

        return false
    }

    fun isLocationEnabled(context: Context): Boolean {

        //device location turned off
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) return false

        //good to go
        return true

    }


    fun locationPermissionsAllowed():Boolean {
        //app location permissions turned off
        val backgroundPermissionAllowed =
            if (runningQOrLater) {
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

        return backgroundPermissionAllowed
    }


//    //start tracking user's geo data and time data
    fun beginGeoTracking(activity: toplev.com.skyhookaccountability.model.Activity) {
        activeActivity = activity
        timer.start()

        val intent = Intent(this, BackgroundForegroundLocationService::class.java)
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE)

    }


    fun stopGeoTracking(context: Context) {
        activeActivity = null
        timer.cancel()

        unbindService(myConnection)

        val intent = Intent(context, BackgroundForegroundLocationService::class.java)
        context.stopService(intent)
    }





    // COUNT UP TIMER CLASS
    abstract inner class CountUpTimer protected constructor(private val duration: Long) :
        CountDownTimer(duration, 1000) {

        abstract fun onTick(second: Int)

        override fun onTick(msUntilFinished: Long) {
            val second = ((duration - msUntilFinished) / 1000).toInt()
            onTick(second)
        }

        override fun onFinish() {
            onTick(duration / 1000)
        }


    }
}
