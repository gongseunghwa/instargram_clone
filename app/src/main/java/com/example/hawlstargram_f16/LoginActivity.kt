package com.example.hawlstargram_f16

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient?=null
    var  GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        email_login_button.setOnClickListener{
            singinAndSignup()
        }
        google_signin_button.setOnClickListener {
            //First step
            googleLogin()
            Log.d("스텝","스텝 1 ok")
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)



    }

    public override fun onStart(){
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if(account!=null){
            moveMainPage(auth?.currentUser)
        }
    }

    fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
        Log.d("스텝","스텝 2ok")

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_LOGIN_CODE){
           val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            }catch(e:ApiException){
                Log.w("LoginActivity","Google sign in failed")
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) {
                    task->
                if(task.isSuccessful){
                    //Login
                    moveMainPage(auth?.currentUser)
                    Log.d("스텝","스텝 4ok")
                }else{
                    //Show the error message
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
            }

    }

    fun singinAndSignup(){
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(),password_edittext.text.toString())
            ?.addOnCompleteListener{
            task->
                if(task.isSuccessful){
                    //Creating a user account
                    moveMainPage(task.result?.user)
                }else if(task.exception?.message.isNullOrEmpty()){
                    //Show the error message
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }else{
                    //Login if you have account
                    signinEmail()
                }
        }
    }
    fun signinEmail(){
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(),password_edittext.text.toString())
            ?.addOnCompleteListener {
                task->
                if(task.isSuccessful){
                    //Login
                    moveMainPage(task.result?.user)
                }else{
                    //Show the error message
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
            }
    }

    fun moveMainPage(user:FirebaseUser?){
        if(user != null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
}