package com.irada.blockerheroar.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.irada.blockerheroar.R
import com.irada.blockerheroar.ui.MainActivity
import com.irada.blockerheroar.ui.auth.LoginViewModel
import com.irada.blockerheroar.ui.theme.IradaTheme

class LoginActivity : ComponentActivity() {
    
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        
        setContent {
            IradaTheme {
                LoginScreen(
                    onLoginSuccess = { navigateToMain() },
                    onGoogleSignIn = { signInWithGoogle() }
                )
            }
        }
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(Exception::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    // Handle Google Sign-In with ViewModel
                    val viewModel = LoginViewModel()
                    viewModel.signInWithGoogle(idToken) { success ->
                        if (success) {
                            navigateToMain()
                        } else {
                            Toast.makeText(this, "فشل في تسجيل الدخول بجوجل", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "فشل في تسجيل الدخول بجوجل", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    companion object {
        private const val RC_SIGN_IN = 9001
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoogleSignIn: () -> Unit,
    viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel<LoginViewModel>()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showRegister by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo/Title
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
        
        // Login Button
        Button(
            onClick = {
                isLoading = true
                viewModel.signInWithEmail(email, password) { success ->
                    isLoading = false
                    if (success) {
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "أحد البيانات المدخلة خطأ", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text(stringResource(R.string.login))
            }
        }
        
        // Forgot Password Button
        TextButton(
            onClick = {
                if (email.isBlank()) {
                    Toast.makeText(context, "أدخل الإيميل أولاً", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.sendPasswordResetEmail(email) { success ->
                        if (success) {
                            Toast.makeText(context, "تم إرسال إيميل إعادة تعيين كلمة المرور", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "فشل في إرسال الإيميل", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "نسيت كلمة المرور؟",
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Google Sign-In Button
        OutlinedButton(
            onClick = onGoogleSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(stringResource(R.string.google_sign_in))
        }
        
        // Register Link
        TextButton(
            onClick = { showRegister = true }
        ) {
            Text(stringResource(R.string.register))
        }
        
        // Forgot Password Link
        TextButton(
            onClick = { /* Handle forgot password */ }
        ) {
            Text(stringResource(R.string.forgot_password))
        }
    }
    
    // Register Dialog
    if (showRegister) {
        RegisterDialog(
            onDismiss = { showRegister = false },
            onRegisterSuccess = { 
                Toast.makeText(context, "تم إنشاء الحساب بنجاح! تحقق من صندوق الوارد لتفعيل الحساب", Toast.LENGTH_LONG).show()
                showRegister = false
                onLoginSuccess()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDialog(
    onDismiss: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel<LoginViewModel>()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.register)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    viewModel.registerWithEmail(name, email, password) { success ->
                        isLoading = false
                        if (success) {
                            onRegisterSuccess()
                        } else {
                            Toast.makeText(context, "فشل في إنشاء الحساب", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isLoading && name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text(stringResource(R.string.register))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
