package toplev.com.skyhookaccountability.activity.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.View
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.skyhookaccountability.LoginUserMutation
import kotlinx.android.synthetic.main.activity_begin_auth.*
import toplev.com.skyhookaccountability.model.User
import toplev.com.skyhookaccountability.support.App



class BeginAuthActivity : AppCompatActivity() {

    private var phase = 0
    private var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(toplev.com.skyhookaccountability.R.layout.activity_begin_auth)

        editText.setFocusableInTouchMode(true);
        editText.requestFocus();


    }



    //Continue button
    fun onContinue(view: View){
        when(phase){
            0 -> requestPassword()
            1 -> loginUser()
        }

    }

    fun requestPassword(){
        if(emailValidated()){
            email = editText.text.toString()
            phase = 1
            segmentSwitch.isChecked
            editText.setText("")
            subHeaderTextView.setText("Enter your password to log back in")
            editText.hint = "Enter your password"
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            editText.setFocusableInTouchMode(true);
            editText.requestFocus();
        }


    }

    fun emailValidated(): Boolean {
        errorTextView.setText("")
        email = ""
        if (TextUtils.isEmpty(editText.text) || !android.util.Patterns.EMAIL_ADDRESS.matcher(editText.text).matches()) {
            errorTextView.setText("Please enter a valid email")
            return false;
        }

        return true

    }

    fun passwordValidated(): Boolean {
        errorTextView.setText("")
        if(editText.text.toString().equals("") || editText.text.toString().length < 6) {
            errorTextView.setText("Password must be 6 or more characters long")
            return false
        }

        return true
    }


    fun loginUser(){
        if(!passwordValidated())
            return

        showLoading(true)
        val password = editText.text.toString()

        showLoading(true)
        System.out.println(email.trim())
        System.out.println(password.trim())

        User().loginUser(email.trim(),password.trim()) {
            showLoading(false)
            if(it){
                goToNext()
            } else{
                errorTextView.setText("Login not recognized")
            }
        }

    }


    fun showLoading(show:Boolean){
        if (show){
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.INVISIBLE

        }

    }

    fun goToNext() {
        val intent = Intent(this, EnableLocationActivity::class.java)
        startActivity(intent)
    }
}

