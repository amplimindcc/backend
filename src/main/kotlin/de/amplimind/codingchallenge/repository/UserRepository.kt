package de.amplimind.codingchallenge.repository

import de.amplimind.codingchallenge.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String>
