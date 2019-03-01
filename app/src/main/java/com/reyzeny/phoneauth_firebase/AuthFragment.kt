package com.reyzeny.phoneauth_firebase

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.auth.*
import java.util.*
import java.util.concurrent.TimeUnit

class AuthFragment: Fragment() {
    private var home_view: View? = null
    private var input: String? = null
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var input_state = Constant.ENTER_PHONE_NUMBER
    var runnableCode: Runnable? = null
    private lateinit var handler: Handler
    private var handler_interrupted: Boolean = false

    private fun startResendCountDown() {
        var RESEND_COUNTER: Long = Constant.PHONE_NUMBER_VERIFICATION_TIMEOUT
        auth_tvResendCode.setOnClickListener (null)
        handler_interrupted=false
        handler = Handler()
        runnableCode = object : Runnable {
            override fun run() {
                if (RESEND_COUNTER < 1){
                    auth_tvResendCode.text = getString(R.string.resend_code)
                    auth_tvResendCode.setOnClickListener { PhoneAuthProvider.getInstance().verifyPhoneNumber(input!!, Constant.PHONE_NUMBER_VERIFICATION_TIMEOUT, TimeUnit.SECONDS, activity!!, callbacks, resendToken) }
                    handler.removeCallbacks(runnableCode)
                    return
                }
                RESEND_COUNTER--
                auth_tvResendCode.text = "You can resend the code in ${RESEND_COUNTER} seconds"
                if (handler_interrupted) { handler.removeCallbacks(runnableCode); auth_tvResendCode.text=""; return }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnableCode)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (home_view==null)
            home_view = inflater.inflate(R.layout.auth, container, false)
        return home_view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initComponents()
    }

    private fun initComponents() {
        auth_tvWrongNumber.visibility=View.GONE
        auth_edtCode.visibility=View.GONE
        auth_progressbar.visibility=View.GONE
        auth_btnVerify.setOnClickListener {
            when (input_state){
                Constant.ENTER_PHONE_NUMBER ->{ println("verifying phone number"); verifyPhoneNumber() }
                Constant.ENTER_CODE -> { verifyCode() }
            }
        }
        auth_tvWrongNumber.setOnClickListener { clearNumber() }
        setHeader()
    }

    private fun verifyPhoneNumber() {
        input = auth_edtPhoneNumber.text.toString().trim().replace(" ", "")
        if (input!!.isEmpty() || !input!!.contains("+")) {
            auth_edtPhoneNumber.error = getString(R.string.invalid_phone_number)
            auth_edtPhoneNumber.requestFocus()
            return
        }
        println("phone auth provider instance")
        auth_progressbar.visibility=View.VISIBLE
        PhoneAuthProvider.getInstance().verifyPhoneNumber(input!!, Constant.PHONE_NUMBER_VERIFICATION_TIMEOUT, TimeUnit.SECONDS, activity!!, callbacks)
    }
    private fun verifyCode() {
        val input = auth_edtCode.text.toString()
        if (input.isEmpty()) {
            auth_edtCode.error = getString(R.string.valid_code_required)
            auth_edtCode.requestFocus()
            return
        }
        val phone_auth_credential = PhoneAuthProvider.getCredential(storedVerificationId!!, input)
        signInWithPhoneAuthCredential(phone_auth_credential)
    }
    var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            println("onVerification completed")
            Toast.makeText(context!!, "Verification completed", Toast.LENGTH_LONG).show()
            signInWithPhoneAuthCredential(credential)
        }
        override fun onVerificationFailed(e: FirebaseException) {
            println("verifying failed cause " + e.message)
            Toast.makeText(context!!, e.message, Toast.LENGTH_LONG).show()
            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
            }
            input_state = Constant.ENTER_PHONE_NUMBER
            auth_edtPhoneNumber.isEnabled = true
            auth_edtCode.visibility = View.GONE
            auth_progressbar.visibility=View.GONE
        }
        override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken) {
            Toast.makeText(context!!, "code has been sent", Toast.LENGTH_LONG).show()
            auth_tvWrongNumber.visibility=View.VISIBLE
            storedVerificationId = verificationId
            resendToken = token
            input_state = Constant.ENTER_CODE
            auth_edtPhoneNumber.isEnabled = false
            auth_edtCode.visibility = View.VISIBLE
            startResendCountDown()
            auth_progressbar.visibility=View.GONE
        }
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth_progressbar.visibility=View.VISIBLE
        val auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity!!) { task ->
                    if (task.isSuccessful) {
                        print("success auth credential")
                        handler_interrupted=true
                        val user = task.result?.user
                        signUpIfNotExist(user)
                    } else {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            Snackbar.make(auth_edtCode, getString(R.string.invalid_code), Snackbar.LENGTH_LONG).show()
                            auth_progressbar.visibility=View.GONE
                        }
                    }

                }
    }

    private fun signUpIfNotExist(user: FirebaseUser?) {
        auth_progressbar.visibility=View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        db.collection(Constant.FIREBASE_USER_PROFILE_COLLECTION).document(user?.phoneNumber!!).get()
            .addOnSuccessListener { document ->
                if (document == null) {
                    create_user(db, user)
                } else {
                    ExecuteAuthSuccess(user)
                }
            }
            .addOnFailureListener { exception ->
                //Log.d(TAG, "get failed with ", exception)
                Toast.makeText(context!!, "sign up failure. ${exception.message}", Toast.LENGTH_LONG).show()
                println(exception.message)
                auth_progressbar.visibility=View.GONE
            }
    }

    fun create_user(db: FirebaseFirestore, user: FirebaseUser?) {
        auth_progressbar.visibility=View.VISIBLE
        val user_profile_data = HashMap<String, Any>()
        user_profile_data[Constant.FIRST_NAME] = ""
        user_profile_data[Constant.LAST_NAME] = ""
        db.collection(Constant.FIREBASE_USER_PROFILE_COLLECTION).document(user?.phoneNumber!!)
            .set(user_profile_data)
            .addOnSuccessListener { _ ->
                Toast.makeText(context!!, "success creating user", Toast.LENGTH_LONG).show()
                //Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.id)
                ExecuteAuthSuccess(user)
            }
            .addOnFailureListener { e ->
                //Log.w(TAG, "Error adding document", e)
                Toast.makeText(context!!, "could not creat user. ${e.message}", Toast.LENGTH_LONG).show()
                println(e.message)
                auth_progressbar.visibility=View.GONE
            }

    }

    private fun ExecuteAuthSuccess(user: FirebaseUser) {
        LocalData.set_user_id(context!!, user.phoneNumber!!)
        LocalData.set_user_authenticated(context!!, true)
        showPendingFragment()
        auth_progressbar.visibility=View.GONE
    }

    fun showPendingFragment() {
        val container = arguments?.getInt(Constant.CONTAINER_ID)
        val fragment_tag = arguments?.getString(Constant.TAG)
        when (fragment_tag) {
            Constant.PROFILE_FRAGMENT_TAG ->{
                fragmentManager!!.beginTransaction().replace(container!!, AccountFragment()).commit()
            }
        }
    }
    fun setHeader() {
        val fragment_tag = arguments?.getString(Constant.TAG)
        when (fragment_tag) {
            Constant.PROFILE_FRAGMENT_TAG ->{
                auth_tvHeader.text = getString(R.string.account)
                auth_header_subtitle.text = "${getString(R.string.need_auth_to_access)} ${getString(R.string.account)}"
            }
        }
    }
    fun clearNumber() {
        input_state=Constant.ENTER_PHONE_NUMBER
        auth_edtPhoneNumber.isEnabled=true
        auth_edtPhoneNumber.requestFocus()
        auth_tvWrongNumber.visibility=View.GONE
        auth_edtCode.setText("")
        auth_edtCode.visibility=View.GONE
        auth_tvResendCode.text=""
        auth_tvResendCode.setOnClickListener (null)
        handler_interrupted=true
    }
}