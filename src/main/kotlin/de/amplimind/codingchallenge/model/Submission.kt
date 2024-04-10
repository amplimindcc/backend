package de.amplimind.codingchallenge.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.sql.Timestamp
import java.util.*

@Entity
class Submission(
    @Id
    val uuid: UUID,
    val expirationDate: Timestamp,
    val projectID: UUID,
    val turnInDate: Timestamp,
    var status : SubmissionStates

) {

}