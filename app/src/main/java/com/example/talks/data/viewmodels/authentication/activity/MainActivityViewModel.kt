package com.example.talks.data.viewmodels.authentication.activity

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.constants.ServerConstants
import com.example.talks.constants.ServerConstants.FIREBASE_DB_NAME
import com.example.talks.data.model.TalksContact
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.others.encryption.Encryption
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel
@Inject constructor(
    val db: FirebaseFirestore,
    val auth: FirebaseAuth,
    val storageRef: FirebaseStorage
) : ViewModel() {

    // OTP AUTH -----------------------------------------------------------------
    val smsCode: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    val verificationID: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }

    private var storedVerificationId = ""

    val isUserLoggedIn: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun sendVerificationCode(
        phoneNumber: String?,
        activity: Activity,
        auth: FirebaseAuth
    ) {
        if (phoneNumber != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val options = activity.let {
                    PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(it)
                        .setCallbacks(callbacks)
                        .build()
                }
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }
    }

    private var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            signInWithPhoneAuthCredentials(p0)
            smsCode.value = p0.smsCode
        }

        override fun onVerificationFailed(p0: FirebaseException) {
//            Toast.makeText(context, p0.localizedMessage, Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(
            verificationId: String,
            forceResendingToken: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, forceResendingToken)
            storedVerificationId = verificationId
            verificationID.value = verificationId
        }
    }

    private fun signInWithPhoneAuthCredentials(p0: PhoneAuthCredential) {
        viewModelScope.launch(Dispatchers.IO) {
            auth.signInWithCredential(p0)
                .addOnCompleteListener {
                    isUserLoggedIn.value = it.isSuccessful
                }
        }
    }

    fun manualOTPAuth(otp: String?) {
        if (storedVerificationId != "") {
            val credential =
                PhoneAuthProvider.getCredential(storedVerificationId, otp.toString())
            signInWithPhoneAuthCredentials(credential)
        }
    }

    // Firestore AUTH --------------------------------------------------------------------

    val existingUserData: MutableLiveData<HashMap<String, String>> by lazy {
        MutableLiveData<HashMap<String, String>>()
    }
    val localUserData: MutableLiveData<HashMap<String, String?>> by lazy {
        MutableLiveData<HashMap<String, String?>>()
    }
    val profileImageUrl: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }

    @DelicateCoroutinesApi
    fun getUsersFromServer(
        databaseContactList: List<String>,
        contactNameList: HashMap<String, String>,
        databaseViewModel: TalksViewModel,
        encryptionKey: String
    ) {

        viewModelScope.launch(Dispatchers.IO) {

            db.collection(FIREBASE_DB_NAME).get().addOnSuccessListener {
                for (document in it.documents) {
                    val contactNumber =
                        document.get(ServerConstants.USER_PHONE_NUMBER).toString()
                    val contactUserName =
                        document.get(ServerConstants.USER_NAME).toString()
                    val contactImageUrl =
                        document.get(ServerConstants.USER_IMAGE_URL).toString()
                    val contactBio = document.get(ServerConstants.USER_BIO).toString()
                    val contactId =
                        document.get(ServerConstants.USER_UNIQUE_ID).toString()

                    val decryptedImage =
                        Encryption().decrypt(contactImageUrl, encryptionKey)
                    val user = TalksContact(
                        contactNumber,
                        contactNameList[contactNumber]
                    ).apply {
                        this.contactUserName = contactUserName
                        this.contactImageUrl = decryptedImage.toString()
                        this.uId = contactId
                        this.isTalksUser = true
                        this.contactBio = contactBio
                    }
                    databaseViewModel.updateUser(user)
                }
            }

//            Firebase.database.getReference(ServerConstants.FIREBASE_DB_NAME).get()
//                .addOnSuccessListener {
//
//                    for (data in it.children) {
//
//                        val contactNumber =
//                            data.child(ServerConstants.USER_PHONE_NUMBER).value.toString()
//                        val contactUserName =
//                            data.child(ServerConstants.USER_NAME).value.toString()
//                        val contactImageUrl =
//                            data.child(ServerConstants.USER_IMAGE_URL).value.toString()
//                        val contactBio = data.child(ServerConstants.USER_BIO).value.toString()
//                        val contactId =
//                            data.child(ServerConstants.USER_UNIQUE_ID).value.toString()
//
//                        val decryptedImage =
//                            Encryption().decrypt(contactImageUrl, encryptionKey)
//                        val user = TalksContact(
//                            contactNumber,
//                            contactNameList[contactNumber]
//                        ).apply {
//                            this.contactUserName = contactUserName
//                            this.contactImageUrl = decryptedImage.toString()
//                            this.uId = contactId
//                            this.isTalksUser = true
//                            this.contactBio = contactBio
//                        }
//                        databaseViewModel.updateUser(user)
//
//                    }
//                }
        }
    }

    fun getUserFromDatabaseIfExists(userUid: String?) {
        viewModelScope.launch(Dispatchers.IO) {

            db.collection(FIREBASE_DB_NAME).document(userUid!!).get().addOnSuccessListener {
                val userName = it.get(ServerConstants.USER_NAME).toString()
                val userImage = it.get(ServerConstants.USER_IMAGE_URL).toString()
                val userBio = it.get(ServerConstants.USER_BIO).toString()

                val user = hashMapOf(
                    ServerConstants.USER_NAME to userName,
                    ServerConstants.USER_IMAGE_URL to userImage,
                    ServerConstants.USER_BIO to userBio
                )
                existingUserData.value = user
            }
//            val dbRef = Firebase.database.getReference(ServerConstants.FIREBASE_DB_NAME)
//            if (userUid != null) {
//                dbRef.child(userUid).get().addOnSuccessListener {
//                    val userName = it.child(ServerConstants.USER_NAME).value.toString()
//                    val userImage = it.child(ServerConstants.USER_IMAGE_URL).value.toString()
//                    val userBio = it.child(ServerConstants.USER_BIO).value.toString()
//
//                    val user = hashMapOf(
//                        ServerConstants.USER_NAME to userName,
//                        ServerConstants.USER_IMAGE_URL to userImage,
//                        ServerConstants.USER_BIO to userBio
//                    )
//                    existingUserData.value = user
//                }
//            }
        }
    }

    fun addUserToFirebaseDatabase(
        user: HashMap<String, String?>,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            db.collection(FIREBASE_DB_NAME).document(user[ServerConstants.USER_UNIQUE_ID]!!)
                .set(user).addOnSuccessListener {
                    val localUser = hashMapOf(
                        ServerConstants.USER_PHONE_NUMBER to user[ServerConstants.USER_PHONE_NUMBER],
                        ServerConstants.USER_NAME to user[ServerConstants.USER_NAME],
                        ServerConstants.USER_IMAGE_URL to user[ServerConstants.USER_IMAGE_URL],
                        ServerConstants.USER_BIO to user[ServerConstants.USER_BIO],
                        ServerConstants.USER_UNIQUE_ID to user[ServerConstants.USER_UNIQUE_ID]
                    )
                    localUserData.value = localUser
                }

//            val dbRef = Firebase.database.getReference(ServerConstants.FIREBASE_DB_NAME)
//            if (user[ServerConstants.USER_UNIQUE_ID] != null) {
//                dbRef.child(user[ServerConstants.USER_UNIQUE_ID]!!).setValue(user)
//                    .addOnSuccessListener {
//                        val localUser = hashMapOf(
//                            ServerConstants.USER_PHONE_NUMBER to user[ServerConstants.USER_PHONE_NUMBER],
//                            ServerConstants.USER_NAME to user[ServerConstants.USER_NAME],
//                            ServerConstants.USER_IMAGE_URL to user[ServerConstants.USER_IMAGE_URL],
//                            ServerConstants.USER_BIO to user[ServerConstants.USER_BIO],
//                            ServerConstants.USER_UNIQUE_ID to user[ServerConstants.USER_UNIQUE_ID]
//                        )
//                        localUserData.value = localUser
//                    }.addOnFailureListener {
//                        Log.i("failed login====", it.toString())
//                    }
//            }
        }
    }


    fun uploadImageToStorage(image: Uri?, userId: String) {
        if (image != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val reference = storageRef.getReference(userId).child(ServerConstants.USER_IMAGE_STORAGE_PATH)
                val uploadTask = reference.putFile(image)
                uploadTask.addOnSuccessListener {
                    getDownloadUrl(uploadTask, reference)
                }
            }
        }
    }

    private fun getDownloadUrl(uploadTask: UploadTask, reference: StorageReference) {
        uploadTask.continueWithTask {
            if (!it.isSuccessful) {
                it.exception?.let {
                    throw it
                }
            }
            reference.downloadUrl
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                profileImageUrl.value = it.result.toString()
            } else {
                profileImageUrl.value = it.exception?.message.toString()
            }
        }
    }

}