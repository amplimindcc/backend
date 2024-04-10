package de.amplimind.codingchallenge.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.sql.Timestamp
import java.util.*

@Entity
class Abgabe(
    @Id
    val uuid: UUID,
    val ablauf: Timestamp,
    val projectID: UUID,
    val abgabeDatum: Timestamp,
    var status : AbgabeStates

) {

}