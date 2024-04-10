package de.amplimind.codingchallenge.repository

import de.amplimind.codingchallenge.model.Abgabe
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AbgabeRepository:JpaRepository<Abgabe,UUID>