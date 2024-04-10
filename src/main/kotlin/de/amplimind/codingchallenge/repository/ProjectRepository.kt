package de.amplimind.codingchallenge.repository

import de.amplimind.codingchallenge.model.Project
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectRepository : JpaRepository<Project, Long>
