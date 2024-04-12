package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.jwt.JWTUtils
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(private val sender: JavaMailSender) {
    fun sendEmail(email: String) {
        val claims = mapOf("email" to email)
        val token = JWTUtils.createToken(claims)

        val message = SimpleMailMessage()
        // TODO: This can be rewritten to contain the actual invite later
        // currently only a testing mail
        message.setTo(email)
        message.subject = "test"
        message.text = "You have 5 Days to start your Coding Challange. After 5 Days the link below will expire host/invite/$token"
        sender.send(message)
    }
}
