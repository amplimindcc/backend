package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.jwt.JWTUtils
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(private val sender: JavaMailSender) {
    /**
     * email to the applicant the URL to set the password
     * @param email The email address, where the email will be sent
     */

    fun sendEmail(email: String) {
        val claims = mapOf(JWTUtils.MAIL_KEY to email)
        val token = JWTUtils.createToken(claims)

        val message = SimpleMailMessage()
        // TODO: This can be rewritten to contain the actual invite later
        // currently only a testing mail
        message.setTo(email)
        message.subject = "test"
        // TODO: change localhost to Constant with actual Servername
        message.text = "You have ${JWTUtils.EXPIRATION_FROM_CREATION} Days to start your Coding Challenge. After 5 Days the link below will expire localhost:8080/v1/auth/invite/$token"
        sender.send(message)
    }
}
