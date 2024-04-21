package de.amplimind.codingchallenge.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.Version
import java.sql.Timestamp

@Entity
@Table(name = "submissions")
class Submission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val userEmail: String,
    val expirationDate: Timestamp,
    val projectID: Long,
    val turnInDate: Timestamp,
    var status: SubmissionStates,
    @Version
    var version: Long? = null
)
