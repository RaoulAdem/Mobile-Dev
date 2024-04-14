package com.example.connectproject

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

//https://ai.google.dev/tutorials/get_started_android#kotlin
class ChatActivity : AppCompatActivity() {
    lateinit var editTextInput: EditText
    lateinit var editTextOutput: EditText

    lateinit var chat: Chat

    var stringBuilder: StringBuilder = java.lang.StringBuilder()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        editTextInput = findViewById(R.id.editTextInput)
        editTextOutput = findViewById(R.id.editTextOutput)

        val generativeModel = GenerativeModel(
            // For text-only input, use the gemini-pro model
            modelName = "gemini-pro",
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = "AIzaSyCQ2q61-SJDtC6GNOZWTYE1HjaAf2hkJRQ"
        )

        chat = generativeModel.startChat(
            history = listOf(
                content(role = "user") { text("Hello, I have 2 dogs in my house.") },
                content(role = "model") { text("Great to meet you. What would you like to know?") }
            )
        )
        stringBuilder.append("Hello, I have 2 dogs in my house.\n")
        stringBuilder.append("Great to meet you. What would you like to know?\n")

        editTextOutput.setText(stringBuilder.toString())
    }

    public fun buttonSendChat(view: View) {
        stringBuilder.append(editTextInput.text.toString() + "\n")
        MainScope().launch {
            val result = chat.sendMessage(editTextInput.text.toString())
            stringBuilder.append(result.text + "\n")

            editTextOutput.setText(stringBuilder.toString())

            editTextInput.setText("") //to reset text when sending
        }
    }
}