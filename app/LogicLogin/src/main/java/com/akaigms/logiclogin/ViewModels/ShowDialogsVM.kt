package com.akaigms.logiclogin.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ShowDialogsVM():ViewModel(){

    private val _showAlert = MutableLiveData<Boolean>()
    val showAlert: LiveData<Boolean> = _showAlert

    private val _alertMessage = MutableLiveData<String>()
    val alertMessage: LiveData<String> = _alertMessage

    fun ShowAlert(message:String){
        _showAlert.value=true
        _alertMessage.value=message
    }
    fun HideAlert(){_showAlert.value=false}

}