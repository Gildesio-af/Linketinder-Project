package zg.acelera.repository

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.Address
import zg.acelera.domain.Company
import zg.acelera.domain.Job
import zg.acelera.utils.exception.EntityNotFoundException

class JobRepositoryJDBC implements IJobRepository {
    Sql sql

    JobRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    Job findById(UUID id) {
        GroovyRowResult row = sql.firstRow("""
            SELECT * FROM jobs AS jb WHERE jb.id = ?
        """, [id])
        if (row) return getJobFromRow(row)

        throw new EntityNotFoundException("Job with ID ${id} not found")
    }

    @Override
    Set<Job> findByName(String name) {
        Set<GroovyRowResult> rows = sql.rows("""
            SELECT * FROM jobs AS jb WHERE lower(jb.name) LIKE lower(?)
        """, ["%"+ name +"%"])

        Set<Job> jobs = rows.collect { row -> getJobFromRow(row) }

        return jobs
    }

    @Override
    Set<Job> findBySkill(String skill) {
        Set<GroovyRowResult> rows = sql.rows("""
            SELECT * FROM jobs AS jb
            INNER JOIN jobs_skill AS js ON js.job_id = jb.id
            INNER JOIN skills AS sk ON sk.id = js.skill_id
            WHERE lower(sk.name) = lower(?)
        """, [skill])

        return rows.collect { row -> getJobFromRow(row) } as Set<Job>
    }

    @Override
    Job create(Job job, List<UUID> skillIds) {
        GroovyRowResult row = sql.firstRow("""
            INSERT INTO jobs (name, description, address_id, publisher_id)
            VALUES (?, ?, ?, ?)
            RETURNING *
        """, [job.name, job.description, job.address.id, job.publisher.id])

        return getJobFromRow(row)
    }

    @Override
    Job update(Job job) {
        GroovyRowResult row = sql.firstRow("""
            UPDATE jobs
            SET name = COALESCE(?, name), description = COALESCE(?, description)
            WHERE id = ?
            RETURNING *
        """, [job.name, job.description, job.id])

        if (row) return getJobFromRow(row)

        throw new EntityNotFoundException("Job with ID ${job.id} not found")
    }

    @Override
    void addSkillToJob(UUID jobId, Set<UUID> skillIds) {
        sql.withTransaction {
            skillIds.each { skillId ->
                sql.executeUpdate("""
                    INSERT INTO jobs_skill (job_id, skill_id)
                    VALUES (?, ?)
                """, [jobId, skillId])
            }
        }
    }

    @Override
    void delete(UUID id) {
        int rowsAffected = sql.executeUpdate("""
            DELETE FROM jobs WHERE id = ?
        """, [id])

        if (rowsAffected == 0) {
            throw new EntityNotFoundException("Job with ID ${id} not found")
        }
    }

    private static Job getJobFromRow(GroovyRowResult row) {
        return new Job(
            id: UUID.fromString(row.id.toString()),
            name: row.name,
            description: row.description,
            address: new Address(id: UUID.fromString(row.address_id.toString())),
            publisher: new Company(id: UUID.fromString(row.publisher_id.toString()))
        )
    }
}
