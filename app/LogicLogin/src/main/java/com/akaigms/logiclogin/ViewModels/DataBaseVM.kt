package com.akaigms.logiclogin.ViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.akaigms.logiclogin.TagsAndOthers.TAG_LOGIN
import com.akaigms.logiclogin.TagsAndOthers.TAG_USERNAME
import com.google.firebase.firestore.FirebaseFirestore

class DataBaseVM(private val userData: UserDataVM):ViewModel(){
    val db = FirebaseFirestore.getInstance()

    private val _email=userData.getEmail
    private val _user =userData.getUser
    val user: LiveData<String> = _user
    fun setUser(user:String){
        userData.setUser(user)
    }

    //region Save data base
    fun saveDB() {
        db.collection("Users").document(_email.value!!).set(
            hashMapOf(
                TAG_USERNAME to _user.value,
                "Income_2" to "abcd"
            )
        ).addOnCompleteListener {
            if (it.isSuccessful){
                Log.i(TAG_LOGIN,"Save Data Base success")
            }
            else{
                Log.w(TAG_LOGIN,"Error Save data",it.exception)
            }
        }
        userData.loginType()
    }

    fun loadUserDB(saveUser:Boolean=false) {
        //el if es para que no haya error al enviar datos
        if (_email.value!="") {
            db.collection("Users").document(_email.value!!).get().addOnSuccessListener {
                if(it.get(TAG_USERNAME)!=null) {
                    setUser(it.get(TAG_USERNAME).toString())
                    Log.i(TAG_LOGIN, "user database: ${it.get(TAG_USERNAME).toString()}")
                }else{
                    Log.d(TAG_LOGIN,"UserSaved: ${_user.value}")
                    saveDB()
                }
            }.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i(TAG_LOGIN, "Load Data Base Success")
                    if (saveUser){
                        Log.d(TAG_LOGIN,"user and email: ${_user.value} , ${_email.value}")
                        userData.saveDataAccount(_user.value,_email.value)
                    }
                } else {
                    Log.w(TAG_LOGIN, "Load Data Base error", it.exception)
                }
            }
            userData.loginType()
        }
    }

    fun deleteDB() {
        db.collection("Users").document(_email.value!!).delete()
    }
//endregion
}