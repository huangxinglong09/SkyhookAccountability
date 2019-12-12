package toplev.com.skyhookaccountability.activity.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import toplev.com.skyhookaccountability.R
import toplev.com.skyhookaccountability.activity.main.MainActivity
import toplev.com.skyhookaccountability.model.User
import toplev.com.skyhookaccountability.support.App

class SplashActivity : AppCompatActivity() {
    // *** Splash page when launching app *** //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //check if user instance saved to login or if not go to login screen
        Handler().postDelayed({
            if(App.shared!!.userExists()){
                loginUser()
            } else {
               goToLogin()
            }

        }, 2000)
    }

    fun loginUser(){
        //login user to server
        val email = App.shared!!.sharedPref.getString("email","")
        val password = App.shared!!.sharedPref.getString("password","")

        User().loginUser(email!!, password!!){
            if(it){
                //login success, check if location turned on
                if(App.shared!!.isLocationEnabled(this) && App.shared!!.locationPermissionsAllowed()){
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    this.finish()

                } else {
                    val intent = Intent(this, EnableLocationActivity::class.java)
                    startActivity(intent)
                    this.finish()

                }


            } else{
                //login failed, take to login
                goToLogin()
            }
        }

    }


    fun goToLogin(){
        val intent = Intent(this, BeginAuthActivity::class.java)
        startActivity(intent)
        this.finish()
    }

}
