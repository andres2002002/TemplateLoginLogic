package com.akaigms.logiclogin.ViewModels

import android.content.Intent
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.*
import com.akaigms.logiclogin.ResoursesCode.AlertMessage
import com.akaigms.logiclogin.ResoursesCode.Log_InOut
import com.akaigms.logiclogin.TagsAndOthers.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginVM(
    private val activity: ComponentActivity,
    private val userDataVM: UserDataVM,
    private val dataBaseVM: DataBaseVM
) : ViewModel() {

    //region private Val
    //email ocupado en todos lados
    private val _email = MutableLiveData<String>()
    val getEmail: LiveData<String> = _email

    //password para email-contraseña
    private val _password = MutableLiveData<String>()
    val getPassword: LiveData<String> = _password

    //password para email-contraseña
    private val _confirmPassword = MutableLiveData<String>()
    val getConfirmPassword: LiveData<String> = _confirmPassword

    //habilitar botones login
    private val _singInEnable = MutableLiveData<Boolean>()
    val getSingInEnable: LiveData<Boolean> = _singInEnable

    //habilitar botones registrar
    private val _singUpEnable = MutableLiveData<Boolean>()
    val getSingUpEnable: LiveData<Boolean> = _singUpEnable

    //habilitar boton reset
    private val _resetEnable = MutableLiveData<Boolean>()
    val getResetEnable: LiveData<Boolean> = _resetEnable

    //habilitar ventana reset
    private val _showReset = MutableLiveData<Boolean>()
    val getShowReset: LiveData<Boolean> = _showReset

    //ver alerta
    private val _showAlert = MutableLiveData<Boolean>()
    val getShowAlert: LiveData<Boolean> = _showAlert

    //ver para navegar entre Login y LogOut
    private val _login = MutableLiveData<Log_InOut>(Log_InOut.None)
    val getLogin: LiveData<Log_InOut> = _login

    //mensaje de alerta
    private val _alertMessage = MutableLiveData<AlertMessage>()
    val getAlertMessage: LiveData<AlertMessage> = _alertMessage

    //metodo de que se va a reintentar
    private val _retry = MutableLiveData<() -> Unit>()
    val getRetry: LiveData<() -> Unit> = _retry

    //endregion

    //maneja el email-contraseña y rectifica si se habilitan los botones login
    fun onSingInChange(email: String, password: String) {
        _email.value = email
        _password.value = password
        _singInEnable.value = isValidEmail(email) && isValidPassword(password)
    }

    //maneja el email-contraseña y rectifica si se habilitan los botones login
    fun onSingUpChange(email: String, password: String,CPassword:String) {
        _email.value = email
        _password.value = password
        _confirmPassword.value=CPassword
        _singUpEnable.value = isValidEmail(email) && isValidPassword(password) && _password.value==_confirmPassword.value
        _singInEnable.value=_singUpEnable.value
    }

    //rectifica que email es valido
    fun validateEmail(email: String) {
        _email.value = email
        _resetEnable.value = isValidEmail(email)
    }

    //envia una alerta
    fun showAlertMessage(showAlert: Boolean, alertMessage: AlertMessage, retry: () -> Unit) {
        _showAlert.value = showAlert
        _retry.value = retry
        _alertMessage.value = alertMessage
    }

    //lo que hace el patterns es ver que el email sea valido automaticamente
    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isValidPassword(password: String): Boolean = password.length > 5

    //region login
    fun SingUp(email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG_LOGIN,"singUp yes")
                    userDataVM.setEmail(email)
                    userDataVM.saveDataAccount(userDataVM.getUser.value,email)
                    _login.value = Log_InOut.LogIn
                } else {
                    Log.w(TAG_LOGIN, "Oh nyo! " + it.exception?.message, it.exception)
                    showAlertMessage(
                        !it.isSuccessful,
                        AlertMessage(
                            "Fallo al registrar cuenta",
                            "Es posible que ya haya una cuenta registrada con este correo\n\n" +
                                    "Informacion del error:\n" +
                                    it.exception?.message
                        )
                    ) {
                        CancelLogin()
                        SingUp(email, password)
                    }
                }
            }
    }

    fun SingIn(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG_LOGIN,"singUp yes")
                    userDataVM.setEmail(email)
                    userDataVM.saveDataAccount(userDataVM.getUser.value,email)
                    _login.value = Log_InOut.LogIn
                } else {
                    Log.w(TAG_LOGIN, "Oh nyo! " + it.exception?.message, it.exception)
                    showAlertMessage(
                        !it.isSuccessful,
                        AlertMessage(
                            "Fallo al iniciar sesion",
                            "Verifique que el correo y la contraseña sean correctos\n\n" +
                                    "Informacion del error:\n" +
                                    it.exception?.message
                        )
                    ) {
                        CancelLogin()
                        SingIn(email, password)
                    }
                }
            }
    }
    fun SingOut() {
        FirebaseAuth.getInstance().signOut()
        _email.value=""
        _password.value=""
        userDataVM.setUser("")
        userDataVM.setEmail("")
        _login.value = Log_InOut.LogOut
        userDataVM.saveDataAccount(null,null)
        Log.i(TAG_LOGIN,"Sing Out")
    }
    //endregion

    //region Login Google

    fun SingInGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_ID)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        googleSignInClient.signOut()
        //botton
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    var resultLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            // There are no request codes
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken!!)
                userDataVM.saveDataAccount(account.displayName,account.email)
                userDataVM.setUser(account.displayName?:"")
                userDataVM.setEmail(account.email?:"")
                _login.value = Log_InOut.LogIn
            } catch (e: ApiException) {
                Log.w(TAG_LOGIN, "Google sign in failed!", e)
                showAlertMessage(
                    true,
                    AlertMessage(
                        title = "Fallo en Iniciar sesion con Google",
                        body = "Se ha producido un error, por favor intentelo mas tarde\n\n" +
                                "Informacion del error:\n" +
                                e.message
                    )
                ) {
                    CancelLogin()
                    SingInGoogle()
                }
            }
        }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    Log.i(TAG_LOGIN, "signInWithCredential:success")
                } else {
                    Log.w(TAG_LOGIN, "signInWithCredential:failure", task.exception)
                    showAlertMessage(
                        true,
                        AlertMessage(
                            title = "Fallo en Iniciar sesion con Google",
                            body = "Se ha producido un error, por favor intentelo mas tarde\n\n" +
                                    "Informacion del error:\n" +
                                    task.exception?.message
                        ),
                        { SingInGoogle() }
                    )
                }
            }
    }
    //endregion

    //region viewsManager
    fun CancelLogin() {
        _showAlert.value = false
    }

    fun ViewReset() {
        _showReset.value = true
    }

    fun CancelReset() {
        _showReset.value = false
    }
    //endregion

    fun resetLog(){
        _login.value=Log_InOut.None
    }

    fun resetPassword(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                CancelReset()
                val toast =
                    Toast.makeText(
                        activity.applicationContext,
                        "Correo enviado a " + email,
                        Toast.LENGTH_SHORT
                    )
                toast.show()
            } else {
                Log.w(TAG_LOGIN, "Oh nyo! " + it.exception?.message, it.exception)
                showAlertMessage(
                    !it.isSuccessful,
                    AlertMessage(
                        "Fallo al reestablecer la contraseña",
                        "Verifique que la direccion de correo electronico sea valido\n\n" +
                                "Informacion del error:\n" +
                                it.exception?.message
                    ),
                    { resetPassword(email) }
                )
            }
        }
    }
}
