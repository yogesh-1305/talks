package com.example.talks.data.viewmodels.authentication.activity

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talks.constants.ServerConstants
import com.example.talks.constants.ServerConstants.FETCH_DATA_FINISHED
import com.example.talks.constants.ServerConstants.FETCH_DATA_IN_PROGRESS
import com.example.talks.constants.ServerConstants.FIREBASE_DB_NAME
import com.example.talks.constants.ServerConstants.USER_UNIQUE_ID
import com.example.talks.data.model.Message
import com.example.talks.data.model.TalksContact
import com.example.talks.data.viewmodels.db.TalksViewModel
import com.example.talks.others.encryption.Encryption
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel
@Inject constructor(
    val db: FirebaseFirestore,
    val auth: FirebaseAuth,
    private val storageRef: FirebaseStorage,
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
        auth: FirebaseAuth,
    ) {
        if (phoneNumber != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }
    }

    private var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            smsCode.value = p0.smsCode
            viewModelScope.launch {
                delay(1000)
                signInWithPhoneAuthCredentials(p0)
            }
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Log.e("AUTHENTICATION FAILED ===", p0.localizedMessage)
        }

        override fun onCodeSent(
            verificationId: String,
            forceResendingToken: PhoneAuthProvider.ForceResendingToken,
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

    @RequiresApi(Build.VERSION_CODES.O)
    @DelicateCoroutinesApi
    fun getUsersFromServer(
        databaseContactList: List<String>,
        contactNameList: HashMap<String, String>,
        databaseViewModel: TalksViewModel,
        encryptionKey: String,
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
                        document.get(USER_UNIQUE_ID).toString()

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
        }
    }

    var userData: HashMap<String, String?> = HashMap()
    fun addUserToFirebaseDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            userData.let { data ->
                db.collection(FIREBASE_DB_NAME).document(data.get(USER_UNIQUE_ID).toString())
                    .set(data).addOnSuccessListener {
                        val localUser = hashMapOf(
                            ServerConstants.USER_PHONE_NUMBER to data[ServerConstants.USER_PHONE_NUMBER],
                            ServerConstants.USER_NAME to data[ServerConstants.USER_NAME],
                            ServerConstants.USER_IMAGE_URL to data[ServerConstants.USER_IMAGE_URL],
                            ServerConstants.USER_BIO to data[ServerConstants.USER_BIO],
                            USER_UNIQUE_ID to data[USER_UNIQUE_ID]
                        )
                        localUserData.value = localUser
                    }
            }
        }
    }


    var imageUri: Uri? = null
    fun uploadImageToStorage() {
        if (imageUri != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val reference =
                    storageRef.getReference(auth.currentUser?.uid.toString())
                        .child(ServerConstants.USER_IMAGE_STORAGE_PATH)
                val uploadTask = reference.putFile(imageUri!!)
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

    val dataFetched: MutableLiveData<Int> = MutableLiveData()

    fun readMessagesFromServer(talksVM: TalksViewModel) {
        viewModelScope.launch(Dispatchers.IO) {
//            dataFetched.postValue(FETCH_DATA_STARTED)
            auth.currentUser?.let { it ->
                db.collection(FIREBASE_DB_NAME).document(it.uid).collection("user_chats")
                    .get().addOnSuccessListener { snapshot ->
                        if (!snapshot.isEmpty) {
                            dataFetched.postValue(FETCH_DATA_IN_PROGRESS)
                            for (document in snapshot.documents) {
                                val message = document.toObject(Message::class.java)
                                if (message != null) {
                                    talksVM.addMessage(message)
                                }
                                dataFetched.postValue(FETCH_DATA_FINISHED)
                            }
                        }

                    }
//                    .addSnapshotListener { snapshot, error ->
//                        if (snapshot != null) {
//                            size = snapshot.size()
//                            for (document in snapshot.documents) {
//                                val message = document.toObject(Message::class.java)
//                                if (message != null) {
//                                    talksVM.addMessage(message)
//                                }
//                            }
//
//                        }
//                    }
            }
        }

    }

}