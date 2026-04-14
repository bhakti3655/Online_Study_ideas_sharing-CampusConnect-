package com.example.campusconnect

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {

    private const val SENDER_EMAIL = "campusconnect2801@gmail.com"
    private const val SENDER_PASSWORD = "rppy bkhb wpai zspj"

    fun sendWelcomeEmail(recipientEmail: String, userName: String) {
        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
            }
        })

        // Generate a random 6-digit code for "Professional" look
        val otpCode = (100000..999999).random().toString()

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SENDER_EMAIL, "CampusConnect Team"))
                addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                subject = "Welcome to CampusConnect - Verification Code: $otpCode"
                
                val emailContent = """
                    Hello $userName,
                    
                    Thank you for choosing CampusConnect!
                    
                    Your account has been successfully initialized. To complete your secure login, please use the following verification details:
                    
                    -------------------------------------------
                    Login ID: $recipientEmail
                    Security Code: $otpCode
                    -------------------------------------------
                    
                    Please Note: This code is for your records to verify your initial setup. You can now proceed to use all features of the CampusConnect Student Panel.
                    
                    If you did not perform this action, please contact our support immediately at $SENDER_EMAIL.
                    
                    Best Regards,
                    The CampusConnect Team
                    Official Website: campusconnect.edu
                """.trimIndent()
                
                setText(emailContent)
            }

            Thread {
                try {
                    Transport.send(message)
                } catch (e: MessagingException) {
                    e.printStackTrace()
                }
            }.start()

        } catch (e: MessagingException) {
            e.printStackTrace()
        }
    }
}