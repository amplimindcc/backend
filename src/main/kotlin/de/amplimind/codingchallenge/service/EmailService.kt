package de.amplimind.codingchallenge.service

import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

/**
 * Service to send emails
 */
@Service
class EmailService(private val mailSender: JavaMailSender) {
    /**
     * sends an email to the specified email address
     * @param email the email address to send the email to
     * @param subject the subject of the email
     * @param text the text of the email
     */
    fun sendEmail(
        email: String,
        subject: String,
        text: String,
    ) {
        val message = mailSender.createMimeMessage()
        message.setRecipients(MimeMessage.RecipientType.TO, email)
        message.subject = subject
        message.setContent(text, "text/html; charset=UTF-8")
        mailSender.send(message)
    }
}
