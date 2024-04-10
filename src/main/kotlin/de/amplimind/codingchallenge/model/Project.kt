package de.amplimind.codingchallenge.model

import jakarta.persistence.Id
import java.util.UUID

class Project(
    @Id
    val id:UUID,
    val description: String,
    var active: Boolean
) {
}