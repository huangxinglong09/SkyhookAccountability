package toplev.com.skyhookaccountability.model

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.skyhookaccountability.ClaimsListQuery
import com.apollographql.apollo.skyhookaccountability.LoginUserMutation
import com.apollographql.apollo.skyhookaccountability.UpdateActivityEndMutation
import com.apollographql.apollo.skyhookaccountability.UpdateClaimEndMutation
import okhttp3.OkHttpClient
import toplev.com.skyhookaccountability.support.App
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Claim: Serializable {

    var id = ""
    var claimNumber = ""
    var claimant = User()
    var insured = User()
    var customer = User()
    var firm = User()
    var activities = ArrayList<Activity>()
    var status = ""
    var claimDate = ""
    var dueDate = ""




    fun loadClaim(claimDetails: ClaimsListQuery.Node) {
        this.id = claimDetails.id()
        this.claimNumber = claimDetails.claimNumber()!!
        this.claimDate = SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).format(claimDetails.claimDate() ?: Date())
        this.dueDate = SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).format(claimDetails.dueDate() ?: Date())

        try{
            this.claimant.loadClaimant(claimDetails.claimant()!!)
        }catch (e:Exception){
            //no claimaint assigned.
        }


        try{
            this.insured.loadInsured(claimDetails.insured()!!)
        }catch (e:Exception){
            //no insured assigned.
        }

        try {
            this.customer.loadCustomer(claimDetails.customer()!!)
        } catch (e: Exception){
            // no customer
        }

        try{
            this.firm.loadFirm(claimDetails.ia()!!)
        }catch (e:Exception){
            //no insured assigned.
        }

        this.status = claimDetails.status()!!.name

        if (claimDetails.activities() != null){
            for(act in claimDetails.activities()!!.edges()!!) {
                val activity = Activity()
                activity.loadActivity(act.node()!!)
                activities.add(activity)
            }
        }



    }


    //graphql claim fetch
    fun fetchClaims(callback: (ArrayList<Claim>?) -> Unit){

        //Query available claims for current user
        val claimsQuery = ClaimsListQuery()

        App.shared!!.apollo.query(claimsQuery).enqueue(object : ApolloCall.Callback<ClaimsListQuery.Data>() {

            override fun onFailure(e: ApolloException) {
                System.out.println("Failed to fetch claims.... "+e.localizedMessage);
                callback(null)
            }


            override fun onResponse(response: Response<ClaimsListQuery.Data>) {
                System.out.println("Claims Found...");

                if (response.data()!!.claims() != null) {
                    val claimsEdges = response.data()!!.claims()!!.edges()!!
                    val claimsList = ArrayList<Claim>()
                    for (node in claimsEdges){
                        val claim = Claim()
                        claim.loadClaim(node.node()!!)
                        claimsList.add(claim)
                    }

                    System.out.println("Claims found: "+claimsList.size)

                    //return claims
                    callback(claimsList)

                }
            }
        })

    }


    fun closeClaim(callback: (Boolean) -> Unit){
         //starting fresh
            val mutation = UpdateClaimEndMutation(this.id)
            App.shared!!.apollo.mutate(mutation).enqueue(object : ApolloCall.Callback<UpdateClaimEndMutation.Data>() {

                override fun onFailure(e: ApolloException) {
                    System.out.println("Failed to update claim to closed... "+e.localizedMessage);
                    callback(false)
                }

                override fun onResponse(response: Response<UpdateClaimEndMutation.Data>) {

                    System.out.println(response.data().toString())
                    if (response.data()!!.updateClaimEnd()!!.success()) {
                        //success.. finished activity
                        this@Claim.status = "CLOSED"

                        callback(true)

                    } else {
                        //failed
                        callback(false)
                    }
                }
            })



    }


}