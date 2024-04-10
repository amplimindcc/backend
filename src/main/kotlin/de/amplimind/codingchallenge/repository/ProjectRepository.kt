package de.amplimind.codingchallenge.repository

import de.amplimind.codingchallenge.model.Project
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProjectRepository:JpaRepository<Project,UUID> {
}