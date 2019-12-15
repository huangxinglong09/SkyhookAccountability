package toplev.com.skyhookaccountability.model

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.skyhookaccountability.ClaimsListQuery
import com.apollographql.apollo.skyhookaccountability.LoginUserMutation
import kotlinx.android.synthetic.main.activity_begin_auth.*
import toplev.com.skyhookaccountability.support.App
import java.io.Serializable
import java.lang.Exception


class User: Serializable {

    var id = ""
    var fullName = ""
    var email = ""
    var jwt = ""

    var business = ""
    var phone = ""
    var phoneB = ""
    var phoneM = ""
    var address = Address()

    var legalBusiness = ""
    var legalContactName = ""
    var legalPhone = ""
    var legalAddress = Address()


    fun loadUser(userDetails: LoginUserMutation.User) {
        this.id = userDetails.id()
        this.fullName = userDetails.fullName()!!
        this.email = userDetails.email()!!
        this.jwt = userDetails.jwt()!!

    }

    fun loadClaimant(userDetails: ClaimsListQuery.Claimant){
        this.fullName = userDetails.name()
        this.phone = userDetails.phone()
        this.address.loadAddress(userDetails.address())
        try {
            loadLegal(userDetails.legal ()!!)
        }
        catch (e: Exception){
            //no legal rep set
        }
    }

    fun loadLegal(legalDetails: ClaimsListQuery.Legal){
        this.legalBusiness = legalDetails.name()
        this.legalContactName= legalDetails.contact()
        this.legalPhone = legalDetails.phone()
        this.legalAddress.loadAddress(legalDetails.address())
    }


    fun loadInsured(userDetails: ClaimsListQuery.Insured){
        this.fullName = userDetails.name()
        this.email = userDetails.email()
        this.phone = userDetails.phoneM()
        this.address.loadAddress(userDetails.address())
    }

    fun loadCustomer(userDetails: ClaimsListQuery.Customer){
        this.business = userDetails.customerName()!!
        this.fullName = userDetails.customerContact()!!
        this.phone = userDetails.phone()!!
        this.address.loadAddress(userDetails.address()!!)
    }

    fun loadFirm(userDetails: ClaimsListQuery.Ia) {
        this.business = userDetails.iaName()!!
        this.fullName = userDetails.iaContact()!!
        this.phone = userDetails.phone()!!
        this.address.loadAddress(userDetails.address()!!)
    }

    fun resetUser(){
        this.id = ""
        this.fullName = ""
        this.email = ""
        this.jwt = ""
    }

    //graphql login
    fun loginUser(email:String, password: String, callback: (Boolean) -> Unit){

        //login user to server
        val loginUser = LoginUserMutation(email,password)
        App.shared!!.apollo.mutate(loginUser).enqueue(object : ApolloCall.Callback<LoginUserMutation.Data>() {

            override fun onFailure(e: ApolloException) {
                callback(false)
                System.out.println("Failed....");
                System.out.println(e.message)

            }

            override fun onResponse(response: Response<LoginUserMutation.Data>) {

                if (response.data()!!.login() != null) {
                    System.out.println("Success...");

                    App.shared!!.sharedPref.edit().putString("email",email).apply()
                    App.shared!!.sharedPref.edit().putString("password",password).apply()

                    App.shared!!.user = User()
                    val userDetails = response.data()!!.login()!!.user()
                    App.shared!!.user.loadUser(userDetails!!)

                    //set header for graphql calls
                    App.shared!!.setHeader(App.shared!!.user.jwt)

                    callback(true)


                } else {
                    callback(false)
                }
            }
        })

    }

}