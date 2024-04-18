package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.dto.request.InviteRequestDTO
import de.amplimind.codingchallenge.utils.JWTUtils
import de.amplimind.codingchallenge.utils.JWTUtils.INVITE_LINK_EXPIRATION_DAYS
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Service to send emails
 */
@Service
class EmailService(private val mailsender: JavaMailSender) {
    // TODO remove this method and use the different send mail method
    /**
     * email to the applicant the URL to set the password
     * @param email The email address, where the email will be sent
     */
    fun sendEmail(inviteRequestDTO: InviteRequestDTO) {
        val claims = mapOf(JWTUtils.MAIL_KEY to inviteRequestDTO.email, JWTUtils.ADMIN_KEY to inviteRequestDTO.isAdmin)
        val token = JWTUtils.createToken(claims, Date.from(Instant.now().plus(INVITE_LINK_EXPIRATION_DAYS, ChronoUnit.DAYS)))

        val message = SimpleMailMessage()
        // TODO: This can be rewritten to contain the actual invite later
        // currently only a testing mail
        message.setTo(inviteRequestDTO.email)
        message.subject = "test"
        // TODO: change localhost to Constant with actual Servername
        message.text = "You have ${JWTUtils.INVITE_LINK_EXPIRATION_DAYS} Days to start your Coding Challenge. " +
            "After 5 Days the link below will expire http://localhost:5174/invite/$token"
        mailsender.send(message)
    }

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
        val message = SimpleMailMessage()
        message.setTo(email)
        message.subject = subject
        message.text = text
        mailsender.send(message)
    }
}
