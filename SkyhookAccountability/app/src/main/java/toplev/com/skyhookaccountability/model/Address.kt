package toplev.com.skyhookaccountability.model

import com.apollographql.apollo.skyhookaccountability.ClaimsListQuery
import java.io.Serializable

class Address: Serializable {
    var street1 = ""
    var street2 = ""
    var city = ""
    var state = ""
    var zip = ""

    fun loadAddress(addressDetails: ClaimsListQuery.Address){
        street1 = addressDetails.street1()!!
        street2 = addressDetails.street2() ?: ""
        city = addressDetails.city()!!
        state = addressDetails.state()!!
        zip = addressDetails.zip()!!
    }

    fun loadAddress(addressDetails: ClaimsListQuery.Address1){
        street1 = addressDetails.street1()!!
        street2 = addressDetails.street2() ?: ""
        city = addressDetails.city()!!
        state = addressDetails.state()!!
        zip = addressDetails.zip()!!
    }

    fun loadAddress(addressDetails: ClaimsListQuery.Address2){
        street1 = addressDetails.street1()!!
        street2 = addressDetails.street2() ?: ""
        city = addressDetails.city()!!
        state = addressDetails.state()!!
        zip = addressDetails.zip()!!
    }

    fun loadAddress(addressDetails: ClaimsListQuery.Address3){
        street1 = addressDetails.street1()!!
        street2 = addressDetails.street2() ?: ""
        city = addressDetails.city()!!
        state = addressDetails.state()!!
        zip = addressDetails.zip()!!
    }

    fun loadAddress(addressDetails: ClaimsListQuery.Address4){
        street1 = addressDetails.street1()!!
        street2 = addressDetails.street2() ?: ""
        city = addressDetails.city()!!
        state = addressDetails.state()!!
        zip = addressDetails.zip()!!
    }

    fun formattedString(): String{
        return street1 + " " + street2 + ", " + city + ", " + state + " " + zip
    }
}