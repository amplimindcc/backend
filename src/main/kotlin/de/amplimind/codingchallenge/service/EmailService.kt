package de.amplimind.codingchallenge.service

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(private val sender: JavaMailSender) {
    fun sendEmail() {
        val message = SimpleMailMessage()
        // TODO: This can be rewritten to contain the actual invite later
        // currently only a testing mail
        message.setTo("amplimindcodingchallenge@gmail.com")
        message.subject = "test"
        message.text = "Test EmailService Mail"
        sender.send(message)
    }
}
