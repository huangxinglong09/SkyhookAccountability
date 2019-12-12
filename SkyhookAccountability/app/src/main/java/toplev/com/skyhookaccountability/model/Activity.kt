package toplev.com.skyhookaccountability.model

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.skyhookaccountability.*
import com.apollographql.apollo.skyhookaccountability.type.ActivityStatus
import com.apollographql.apollo.skyhookaccountability.type.ActivityType
import toplev.com.skyhookaccountability.support.App
import java.io.Serializable
import java.util.concurrent.TimeUnit


class Activity: Serializable {


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



//    fun uploadImage(image: String, callback: (Boolean) -> Unit){

//        //Push geo and time data
//        val mutation =
//
//        App.shared!!.apollo.mutate(mutation).enqueue(object : ApolloCall.Callback<UpdateNotesMutation.Data>() {
//
//            override fun onFailure(e: ApolloException) {
//                System.out.println("Failed to update notes... "+e.localizedMessage);
//                callback(false)
//            }
//
//            override fun onResponse(response: Response<UpdateNotesMutation.Data>) {
//                System.out.println("Notes pushed...");
//
//                System.out.println(response.data().toString())
//
//                if (response.data()!!.updateActivityNotes() != null && response.data()!!.updateActivityNotes()!!.success()) {
//                    //success.. added notes
//                    response.data()!!.updateActivityNotes()
//                    if(!this@Activity.notes.equals("")){
//                        this@Activity.notes = this@Activity.notes+"\n"+notes
//                    } else {
//                        this@Activity.notes = notes
//                    }
//                    callback(true)
//
//                } else {
//                    //failed
//                    callback(false)
//                }
//            }
//        })

//    }




}