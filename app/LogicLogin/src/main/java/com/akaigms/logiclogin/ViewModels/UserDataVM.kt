package com.akaigms.logiclogin.ViewModels

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.akaigms.logiclogin.ResoursesCode.Account
import com.akaigms.logiclogin.ResoursesCode.LoginType
import com.akaigms.logiclogin.TagsAndOthers.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class UserDataVM(private val activity: ComponentActivity){

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Account")

    private val _user = MutableLiveData<String>()
    val getUser: LiveData<String> = _user

    private val _email = MutableLiveData<String>()
    val getEmail: LiveData<String> = _email

    private val _loginType = MutableLiveData<LoginType>()
    val getLoginType: LiveData<LoginType> = _loginType

    fun setUser(user:String){
        _user.value=user
    }
    fun setEmail(email:String){
        _email.value=email
    }

    fun singUpType(){
        _loginType.value=LoginType.SingUp
    }
    fun singInType(){
        _loginType.value=LoginType.SingIn
    }
    fun loginType(){
        _loginType.value=LoginType.Login
    }

    fun GoogleType(){
        _loginType.value=LoginType.Google
    }

    fun saveDataAccount(User:String?,email: String?) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            saveAccountCorrutine(User?:"",email ?: "")
        }
    }

    private suspend fun saveAccountCorrutine(User:String,email: String) {
        activity.dataStore.edit { account ->
            account[stringPreferencesKey(TAG_USERNAME)] = User
            account[stringPreferencesKey(TAG_USEREMAIL)] = email
            account[booleanPreferencesKey(TAG_EXISTEMAIL)] = email != ""
            Log.i(TAG_LOGIN, "Cuenta salvada")
        }
    }

    fun RecoverAccount() = activity.dataStore.data.map { preferences ->
        Account(
            user = preferences[stringPreferencesKey(TAG_USERNAME)] ?: "",
            email = preferences[stringPreferencesKey(TAG_USEREMAIL)] ?: "",
            login = preferences[booleanPreferencesKey(TAG_EXISTEMAIL)] ?: false
        )
    }

}