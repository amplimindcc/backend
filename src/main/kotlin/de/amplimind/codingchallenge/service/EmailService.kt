package de.amplimind.codingchallenge.service

import de.amplimind.codingchallenge.utils.EmailUtils
import de.amplimind.codingchallenge.utils.JWTUtils
import de.amplimind.codingchallenge.utils.JWTUtils.INVITE_LINK_EXPIRATION_DAYS
import jakarta.mail.internet.MimeMessage
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

    // TODO move to own configfile
    val SUBMISSION_EXPIRATION_DAYS = 3

    /**
     * email to the applicant the URL to set the password
     * @param inviteRequestDTO the dto containing all relevant information about the invite being sent out
     */
    fun sendUserEmail(email: String) {

        val claims = mapOf(JWTUtils.MAIL_KEY to email, JWTUtils.ADMIN_KEY to false)
        val token =
            JWTUtils.createToken(
                claims,
                Date.from(Instant.now().plus(INVITE_LINK_EXPIRATION_DAYS, ChronoUnit.DAYS)),
            )

        val subject = EmailUtils.USER_SUBJECT
        val text =
                    "<p>Sehr geehrter Bewerber,<br>" +
                    "<br>" +
                    "wir laden Sie hiermit zu ihrer Coding Challange ein.<br>" +
                    "Mit dem unten stehenden Link können Sie sich auf unserer Plattform registrieren.<br>" +
                    "<br>" +
                    "<a href=\"${EmailUtils.SERVER_URL}/invite/$token\">Für Coding Challange registrieren</a><br>" +
                    "<br>" +
                    "<b>Der Link läuft nach ${JWTUtils.INVITE_LINK_EXPIRATION_DAYS} Tagen ab.</b> Nachdem Sie sich registriert haben,<br> können Sie ihre Aufgabe einsehen. Ab dann haben Sie <b>$SUBMISSION_EXPIRATION_DAYS Tage</b> Zeit ihre Lösung hochzuladen.<br>" +
                    EmailUtils.EMAIL_SIGNATURE +
                    "</p>"
        sendEmail(email, subject, text)
    }


    fun sendAdminEmail(email: String) {
        val claims = mapOf(JWTUtils.MAIL_KEY to email, JWTUtils.ADMIN_KEY to true)
        val token =
            JWTUtils.createToken(
                claims,
                Date.from(Instant.now().plus(INVITE_LINK_EXPIRATION_DAYS, ChronoUnit.DAYS)),
            )

        val subject = EmailUtils.ADMIN_SUBJECT
        val text =
            "<p>Hallo,<br>" +
                    "<br>" +
                    "Mit dem unten stehenden Link können sie sich als Admin auf der coding challange Plattform von Amplimind registrieren.<br>" +
                    "<br>" +
                    "<a href=\"${EmailUtils.SERVER_URL}/invite/$token\">Jetzt registrieren</a><br>" +
                    "<br>" +
                    "<b>Der Link läuft nach ${JWTUtils.INVITE_LINK_EXPIRATION_DAYS} Tagen ab.</b>" +
                    EmailUtils.EMAIL_SIGNATURE +
                    "</p>"

        sendEmail(email, subject, text)
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
        val message = mailsender.createMimeMessage()
        message.setRecipients(MimeMessage.RecipientType.TO, email)
        message.subject = subject
        message.setContent(text, "text/html; charset=UTF-8")
        mailsender.send(message)
    }
}
