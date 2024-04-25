package de.amplimind.codingchallenge.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version

/**
 * Holds information about the expiration of an invite token for a certain user
 */
@Entity
@Table(name = "invite_tokens_expiration")
class InviteTokenExpiration(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val email: String,
    @Column(name = "token_expiration")
    var expirationInMillis: Long,
    @Version
    var version: Long? = null,
)
