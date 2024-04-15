package com.example.connectproject

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
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
    private val messageList = mutableListOf<Pair<String, Boolean>>()
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
                content(role = "user") { text("Hello, I need assistance with one of my course.") },
                content(role = "model") { text("Nice to meet you! How can I help you today?") }
            )
        )
        messageList.add("Hello, I need assistance with one of my course." to true)
        messageList.add("Nice to meet you! How can I help you today?" to false)
        displayMessages()

        binding.navigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    return@setOnItemSelectedListener true
                }
                R.id.profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }
    }

    fun buttonSendChat(view: View) {
        val userInput = binding.editTextInput.text.toString()
        binding.editTextInput.setText("") //reset text input when sending
        if (userInput.isNotEmpty()) {
            Toast.makeText(this, "Sending message...", Toast.LENGTH_LONG).show()
            messageList.add(userInput to true)
            MainScope().launch {
                val result = chat.sendMessage(userInput)
                result.text?.let { messageList.add(it to false) } //add bot response
                displayMessages()
            }
        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayMessages() {
        val spannableStringBuilder = SpannableString(messageList.joinToString("") { it.first + "\n" })
        messageList.forEachIndexed { index, message ->
            val start = spannableStringBuilder.indexOf(message.first)
            val end = start + message.first.length
            if (message.second) { //user response format
                spannableStringBuilder.setSpan(
                    StyleSpan(Typeface.ITALIC),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableStringBuilder.setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else { //bot response format
                spannableStringBuilder.setSpan(
                    ForegroundColorSpan(Color.RED),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableStringBuilder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableStringBuilder.setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        binding.editTextOutput.text = spannableStringBuilder
    }
}