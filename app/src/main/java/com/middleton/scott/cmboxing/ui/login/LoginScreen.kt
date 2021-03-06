package com.middleton.scott.cmboxing.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.other.Constants.GOOGLE_SIGN_IN
import com.middleton.scott.cmboxing.utils.hideKeyboard
import kotlinx.android.synthetic.main.fragment_login_screen.*
import org.koin.android.ext.android.inject


class LoginScreen : Fragment() {
    private val viewModel: LoginViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
        subscribeUI()
    }

    private fun setClickListeners() {
        login_btn.setOnClickListener {
            hideKeyboard()
            login_progress_bar.visibility = VISIBLE
            login_btn.text = ""
            viewModel.authSignInWithEmailPassword()
        }

        google_login_btn.setOnClickListener {
            hideKeyboard()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
            google_login_progress_bar.visibility = VISIBLE
            google_login_btn.text = ""
        }

        create_account_btn.setOnClickListener {
            hideKeyboard()
            val action = LoginScreenDirections.actionLoginScreenToCreateAccountScreen()
            findNavController().navigate(
                action
            )
        }

        email_et.doAfterTextChanged {
            viewModel.email = it.toString()
            viewModel.validate()
        }

        password_et.doAfterTextChanged {
            viewModel.password = it.toString()
            viewModel.validate()
        }
    }

    private fun subscribeUI() {
        viewModel.signInResponseLD.observe(viewLifecycleOwner, {
            if (it.success) {
                hideKeyboard()
                val action =
                    LoginScreenDirections.actionLoginScreenToMyWorkoutsScreen(true)
                findNavController().navigate(
                    action
                )
            } else {
                login_progress_bar.visibility = GONE
                login_btn.text = getString(R.string.log_in)
                Toast.makeText(
                    requireContext(), it.errorString,
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        viewModel.addUserResponseLD.observe(viewLifecycleOwner, {
            if (it.success) {
                hideKeyboard()
                val action =
                    LoginScreenDirections.actionLoginScreenToMyWorkoutsScreen(true)
                findNavController().navigate(
                    action
                )
            } else {
                Toast.makeText(
                    requireContext(), it.errorString,
                    Toast.LENGTH_LONG
                ).show()
            }
            google_login_progress_bar.visibility = GONE
            google_login_btn.text = getString(R.string.sign_in)
        })

        viewModel.emailValidLD.observe(viewLifecycleOwner, {
            if (it) {
                email_til.error = null
            } else {
                email_til.error = getString(R.string.email_validation_error)
                login_progress_bar.visibility = GONE
                login_btn.text = getString(R.string.log_in)
            }
        })

        viewModel.passwordValidLD.observe(viewLifecycleOwner, {
            if (it) {
                password_til.error = null
            } else {
                password_til.error = getString(R.string.password_validation_error)
                login_progress_bar.visibility = GONE
                login_btn.text = getString(R.string.log_in)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            viewModel.handleGoogleSignInResult(
                account?.givenName ?: "",
                account?.familyName ?: "",
                account?.email ?: ""
            )
            firebaseAuthWithGoogle(account?.idToken!!)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("TAG", "signInResult:failed code=" + e.statusCode)
            Toast.makeText(
                requireContext(), getString(R.string.google_sign_in_error),
                Toast.LENGTH_LONG
            ).show()
            google_login_progress_bar.visibility = GONE
            google_login_btn.text = getString(R.string.sign_in)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithCredential:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                }
            }
    }
}