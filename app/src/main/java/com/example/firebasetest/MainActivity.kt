package com.example.firebasetest

import android.R.attr
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = "MainActivity"
        private val RC_SIGN_IN = 100
        private val RC_SIGN_OUT = 110
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var gSignInOpts: GoogleSignInOptions
    private lateinit var googleAuthClient: GoogleSignInClient
    private lateinit var email:String
    private lateinit var provider:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        gSignInOpts = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleAuthClient = GoogleSignIn.getClient(this, gSignInOpts)

        setup()
    }

    private fun setup() {
        registerButton.setOnClickListener {
            if (emailTextView.text.isNotEmpty() && passwordTextView.text.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(
                    emailTextView.text.toString(),
                    passwordTextView.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Se ha registrado satisfactoriamente.",
                            Toast.LENGTH_SHORT
                        ).show()

                        email = auth.currentUser?.email.toString()
                        provider = auth.currentUser?.providerId.toString()

                        nextActivity()
                    } else {
                        Toast.makeText(
                            this,
                            "Error al registrarse",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        loginButton.setOnClickListener {
            if (emailTextView.text.isNotEmpty() && passwordTextView.text.isNotEmpty()) {
                auth.signInWithEmailAndPassword(
                    emailTextView.text.toString(),
                    passwordTextView.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Se ha iniciado sesión satisfactoriamente.",
                            Toast.LENGTH_SHORT
                        ).show()

                        email = auth.currentUser?.email.toString()
                        provider = auth.currentUser?.providerId.toString()

                        nextActivity()
                    } else {
                        Toast.makeText(
                            this,
                            "Error al iniciar sesión",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        googleLoginButton.setOnClickListener {
            startActivityForResult(googleAuthClient.signInIntent, RC_SIGN_IN)
        }

        anonymousButton.setOnClickListener {
            auth.signInAnonymously().addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Se ha iniciado sesión satisfactoriamente.",
                        Toast.LENGTH_SHORT
                    ).show()

                    email = auth.currentUser?.email.toString()
                    provider = auth.currentUser?.providerId.toString()

                    nextActivity()
                } else {
                    Toast.makeText(
                        this,
                        "Error al iniciar sesión",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                    Toast.makeText(
                        this,
                        "Se ha registrado satisfactoriamente.",
                        Toast.LENGTH_SHORT
                    ).show()

                    email = auth.currentUser?.email.toString()
                    provider = auth.currentUser?.providerId.toString()

                    nextActivity()
                } catch (e: ApiException) {
                    Log.w(TAG, "Google sign in failed", e)
                    Toast.makeText(
                        this,
                        "Error al iniciar sesión",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun nextActivity() {
        val nextActivityIntent = Intent(this, GameActivity::class.java)
        startActivity(nextActivityIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        auth.signOut()
    }
}