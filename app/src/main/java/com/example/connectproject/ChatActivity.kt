package com.example.connectproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.connectproject.databinding.ActivityChatBinding
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

//https://ai.google.dev/tutorials/get_started_android#kotlin
class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chat: Chat
    private var stringBuilder: StringBuilder = java.lang.StringBuilder()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val generativeModel = GenerativeModel(
            modelName = "gemini-pro",
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

        binding.editTextOutput.text = stringBuilder.toString()

        binding.navigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    return@setOnItemSelectedListener true
                }
                R.id.chat -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }
    }

    fun buttonSendChat(view: View) {
        stringBuilder.append(binding.editTextInput.text.toString() + "\n")
        MainScope().launch {
            val result = chat.sendMessage(binding.editTextInput.text.toString())
            stringBuilder.append(result.text + "\n")

            binding.editTextOutput.text = stringBuilder.toString()

            binding.editTextInput.setText("") //to reset text when sending
        }
    }
}