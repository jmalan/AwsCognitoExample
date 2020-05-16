package com.example.awscognitoexample.activity

import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.awscognitoexample.R
import com.example.awscognitoexample.viewmodel.MainActivityViewModel
import org.w3c.dom.Text
import kotlin.math.sign

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    private val viewModel: MainActivityViewModel by viewModels()

    private var signedUserNameTextView: TextView? = null
    private var signedUserEmailTextView: TextView? = null
    private var signInButton: Button? = null
    private var signInGoogleButton: Button? = null
    private var signOutButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        signedUserNameTextView = findViewById(R.id.signed_user_name_textview)
        signedUserEmailTextView = findViewById(R.id.signed_user_email_textview)
        signInButton = findViewById(R.id.sign_in_button)
        signInGoogleButton = findViewById(R.id.sign_in_google_button)
        signOutButton = findViewById(R.id.sign_out_button)

        viewModel.signed.observe(this, Observer {
            signInButton?.visibility = if (it) View.GONE else View.VISIBLE
            signInGoogleButton?.visibility = if (it) View.GONE else View.VISIBLE
            signOutButton?.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.signedUserName.observe(this, Observer {
            signedUserNameTextView?.text = it
        })
        viewModel.signedUserEmail.observe(this, Observer {
            signedUserEmailTextView?.text = it
        })

        signInButton?.setOnClickListener {
            viewModel.signInCognitoUser()
        }

        signInGoogleButton?.setOnClickListener {
            viewModel.signInGoogleUser()
        }

        signOutButton?.setOnClickListener {
            viewModel.signOut()
        }
    }

    override fun onResume() {
        super.onResume()
        intent?.data?.let { viewModel.redirectIntentUri(it) }
    }
}
