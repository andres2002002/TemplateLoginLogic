package com.akaigms.logiclogin.ResoursesCode

data class AlertMessage(val title:String,val body:String)

data class Account(val user:String?,val email: String?, val login: Boolean)

data class Login_Launch(val Login: Boolean, val Launch: Boolean)

enum class Log_InOut{
    LogIn,LogOut,None
}
enum class LoginScreenState(){
    SingIn,SingUp,ForgottPwd
}
enum class LoginType{
    SingIn,SingUp,Login,Google
}