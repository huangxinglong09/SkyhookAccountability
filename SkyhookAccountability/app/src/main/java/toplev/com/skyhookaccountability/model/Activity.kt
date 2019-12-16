package toplev.com.skyhookaccountability.model

import android.util.Log
import android.webkit.MimeTypeMap
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.skyhookaccountability.*
import com.apollographql.apollo.skyhookaccountability.type.ActivityStatus
import com.apollographql.apollo.skyhookaccountability.type.ActivityType
import okhttp3.*
import okio.Buffer
import okio.BufferedSource
import org.json.JSONObject
import toplev.com.skyhookaccountability.support.App
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit


class Activity: Serializable {
    private val TAG = "modelActivity"

    var id = ""
    var name = ""
    var totalElapsedMillis = 0
    var flags:String = ""
    var notes:String = ""
    var file:String = ""
    var status = ""
    var uploads = ArrayList<String>()

    fun loadActivity(activityDetails: ClaimsListQuery.Node1){

        this.id = activityDetails.id()
        this.name = activityDetails.name()!!
        this.flags = activityDetails.flags() ?: ""
        this.totalElapsedMillis = activityDetails.totalElapsedMillis() ?: 0
        this.notes = activityDetails.notes() ?: ""
        this.status = activityDetails.status()!!.name
//        this.uploads = activityDetails

    }

    fun loadActivity(activityDetails: CreateActivityMutation.Activity){

        this.id = activityDetails.id()
        this.name = activityDetails.name()!!
        this.flags = activityDetails.flags() ?: ""
        this.totalElapsedMillis = activityDetails.totalElapsedMillis() ?: 0
        this.notes = activityDetails.notes() ?: ""
        this.status = activityDetails.status()!!.name

    }


    fun formatTime(miliSeconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(miliSeconds).toInt() % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(miliSeconds).toInt() % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds).toInt() % 60
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%02d:%02d", minutes, seconds)
            seconds > 0 -> String.format("00:%02d", seconds)
            else -> {
                "00:00:00"
            }
        }
    }



    fun addNewActivity(claimId:String, name:String, callback: (Activity?) -> Unit){

        //set the type
        var type: ActivityType = ActivityType.ONSITE
        if (name.contains("Driv")){
            type = ActivityType.TRAVEL
        }

        val mutation = CreateActivityMutation(claimId, name, Input.optional(ActivityStatus.PENDING), Input.optional(type))

        App.shared!!.apollo.mutate(mutation).enqueue(object : ApolloCall.Callback<CreateActivityMutation.Data>() {

            override fun onFailure(e: ApolloException) {
                System.out.println("Failed to create activity.... "+e.localizedMessage);
                callback(null)
            }

            override fun onResponse(response: Response<CreateActivityMutation.Data>) {
                System.out.println("Activity created...");

                if (response.data()!!.createActivity() != null) {
                    val activity = Activity()
                    activity.loadActivity(response.data()!!.createActivity()!!.activity()!!)
                    callback(activity)

                } else {
                    //failed
                    callback(null)
                }
            }
        })

    }


    fun startActivity(callback: (Boolean) -> Unit) {

        App.shared!!.sharedPref.edit().putString("path","").apply()
        App.shared!!.sharedPref.edit().putString("timeCheck","").apply()
        App.shared!!.sharedPref.edit().putString("latCheck","").apply()
        App.shared!!.sharedPref.edit().putString("longCheck","").apply()

        //starting fresh
        val mutation = UpdateActivityStartMutation(this.id)

        App.shared!!.apollo.mutate(mutation).enqueue(object : ApolloCall.Callback<UpdateActivityStartMutation.Data>() {

            override fun onFailure(e: ApolloException) {
                System.out.println("Failed to start activity.... "+e.localizedMessage);
                callback(false)
            }

            override fun onResponse(response: Response<UpdateActivityStartMutation.Data>) {
                System.out.println("Activity started...");

                System.out.println(response.data().toString())

                if (response.data()!!.updateActivityStart()!!.success()) {
                    //success.. started activity
                    App.shared!!.activeActivity = this@Activity
                    this@Activity.status = "STARTED"
                    callback(true)

                } else {
                    //failed
                    callback(false)
                }
            }
        })

    }


    fun endActivity(callback: (Boolean) -> Unit){
        val path = App.shared!!.sharedPref.getString("path","")

            App.shared!!.activeActivity!!.updateGeo("",path?:"") {
                if(it){
                    //success
                    App.shared!!.sharedPref.edit().putString("path","").apply()
                    App.shared!!.sharedPref.edit().putString("timeCheck","").apply()
                    App.shared!!.sharedPref.edit().putString("latCheck","").apply()
                    App.shared!!.sharedPref.edit().putString("longCheck","").apply()

                }
                System.out.println("FINAL GEO UPDATED")

                //starting fresh
                val mutation = UpdateActivityEndMutation(this.id)

                App.shared!!.apollo.mutate(mutation).enqueue(object : ApolloCall.Callback<UpdateActivityEndMutation.Data>() {

                    override fun onFailure(e: ApolloException) {
                        System.out.println("Failed to update end... "+e.localizedMessage);
                        callback(false)
                    }

                    override fun onResponse(response: Response<UpdateActivityEndMutation.Data>) {
                        System.out.println("Activity finished...");

                        System.out.println(response.data().toString())
                        if (response.data()!!.updateActivityEnd()!!.success()) {
                            //success.. finished activity
                            this@Activity.status = "COMPLETE"

                            callback(true)

                        } else {
                            //failed
                            callback(false)
                        }
                    }
                })
            }


    }


    fun updateGeo(flag: String, path: String, callback: (Boolean) -> Unit){

        System.out.println("PUSHING GEO DATA-->")
        System.out.println("ID: "+this.id)
        System.out.println("FLAG: "+flag)
        System.out.println("PATH: "+path)


        //Push geo and time data
        val mutation = UpdateActivityGeoInputMutation(this.id, path, flag)

        System.out.println(mutation.queryDocument())

        App.shared!!.apollo.mutate(mutation).enqueue(object : ApolloCall.Callback<UpdateActivityGeoInputMutation.Data>() {

            override fun onFailure(e: ApolloException) {
                System.out.println("Failed to update geo data... "+e.localizedMessage);
                callback(false)
            }

            override fun onResponse(response: Response<UpdateActivityGeoInputMutation.Data>) {
                System.out.println("Geo data pushed...");

                System.out.println(response.data()!!.updateActivityGeo())

                if (response.data()!!.updateActivityGeo() != null && response.data()!!.updateActivityGeo()!!.success()) {
                    //success.. finished activity
                    callback(true)

                } else {
                    //failed
                    callback(false)
                }
            }
        })

    }



    fun updateNotes(notes: String, callback: (Boolean) -> Unit){

        //Push geo and time data
        val mutation = UpdateNotesMutation(this.id, notes)

        App.shared!!.apollo.mutate(mutation).enqueue(object : ApolloCall.Callback<UpdateNotesMutation.Data>() {

            override fun onFailure(e: ApolloException) {
                System.out.println("Failed to update notes... "+e.localizedMessage);
                callback(false)
            }

            override fun onResponse(response: Response<UpdateNotesMutation.Data>) {
                System.out.println("Notes pushed...");

                System.out.println(response.data().toString())

                if (response.data()!!.updateActivityNotes() != null && response.data()!!.updateActivityNotes()!!.success()) {
                    //success.. added notes
                    response.data()!!.updateActivityNotes()
                    if(!this@Activity.notes.equals("")){
                        this@Activity.notes = this@Activity.notes+"\n"+notes
                    } else {
                        this@Activity.notes = notes
                    }
                    callback(true)

                } else {
                    //failed
                    callback(false)
                }
            }
        })

    }


    fun uploadImage(fileToUpload: File, callback: (Boolean) -> Unit) {
       var mimeType = "image/jpeg"
        val extension = MimeTypeMap.getFileExtensionFromUrl(fileToUpload.path)
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)!!
        }

        val mutation = ActivityFileUploadMutation.builder()
//            .file(FileUpload(mimeType, file))
            .file(fileToUpload.path)
            .activityId(this.id)
            .build()

        val operationDefinition = "{\"query\": \"" +
                "mutation ActivityFileUpload(\$activityId: ID!, \$file: Upload!) { updateActivityUpload(input: {activityId: \$activityId, file: \$file}) { upload { id path } } }" +
                "\", \"variables\": { \"activityId\": \"" +
                this.id +
                "\" ,\"file\": null } }"


        try {
            val okHttpClient = OkHttpClient.Builder().addNetworkInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .addHeader("Authorization", "Bearer " + App.shared!!.user.jwt)
                    .build()
                chain.proceed(request)
            }.build()

            Log.i(TAG, "fileToUpload name >>> " + fileToUpload.name)
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("operations", operationDefinition)
                .addFormDataPart("map", "{ \"0\": [\"variables.file\"] }")
                .addFormDataPart(
                    "0",
                    fileToUpload.name,
                    RequestBody.create(MediaType.parse("image/png"), fileToUpload)
                ).build()

            val request = Request.Builder()
                .url(App.shared!!.BASE_URL)
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    if (response.isSuccessful) {
                        Log.i(TAG, "success")

                        val jsonObject = JSONObject()
                        try {
                            jsonObject.put("code", 200)
                            jsonObject.put("status", "OK")
                            jsonObject.put("message", "Successful")

                            val contentType: MediaType? = response.body()!!.contentType()

                            val source: BufferedSource = response.body()!!.source()
                            source.request(Long.MAX_VALUE)
                            val buffer: Buffer = source.buffer()

                            val responseBodyString = buffer.clone().readString(Charset.forName("UTF-8"))
                            val jsonObject: JSONObject = JSONObject(responseBodyString)
                            val data = jsonObject.getJSONObject("data")
                            val updateActivityUpload = data.getJSONObject("updateActivityUpload")
                            val upload = updateActivityUpload.getJSONObject("upload")
                            val pathString = upload.getString("path")

                            Log.i(TAG, "path >>> $pathString")
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                        callback(true)
                    } else {
                        Log.i(TAG, "fail")
                        callback(false)
                    }
                }

            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }


//        App.shared!!.apollo.mutate(mutation).enqueue(object : ApolloCall.Callback<ActivityFileUploadMutation.Data>() {
//
//            override fun onFailure(e: ApolloException) {
//                println("Failed to update notes... "+e.localizedMessage);
//                Log.i(TAG, "onFailure")
//                callback(false)
//            }
//
//            override fun onResponse(response: Response<ActivityFileUploadMutation.Data>) {
//                println("Notes pushed...")
//                println(response.data().toString())
//                Log.i(TAG, "success")
//
//                if (response.data()!!.updateActivityUpload() != null) {
//                    //success.. added notes
//
//                    Log.i(TAG, "success")
//                    callback(true)
//
//                } else {
//                    //failed
//                    Log.i(TAG, "fail")
//                    callback(false)
//                }
//            }
//        })

    }




}