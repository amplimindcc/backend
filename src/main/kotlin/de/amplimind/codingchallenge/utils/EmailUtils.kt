package de.amplimind.codingchallenge.utils

import org.springframework.beans.factory.annotation.Value

/**
 * Utils object for creating emails
 */
object EmailUtils {
    // TODO fix server variable.
    @Value("\${app.frontend.url}")
    const val SERVER_URL: String = "null"

    const val USER_SUBJECT: String = "Invitation to your coding challange"

    const val ADMIN_SUBJECT: String = "Invitation for amplimind coding challange platform"

    const val EMAIL_SIGNATURE: String =
        "<br>" +
            "<br>" +
            "<br>" +
            "Vielen Dank und viele Grüße<br>" +
            "Anis<br>" +
            "<br>" +
            "Anis Rahimic > Managing Director<br>" +
            "+49 151 58922570<br>" +
            "anis.rahimic@amplimind.io<br>" +
            "amplimind.io<br><br>" +
            "amplimind GmbH > LabCampus 48 > 85356 München-Flughafen > Germany<br>" +
            "powered by Audi and Lufthansa Industry Solutions <br><br>" +
            "Sitz/Domicile: München<br>" +
            "Registereintrag/Court of Registry: Amtsgericht München HRB 278664<br>" +
            "Geschäftsführung/Managing Directors: Anis Rahimic, Bettina Bernhardt<br>"
}
