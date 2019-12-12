package toplev.com.skyhookaccountability.support

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import toplev.com.skyhookaccountability.activity.auth.SplashActivity
import java.util.concurrent.TimeUnit
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Binder
import toplev.com.skyhookaccountability.activity.main.MainActivity


class BackgroundForegroundLocationService : Service() {
    /*
     * Checks whether the bound activity has really gone away (foreground service with notification
     * created) or simply orientation change (no-op).
     */
    private var configurationChange = false

    private var serviceRunningInForeground = false

    private lateinit var notificationManager: NotificationManager

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var currentLocation: Location? = null

    /*
    * The desired interval for location updates. Inexact. Updates may be
    * more or less frequent.
    */
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = TimeUnit.SECONDS.toMillis(10)

     /*
     * The fastest rate for active location updates. Updates will never be
     * more frequent than this value.
     */
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2


    private val myBinder = MyLocalBinder()


    override fun onCreate() {

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationRequest = LocationRequest()
        locationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                System.out.println("***LOCATION CHANGED")

                if (locationResult?.lastLocation != null) {
                    onNewLocation(locationResult.lastLocation)
                } else {
                    System.out.println("ERROR: Could not parse location.")
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )

        val cancelLocationTrackingFromNotification =
            intent.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)

        // Tells the system not to recreate the service after it's been killed.
        return START_STICKY
    }

    inner class MyLocalBinder : Binder() {
        fun getService() : BackgroundForegroundLocationService {
            return this@BackgroundForegroundLocationService
        }

    }

    override fun onBind(intent: Intent): IBinder? {
//        Log.d(TAG, "onBind()")

        // MainActivity (client) comes into foreground and binds to service, so the service can
        // become a background services.
//        stopForeground(true)
//        serviceRunningInForeground = false
//        configurationChange = false
//        return localBinder

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )

//        val cancelLocationTrackingFromNotification =
//            intent.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)

        return myBinder
    }

    override fun onRebind(intent: Intent) {
//        Log.d(TAG, "onRebind()")
        System.out.println("*** REBINDED *****")

        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
//        stopForeground(true)
//        serviceRunningInForeground = false
//        configurationChange = false
//        super.onRebind(intent)

    }

    override fun onUnbind(intent: Intent): Boolean {
//        Log.d(TAG, "onUnbind()")
        System.out.println("*** UNBINDED *****")

        // MainActivity (client) leaves foreground, so service needs to become a foreground service
        // to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in MainActivity,
        // we do nothing.
//        if (!configurationChange && SharedPreferenceUtil.getLocationTrackingPref(this)) {
//            Log.d(TAG, "Start foreground service")
//            val notification = generateNotification()
//            startForeground(NOTIFICATION_ID, notification)
//            serviceRunningInForeground = true
//        }

        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

    override fun onDestroy() {
        System.out.println("LOCATION SERVICE DESTROYED")
        stopTrackingLocation()

    }


    override fun startForegroundService(service: Intent?): ComponentName? {
        System.out.println("**********FOREGROUND STRTED*******")
        return super.startForegroundService(service)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }



    fun stopTrackingLocation() {
        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    System.out.println("LOCATION LISTENER FINISHED")
                    stopSelf()
                } else {
                    System.out.println("LOCATION LISTENER FAILED TO FINISH")
                }
            }


        } catch (unlikely: SecurityException) {
            System.out.println("Location permissions shut off?")
        }
    }



    private fun onNewLocation(location: Location) {

        currentLocation = location

        //handl location data here!
        System.out.println(location.latitude)
        System.out.println(location.longitude)
        System.out.println(location.time)

        val dataGroup = arrayOf(location.latitude,location.longitude,location.time)
        var path = App.shared!!.sharedPref.getString("path","")
        //starting new path
        if (path == null || path.equals("")){
            App.shared!!.sharedPref.edit().putLong("timeCheck",location.time).apply()
            App.shared!!.sharedPref.edit().putString("latCheck",location.latitude.toString()).apply()
            App.shared!!.sharedPref.edit().putString("longCheck",location.longitude.toString()).apply()

            var newPath = "["
            newPath += dataGroup.get(0).toString() + ", "
            newPath += dataGroup.get(1).toString() + ", "
            newPath += dataGroup.get(2).toString() + "]"
            path = newPath

            App.shared!!.sharedPref.edit().putString("path", path).apply()
        }else {
            //append to created path
            var newPath = "["
            newPath += dataGroup.get(0).toString() + ", "
            newPath += dataGroup.get(1).toString() + ", "
            newPath += dataGroup.get(2).toString() + "]"


            path = path  + ", " + newPath
            App.shared!!.sharedPref.edit().putString("path", path).apply()

        }


        // time check for >4 minutes
        val orginTime =  App.shared!!.sharedPref.getLong("timeCheck",0)

        val difference = location.time - orginTime
        val days = (difference / (1000 * 60 * 60 * 24))
        val hours = ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60))
        val min =
            (difference - 1000 * 60 * 60 * 24 * days - 1000 * 60 * 60 * hours) / (1000 * 60)


        System.out.println("MINUTES PASSED: "+min)
        if(min >= 4){

            //check distance moved
            val origin = Location("")
            origin.latitude = App.shared!!.sharedPref.getString("latCheck","")!!.toDouble()
            origin.longitude = App.shared!!.sharedPref.getString("longCheck","")!!.toDouble()
            val distanceInMeters = origin.distanceTo(location)

            var flag = ""
            if(distanceInMeters < 30 && App.shared!!.activeActivity!!.name.contains("Driv")){
                flag = "NO MOVEMENT DRIVING"
            }
            if(distanceInMeters > 30 && !App.shared!!.activeActivity!!.name.contains("Driv")){
                flag = "UNAPPROVED MOVEMENT"
            }


            if(!flag.equals("")){
                sendNotification("Activity flagged: "+flag)
            }
            //grapqhl call, send to server
            App.shared!!.activeActivity!!.updateGeo(flag, path) {
                if(it){
                    //success
                    App.shared!!.sharedPref.edit().putString("path","").apply()
                    App.shared!!.sharedPref.edit().putLong("timeCheck",0).apply()
                    App.shared!!.sharedPref.edit().putString("latCheck","").apply()
                    App.shared!!.sharedPref.edit().putString("longCheck","").apply()
                }

            }
        }


        // Notify anyone listening for broadcasts about the new location.
//        val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
//        intent.putExtra(EXTRA_LOCATION, location)
//        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
//
//        // Update notification content if running as a foreground service.
//        if (serviceRunningInForeground) {
//            notificationManager.notify(NOTIFICATION_ID, generateNotification())
//        }
    }

    private fun sendNotification(messageBody: String) {

        //First create the notification channel:

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //Notification Channel
            val channelName = "Channel1"
            val importance = NotificationManager.IMPORTANCE_LOW
            @SuppressLint("WrongConstant") val notificationChannel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID, "Channel1", importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)


            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
            val intent = Intent(this, MainActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val pendingIntent = PendingIntent.getActivity(
                this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT
            )


            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(toplev.com.skyhookaccountability.R.mipmap.ic_launcher_round)
                .setContentTitle("Skyhook Accountability")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

            notificationManager.notify(0 /* ID of notifi
            cation */, notificationBuilder.build())

        } else {

            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(
                this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT
            )


            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(toplev.com.skyhookaccountability.R.mipmap.ic_launcher_round)
                .setContentTitle("Skyhook Accountability")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        }

    }


    /*
     * Generates a BIG_TEXT_STYLE Notification that represent latest location.
     */
//    private fun generateNotification(): Notification {
////        Log.d(TAG, "generateNotification()")
//
//        // Main steps for building a BIG_TEXT_STYLE notification:
//        //      0. Get data
//        //      1. Create Notification Channel for O+
//        //      2. Build the BIG_TEXT_STYLE
//        //      3. Set up Intent / Pending Intent for notification
//        //      4. Build and issue the notification
//
//        // 0. Get data
//        val mainNotificationText = currentLocation.toString()
//        val titleText = "Skyhook Accountability"
//
//        // 1. Create Notification Channel for O+ and beyond devices (26+).
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//            val notificationChannel = NotificationChannel(
//                NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT)
//
//            // Adds NotificationChannel to system. Attempting to create an
//            // existing notification channel with its original values performs
//            // no operation, so it's safe to perform the below sequence.
//            notificationManager.createNotificationChannel(notificationChannel)
//        }
//
//        // 2. Build the BIG_TEXT_STYLE.
//        val bigTextStyle = NotificationCompat.BigTextStyle()
//            .bigText(mainNotificationText)
//            .setBigContentTitle(titleText)
//
//        // 3. Set up main Intent/Pending Intents for notification.
//        val launchActivityIntent = Intent(this, SplashActivity::class.java)
//
//        val cancelIntent = Intent(this, BackgroundForegroundLocationService::class.java)
//        cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)
//
//        val servicePendingIntent = PendingIntent.getService(
//            this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//        val activityPendingIntent = PendingIntent.getActivity(
//            this, 0, launchActivityIntent, 0)
//
//        // 4. Build and issue the notification.
//        // Notification Channel Id is ignored for Android pre O (26).
//        val notificationCompatBuilder =
//            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
//
//        return notificationCompatBuilder
//            .setStyle(bigTextStyle)
//            .setContentTitle(titleText)
//            .setContentText(mainNotificationText)
////            .setSmallIcon(R.mipmap.ic_launcher)
//            .setDefaults(NotificationCompat.DEFAULT_ALL)
//            .setOngoing(true)
//            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
////            .addAction(
////                R.drawable.ic_launch, getString(R.string.launch_activity),
////                activityPendingIntent
////            )
////            .addAction(
////                R.drawable.ic_cancel,
////                getString(R.string.disable_foreground_only_location),
////                servicePendingIntent
////            )
//            .build()
//    }



    companion object {

        private const val PACKAGE_NAME = "toplev.com.skyhookaccountability"

        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"

        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"


    }
}

private const val NOTIFICATION_ID = 12345678
private const val NOTIFICATION_CHANNEL_ID = "while_in_use_channel_01"
