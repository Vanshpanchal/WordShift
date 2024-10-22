package com.example.wordshift

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordshift.ui.theme.WordShiftTheme
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class MainActivity : ComponentActivity() {
    private var translator: Translator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordShiftTheme {
                App()
            }
        }
    }

    private fun setupTranslator(
        targetSelectedLanguage: String,
        sourceSelectedLanguage: String, // Pass the source language as a parameter
        onSetupComplete: () -> Unit
    ) {
        translator?.close() // Close existing translator if any

        val targetLanguageCode = when (targetSelectedLanguage) {
            "Hindi" -> TranslateLanguage.HINDI
            "Spanish" -> TranslateLanguage.SPANISH
            "French" -> TranslateLanguage.FRENCH
            "German" -> TranslateLanguage.GERMAN
            "Chinese" -> TranslateLanguage.CHINESE
            "English" -> TranslateLanguage.ENGLISH
            "Gujarati" -> TranslateLanguage.GUJARATI
            else -> TranslateLanguage.HINDI
        }

        val sourceLanguageCode = when (sourceSelectedLanguage) {
            "Hindi" -> TranslateLanguage.HINDI
            "Spanish" -> TranslateLanguage.SPANISH
            "French" -> TranslateLanguage.FRENCH
            "German" -> TranslateLanguage.GERMAN
            "Chinese" -> TranslateLanguage.CHINESE
            "Gujarati" -> TranslateLanguage.GUJARATI
            "English" -> TranslateLanguage.ENGLISH
            else -> TranslateLanguage.ENGLISH
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguageCode)
            .setTargetLanguage(targetLanguageCode).build()

        translator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder().requireWifi().build()

        translator?.downloadModelIfNeeded(conditions)?.addOnSuccessListener {
            onSetupComplete()
        }?.addOnFailureListener {
            onSetupComplete()
        }
    }

    private fun translateText(
        input: String, onTranslated: (String) -> Unit
    ) {
        if (input.isEmpty()) {
            onTranslated("")
            return
        }

        translator?.translate(input)?.addOnSuccessListener { translatedText ->
            onTranslated(translatedText)
        }?.addOnFailureListener { exception ->
            onTranslated("Translation failed: ${exception.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        translator?.close()
    }


    @Preview
    @Composable
    fun App() {
        var inputText by remember { mutableStateOf("") }
        var translatedText by remember { mutableStateOf("") }
        var targetSelectedLanguage by remember { mutableStateOf("Hindi") }
        var sourceSelectedLanguage by remember { mutableStateOf("English") }
        var isLoading by remember { mutableStateOf(false) }
        var expandedSource by remember { mutableStateOf(false) }
        var expandedTarget by remember { mutableStateOf(false) }

        val languages = listOf("Hindi", "Spanish", "French", "German", "Chinese", "Gujarati","English")

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "WordShift",
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                )
                Text(
                    text = "Your Language Companion",
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                    ),
                )
                Divider()

                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Input Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        elevation = CardDefaults.cardElevation(4.dp)

                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(
                                "Enter text in $sourceSelectedLanguage",
                                fontSize = 20.sp,
                                style = TextStyle(
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                color = MaterialTheme.colorScheme.onSecondary,
                            )

                            TextField(
                                value = inputText,
                                shape = RoundedCornerShape(15.dp),
                                textStyle = TextStyle(fontSize = 18.sp),
                                placeholder = { Text(text = "Enter here...") },
                                onValueChange = { inputText = it },
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                                    .align(alignment = Alignment.Center)
                                    .fillMaxHeight(0.6f),
                                maxLines = 10,
                                singleLine = false
                            )

                            Button(
                                onClick = {
                                    isLoading = true
                                    setupTranslator(
                                        targetSelectedLanguage = targetSelectedLanguage,
                                        sourceSelectedLanguage = sourceSelectedLanguage
                                    ) {
                                        translateText(inputText) { translated ->
                                            translatedText = translated
                                            isLoading = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .align(alignment = Alignment.BottomEnd)
                                    .padding(15.dp, 10.dp)
                            ) {
                                Text(
                                    text = "Translate",
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box {
                            Button(
                                onClick = { expandedSource = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = sourceSelectedLanguage,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            DropdownMenu(
                                expanded = expandedSource,
                                onDismissRequest = { expandedSource = false }
                            ) {
                                languages.forEach { language ->
                                    DropdownMenuItem(onClick = {
                                        sourceSelectedLanguage = language
                                        expandedSource = false
                                        setupTranslator(
                                            targetSelectedLanguage = targetSelectedLanguage,
                                            sourceSelectedLanguage = language
                                        ) {}
                                    }, text = { Text(language) })
                                }
                            }
                        }

                        Box {
                            Button(
                                onClick = { expandedTarget = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = targetSelectedLanguage,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            DropdownMenu(
                                expanded = expandedTarget,
                                onDismissRequest = { expandedTarget = false }
                            ) {
                                languages.forEach { language ->
                                    DropdownMenuItem(onClick = {
                                        targetSelectedLanguage = language
                                        expandedTarget = false
                                    }, text = { Text(language) })
                                }
                            }
                        }
                    }


                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(
                                "Translated into $targetSelectedLanguage",
                                fontSize = 20.sp,
                                style = TextStyle(
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            TextField(
                                value = translatedText,
                                textStyle = TextStyle(fontSize = 18.sp),
                                shape = RoundedCornerShape(15.dp),
                                placeholder = { },
                                onValueChange = { },
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    disabledContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                                    .align(alignment = Alignment.Center)
                                    .fillMaxHeight(0.6f),
                                maxLines = 10,
                                singleLine = false,
                                enabled = false
                            )
                            Row(
                                modifier = Modifier.align(Alignment.BottomEnd),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                                }
                                Button(
                                    onClick = {
                                        if (translatedText.isNotEmpty()){
                                        val clipboard =
                                            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText(
                                            "translated_text",
                                            translatedText
                                        )
                                        clipboard.setPrimaryClip(clip)
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = ButtonDefaults.buttonElevation(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .padding(15.dp, 10.dp)
                                ) {
                                    Text(text = "Copy", color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }


                }
            }
        }
    }

}