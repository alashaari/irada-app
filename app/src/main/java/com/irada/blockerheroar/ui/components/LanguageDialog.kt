package com.irada.blockerheroar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.irada.blockerheroar.R
import com.irada.blockerheroar.utils.AppPreferences
import com.irada.blockerheroar.IradaApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedLanguage by remember { 
        mutableStateOf(
            try {
                AppPreferences.getLanguage()
            } catch (e: Exception) {
                "ar" // Default language
            }
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(stringResource(R.string.language))
        },
        text = {
            Column {
                RadioButton(
                    selected = selectedLanguage == "ar",
                    onClick = { selectedLanguage = "ar" }
                )
                Text(
                    text = stringResource(R.string.arabic),
                    modifier = Modifier.padding(start = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                RadioButton(
                    selected = selectedLanguage == "en",
                    onClick = { selectedLanguage = "en" }
                )
                Text(
                    text = stringResource(R.string.english),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    AppPreferences.setLanguage(selectedLanguage)
                    // Apply language change immediately
                    (context.applicationContext as IradaApplication).setAppLanguage(selectedLanguage)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}