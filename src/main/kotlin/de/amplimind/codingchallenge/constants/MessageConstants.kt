package de.amplimind.codingchallenge.constants

/**
 * Constants for messages which are used in the application
 */
object MessageConstants {

    /**
     * The subject for the user emails
     */
    const val USER_SUBJECT: String = "Invitation to your coding challange"

    /**
     * The subject for the admin emails
     */
    const val ADMIN_SUBJECT: String = "Invitation for amplimind coding challange platform"

    /**
     * The default signature for the emails
     */
    val EMAIL_SIGNATURE: String =
        """
        <br>
        <br>
        <br>
        Vielen Dank und viele Grüße<br>
        Anis<br>
        <br>
        Anis Rahimic > Managing Director<br>
        +49 151 58922570<br>
        anis.rahimic@amplimind.io<br>
        amplimind.io<br><br>
        amplimind GmbH > LabCampus 48 > 85356 München-Flughafen > Germany<br>
        powered by Audi and Lufthansa Industry Solutions <br><br>
        Sitz/Domicile: München<br>
        Registereintrag/Court of Registry: Amtsgericht München HRB 278664<br>
        Geschäftsführung/Managing Directors: Anis Rahimic, Bettina Bernhardt<br>
        """.trimIndent()
}
