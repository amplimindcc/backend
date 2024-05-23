package de.amplimind.codingchallenge.repository

import de.amplimind.codingchallenge.model.Project
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repository responsible for the storage of [Project]s.
 */
interface ProjectRepository : JpaRepository<Project, Long> {
    /**
     * Find all projects by active status.
     * @param active the active status of the project
     * @return a list of projects
     */
    fun findByActive(active: Boolean = true): List<Project>
}
