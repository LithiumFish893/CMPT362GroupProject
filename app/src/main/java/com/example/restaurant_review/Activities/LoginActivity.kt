package com.example.restaurant_review.Activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.restaurant_review.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Activity that lets the user login.
 */
class LoginActivity:AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailTV: EditText
    private lateinit var passwordTV: EditText
    private lateinit var loginButton: Button
    private lateinit var googleSignInButton: ImageButton
    private lateinit var signUpButton: TextView
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstance: Bundle?){
        super.onCreate(savedInstance)
        setContentView(R.layout.activity_login)

        emailTV = findViewById(R.id.email_input)
        passwordTV = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        signUpButton = findViewById(R.id.sign_up)
        signUpButton = findViewById(R.id.sign_up)
        googleSignInButton = findViewById(R.id.google_login)
        auth = FirebaseAuth.getInstance()

        val termText = findViewById<View>(R.id.Terms) as TextView

        termText.setOnClickListener(){
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/LithiumFish893/CMPT362GroupProject/blob/kotlin_code/EULA")
            startActivity(intent)
        }
        val privatePolicyText = findViewById<View>(R.id.Privacy) as TextView

        privatePolicyText.setOnClickListener(){
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/LithiumFish893/CMPT362GroupProject/blob/kotlin_code/Privacy")
            startActivity(intent)
        }

        emailTV.setOnFocusChangeListener(){
                view, focus->
            if (view == emailTV && focus){
                emailTV.error = null
            }
        }

        loginButton.setOnClickListener(){
            loginUser()
        }
        signUpButton.setOnClickListener(){
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("867579589217-220v84ojps0f70vv52478kc52pgr4l33.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        googleSignInButton.setOnClickListener{
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, 100)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.result
                loginWithGoogle(account)
            }
            catch (e:Exception){
                Toast.makeText(this,"Error. Google Sign in failed", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onBackPressed() {
        finishAffinity()
    }


    private fun loginWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(this,"Success!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                Toast.makeText(this,"Error!" + it.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(){
        val email = emailTV.text.toString()
        val password = passwordTV.text.toString()
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(email.isEmpty()){
            //emailTV.requestFocus()
            emailTV.error = "Email cannot be empty!"
            imm.hideSoftInputFromWindow(emailTV.windowToken, 0)

        }else if(password.isEmpty()){
            //passwordTV.requestFocus()
            passwordTV.error = "Password cannot be empty!"
            imm.hideSoftInputFromWindow(passwordTV.windowToken, 0)

        }else{
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this){
                if(it.isSuccessful){
                    Toast.makeText(this,"Success!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                }else{
                    if (it.exception!!.message=="The password is invalid or the user does not have a password.") {
                        //passwordTV.requestFocus()
                        passwordTV.error = "Invalid password!"
                        imm.hideSoftInputFromWindow(passwordTV.windowToken, 0)
                    }else if (it.exception!!.message=="The email address is badly formatted.") {
                        //emailTV.requestFocus()
                        emailTV.error = "Email address is badly formatted!"
                        imm.hideSoftInputFromWindow(emailTV.windowToken, 0)
                    }else{
                        Toast.makeText(this,"Error!" + it.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

}