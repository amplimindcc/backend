package de.amplimind.codingchallenge.repository

import de.amplimind.codingchallenge.model.Project
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectRepository : JpaRepository<Project, Long>{
    /**
     * returns all ids from Projects which are active
     */

    @Query("SELECT p.id FROM projects p where r.active = true")
    fun findAllActiveProjects(): List<long>?
}
