package toplev.com.skyhookaccountability.activity.claim

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_add_new.backButton
import kotlinx.android.synthetic.main.activity_claim_info.*
import toplev.com.skyhookaccountability.R
import toplev.com.skyhookaccountability.support.App
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T


class ClaimInfoActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(toplev.com.skyhookaccountability.R.layout.activity_claim_info)

        //set back navigation button
        backButton.setOnClickListener({
                val intent = Intent(this,
                    ClaimDetailActivity::class.java)
                startActivity(intent)
        })

        //set data
        setCustomerInfo()
        setFirmInfo()
        setInsuredInfo()
        setClaimantInfo()

    }


    fun setCustomerInfo(){
        customerTextView.text = App.shared!!.selectedClaim.customer.business
        customerContactNameTextView.text = App.shared!!.selectedClaim.customer.fullName
        customerContactPhoneTextView.text = App.shared!!.selectedClaim.customer.phone
    }


    fun setFirmInfo(){
        iaFirmTextView.text = App.shared!!.selectedClaim.firm.business
        iaContactNameTextView.text = App.shared!!.selectedClaim.firm.fullName
        iaContactPhoneTextView.text = App.shared!!.selectedClaim.firm.phone
    }


    fun setInsuredInfo(){
        insuredNameTexView.text = App.shared!!.selectedClaim.insured.fullName
        insuredContactPhoneTextView.text = App.shared!!.selectedClaim.insured.phone
        insuredContactEmailTextView.text = App.shared!!.selectedClaim.insured.email
        insuredContactAddressTextView.text = App.shared!!.selectedClaim.insured.address.formattedString()

        insuredContactPhoneTextView.setOnClickListener({
            try{
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + App.shared!!.selectedClaim.insured.phone))
                startActivity(intent)
            }catch (e:SecurityException){
                //permission issue
                System.out.println(e.message)
            }
        })

        insuredContactEmailTextView.setOnClickListener({
            val emailIntent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", App.shared!!.selectedClaim.insured.email, null
                )
            )
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        })

        insuredContactAddressTextView.setOnClickListener({
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q="+App.shared!!.selectedClaim.insured.address.formattedString())
            )
            startActivity(intent)
        })
    }



    fun setClaimantInfo(){
        claimantNameTexView.text = App.shared!!.selectedClaim.claimant.fullName
        claimantContactPhoneTextView.text = App.shared!!.selectedClaim.claimant.phone
        claimantContactAddressTextView.text = App.shared!!.selectedClaim.claimant.address.formattedString()

        claimantContactPhoneTextView.setOnClickListener({
            try{
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + App.shared!!.selectedClaim.claimant.phone))
                startActivity(intent)
            }catch (e:SecurityException){
                //permission issue
                System.out.println(e.message)
            }
        })

        claimantContactAddressTextView.setOnClickListener({
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q="+App.shared!!.selectedClaim.claimant.address.formattedString())
            )
            startActivity(intent)
        })





        // *** SET LEGAL REP INFO *** //


        claimantLegalNameTexView.text = App.shared!!.selectedClaim.claimant.legalContactName
        claimantLegalContactPhoneTextView.text = App.shared!!.selectedClaim.claimant.legalPhone
        claimantLegalContactAddressTextView.text = App.shared!!.selectedClaim.claimant.legalAddress.formattedString()

        claimantLegalContactPhoneTextView.setOnClickListener({
            try{
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + App.shared!!.selectedClaim.claimant.legalPhone))
                startActivity(intent)
            }catch (e:SecurityException){
                //permission issue
                System.out.println(e.message)
            }
        })

        claimantLegalContactAddressTextView.setOnClickListener({
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q="+App.shared!!.selectedClaim.claimant.legalAddress.formattedString())
            )
            startActivity(intent)
        })
    }
}
