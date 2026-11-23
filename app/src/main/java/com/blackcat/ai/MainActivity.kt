package com.blackcat.ai

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Night = Color(0xFF080610)
private val Panel = Color(0xFF171225)
private val Violet = Color(0xFF9B5DE5)
private val Cyan = Color(0xFF35D6E8)
private val SoftText = Color(0xFFBDB6CC)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    internal val recognizedTextState = mutableStateOf("")
    internal val listeningState = mutableStateOf(false)
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var currentLanguage = "bn"

    private val microphonePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) beginListening()
        else listeningState.value = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        setContent {
            MaterialTheme {
                BlackCatDashboard(activity = this)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) applyTtsLanguage(currentLanguage)
    }

    internal fun speak(text: String, language: String) {
        currentLanguage = language
        applyTtsLanguage(language)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "black-cat-response")
    }

    private fun applyTtsLanguage(language: String) {
        val locale = if (language == "bn") Locale("bn", "BD") else Locale.US
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA ||
            result == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            tts?.setLanguage(Locale.US)
        }
    }

    internal fun startVoiceInput(language: String) {
        currentLanguage = language
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        beginListening()
    }

    private fun beginListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizedTextState.value = "Speech recognition is unavailable on this device."
            return
        }
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).also { recognizer ->
                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) { listeningState.value = true }
                    override fun onBeginningOfSpeech() { listeningState.value = true }
                    override fun onRmsChanged(rmsdB: Float) = Unit
                    override fun onBufferReceived(buffer: ByteArray?) = Unit
                    override fun onEndOfSpeech() { listeningState.value = false }
                    override fun onError(error: Int) {
                        listeningState.value = false
                        recognizedTextState.value = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "কথা বোঝা যায়নি। আবার চেষ্টা করুন।"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "কোনো কথা শোনা যায়নি।"
                            else -> "ভয়েস ইনপুটে সমস্যা হয়েছে। আবার চেষ্টা করুন।"
                        }
                    }
                    override fun onResults(results: Bundle?) {
                        listeningState.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        recognizedTextState.value = matches?.firstOrNull().orEmpty()
                    }
                    override fun onPartialResults(partialResults: Bundle?) = Unit
                    override fun onEvent(eventType: Int, params: Bundle?) = Unit
                })
            }
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (currentLanguage == "bn") "bn-BD" else "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_PROMPT, if (currentLanguage == "bn") "BLACK CAT-কে বলুন" else "Speak to BLACK CAT")
        }
        listeningState.value = true
        speechRecognizer?.startListening(intent)
    }

    internal fun runCommand(rawCommand: String, language: String, address: String): String {
        val command = rawCommand.trim().lowercase(Locale.ROOT)
        val greeting = if (language == "bn") "হ্যাঁ $address, " else "Yes $address, "
        return when {
            command.contains("youtube") || command.contains("ইউটিউব") ->
                openApp("com.google.android.youtube", "YouTube", language)
            command.contains("facebook") || command.contains("ফেসবুক") ->
                openApp("com.facebook.katana", "Facebook", language)
            command.contains("whatsapp") || command.contains("হোয়াটসঅ্যাপ") || command.contains("হোয়াটসঅ্যাপ") ->
                openApp("com.whatsapp", "WhatsApp", language)
            command.contains("dial") || command.contains("call") ||
                command.contains("কল") || command.contains("ডায়াল") || command.contains("ডায়াল") -> {
                startActivity(Intent(Intent.ACTION_DIAL))
                greeting + if (language == "bn") "ফোন ডায়ালার খুলেছি। কল করার আগে নম্বর যাচাই করুন।"
                else "the phone dialer is open. Check the number before calling."
            }
            command.contains("wi-fi") || command.contains("wifi") ||
                command.contains("ওয়াইফাই") || command.contains("ওয়াইফাই") -> {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                greeting + if (language == "bn") "Wi-Fi সেটিংস খুলেছি।" else "Wi-Fi settings opened."
            }
            command.contains("bluetooth") || command.contains("ব্লুটুথ") -> {
                startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                greeting + if (language == "bn") "Bluetooth সেটিংস খুলেছি।" else "Bluetooth settings opened."
            }
            command.contains("display") || command.contains("brightness") ||
                command.contains("ডিসপ্লে") || command.contains("উজ্জ্বলতা") -> {
                startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
                greeting + if (language == "bn") "Display সেটিংস খুলেছি।" else "Display settings opened."
            }
            command.contains("time") || command.contains("সময়") || command.contains("সময়") ||
                command.contains("কয়টা") || command.contains("কটা") -> {
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                if (language == "bn") "এখন সময় $time।" else "The time is $time."
            }
            else -> if (language == "bn") {
                "এই কমান্ডটি এখনো যুক্ত করা হয়নি। অনলাইন AI চালু করতে নিরাপদ backend সংযোগ প্রয়োজন।"
            } else {
                "That command is not available yet. Online AI requires a secure backend connection."
            }
        }
    }

    private fun openApp(packageName: String, label: String, language: String): String {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            startActivity(launchIntent)
            if (language == "bn") "$label খুলেছি।" else "$label opened."
        } else {
            if (language == "bn") "$label ইনস্টল করা নেই বা খোলা যাচ্ছে না।"
            else "$label is not installed or cannot be opened."
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}

@Composable
private fun BlackCatDashboard(activity: MainActivity) {
    val prefs = remember { activity.getSharedPreferences("black_cat_preferences", Context.MODE_PRIVATE) }
    var language by remember { mutableStateOf(prefs.getString("language", "bn") ?: "bn") }
    var address by remember { mutableStateOf(prefs.getString("address", "বস") ?: "বস") }
    var command by remember { mutableStateOf("") }
    var reply by remember {
        mutableStateOf("আমি BLACK CAT AI। মাইক্রোফোন চাপুন অথবা নিচে কমান্ড লিখুন।")
    }
    val recognized by activity.recognizedTextState
    val listening by activity.listeningState
    val isBangla = language == "bn"

    LaunchedEffect(recognized) {
        if (recognized.isNotBlank()) command = recognized
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Night) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))
            Text("BLACK CAT", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("PERSONAL AI ASSISTANT", color = Cyan, fontSize = 11.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Panel)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(if (listening) "LISTENING" else "READY", color = if (listening) Cyan else Violet, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("◉", color = if (listening) Cyan else Violet, fontSize = 64.sp)
                    Text(
                        if (isBangla) "আপনার নির্দেশের অপেক্ষায়" else "Waiting for your command",
                        color = Color.White
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { activity.startVoiceInput(language) },
                        colors = ButtonDefaults.buttonColors(containerColor = Violet)
                    ) {
                        Text(if (listening) "শুনছি..." else if (isBangla) "কথা বলুন" else "Speak")
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Panel)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("BLACK CAT", color = Cyan, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(reply, color = Color.White, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (isBangla) "আপনার কমান্ড লিখুন" else "Type a command") },
                minLines = 2
            )
            Button(
                onClick = {
                    if (command.isNotBlank()) {
                        reply = activity.runCommand(command, language, address)
                        activity.speak(reply, language)
                        command = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Violet)
            ) {
                Text(if (isBangla) "কমান্ড চালান" else "Run command")
            }

            Spacer(Modifier.height(22.dp))
            Text(if (isBangla) "দ্রুত কাজ" else "QUICK ACTIONS", color = Cyan, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAction("YouTube", Modifier.weight(1f)) {
                    reply = activity.runCommand("open youtube", language, address)
                    activity.speak(reply, language)
                }
                QuickAction("WhatsApp", Modifier.weight(1f)) {
                    reply = activity.runCommand("open whatsapp", language, address)
                    activity.speak(reply, language)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAction("Dialer", Modifier.weight(1f)) {
                    reply = activity.runCommand("open dialer", language, address)
                    activity.speak(reply, language)
                }
                QuickAction("Wi-Fi", Modifier.weight(1f)) {
                    reply = activity.runCommand("open wifi", language, address)
                    activity.speak(reply, language)
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(if (isBangla) "ব্যক্তিগত সেটিংস" else "PERSONAL SETTINGS", color = Cyan, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {
                    language = "bn"
                    prefs.edit().putString("language", language).apply()
                }) { Text("বাংলা", color = if (isBangla) Cyan else SoftText) }
                TextButton(onClick = {
                    language = "en"
                    prefs.edit().putString("language", language).apply()
                }) { Text("English", color = if (!isBangla) Cyan else SoftText) }
            }
            OutlinedTextField(
                value = address,
                onValueChange = {
                    address = it
                    prefs.edit().putString("address", it).apply()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (isBangla) "আপনাকে কী নামে ডাকবে" else "How should I address you") },
                singleLine = true
            )
            Spacer(Modifier.height(14.dp))
            Text(
                if (isBangla) "ভয়েসের মান ও কণ্ঠের ধরন আপনার ফোনের Text-to-Speech ইঞ্জিনের ওপর নির্ভর করে।"
                else "Voice quality and available voice types depend on your device's Text-to-Speech engine.",
                color = SoftText,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun QuickAction(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(48.dp)) {
        Text(label, fontSize = 13.sp)
    }
}
