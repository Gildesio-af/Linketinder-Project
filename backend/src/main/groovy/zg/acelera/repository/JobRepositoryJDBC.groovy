package zg.acelera.repository

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.Address
import zg.acelera.domain.Company
import zg.acelera.domain.Job
import zg.acelera.domain.Skill
import zg.acelera.utils.exception.EntityNotFoundException

class JobRepositoryJDBC implements IJobRepository {
    Sql sql

    JobRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    Job findById(UUID id) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT jb.*, sk.id AS skill_id, sk.name AS skill_name
            FROM jobs AS jb
            LEFT JOIN jobs_skill AS js ON js.job_id = jb.id
            LEFT JOIN skills AS sk ON sk.id = js.skill_id
            WHERE jb.id = ?
        """, [id])

        if (!rows) {
            throw new EntityNotFoundException("Job with ID ${id} not found")
        }

        return getJobsWithSkillsFromRows(rows).first()
    }

    @Override
    Set<Job> findAll() {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT jb.*, sk.id AS skill_id, sk.name AS skill_name 
            FROM jobs AS jb
            LEFT JOIN jobs_skill AS js ON js.job_id = jb.id
            LEFT JOIN skills AS sk ON sk.id = js.skill_id
        """)

        return getJobsWithSkillsFromRows(rows)
    }

    @Override
    Set<Job> findByName(String name) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT jb.*, sk.id AS skill_id, sk.name AS skill_name
            FROM jobs AS jb
            LEFT JOIN jobs_skill AS js ON js.job_id = jb.id
            LEFT JOIN skills AS sk ON sk.id = js.skill_id
            WHERE lower(jb.name) LIKE lower(?)
        """, ["%" + name + "%"])

        return getJobsWithSkillsFromRows(rows)
    }

    @Override
    Set<Job> findBySkill(String skill) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT jb.*, sk.id AS skill_id, sk.name AS skill_name
            FROM jobs AS jb
            LEFT JOIN jobs_skill AS js ON js.job_id = jb.id
            LEFT JOIN skills AS sk ON sk.id = js.skill_id
            WHERE jb.id IN (
                SELECT DISTINCT js2.job_id
                FROM jobs_skill AS js2
                INNER JOIN skills AS sk2 ON sk2.id = js2.skill_id
                WHERE lower(sk2.name) = lower(?)
            )
        """, [skill])

        return getJobsWithSkillsFromRows(rows)
    }

    @Override
    Set<Job> findByPublisherId(UUID publisherId) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT jb.*, sk.id AS skill_id, sk.name AS skill_name
            FROM jobs AS jb
            LEFT JOIN jobs_skill AS js ON js.job_id = jb.id
            LEFT JOIN skills AS sk ON sk.id = js.skill_id
            WHERE jb.publisher_id = ?
        """, [publisherId])

        return getJobsWithSkillsFromRows(rows)
    }

    @Override
    Job create(Job job, List<UUID> skillIds) {
        GroovyRowResult row = sql.firstRow("""
            INSERT INTO jobs (name, description, address_id, publisher_id)
            VALUES (?, ?, ?, ?)
            RETURNING *
        """, [job.name, job.description, job.address.id, job.publisher.id])

        if (!row) {
            return null
        }

        if (skillIds) {
            addSkillToJob(UUID.fromString(row.id.toString()), skillIds as Set<UUID>)
        }

        List<GroovyRowResult> rows = sql.rows("""
            SELECT jb.*, sk.id AS skill_id, sk.name AS skill_name
            FROM jobs AS jb
            LEFT JOIN jobs_skill AS js ON js.job_id = jb.id
            LEFT JOIN skills AS sk ON sk.id = js.skill_id
            WHERE jb.id = ?
        """, [row.id])

        return getJobsWithSkillsFromRows(rows).first()
    }

    @Override
    Job update(Job job) {
        GroovyRowResult row = sql.firstRow("""
            UPDATE jobs
            SET name = COALESCE(?, name), description = COALESCE(?, description)
            WHERE id = ?
            RETURNING *
        """, [job.name, job.description, job.id])

        if (!row) {
            throw new EntityNotFoundException("Job with ID ${job.id} not found")
        }

        List<GroovyRowResult> rows = sql.rows("""
            SELECT jb.*, sk.id AS skill_id, sk.name AS skill_name
            FROM jobs AS jb
            LEFT JOIN jobs_skill AS js ON js.job_id = jb.id
            LEFT JOIN skills AS sk ON sk.id = js.skill_id
            WHERE jb.id = ?
        """, [row.id])

        return getJobsWithSkillsFromRows(rows).first()
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
        sql.withTransaction {
            GroovyRowResult row = sql.firstRow("""
                DELETE FROM jobs WHERE id = ? RETURNING address_id
            """, [id])

            if (row?.address_id) {
                sql.executeUpdate("""
                    DELETE FROM addresses WHERE id = ?
                """, [row.address_id])
            }
        }
    }

    private static Set<Job> getJobsWithSkillsFromRows(List<GroovyRowResult> rows) {
        if (!rows) return [] as Set<Job>

        return rows.groupBy { it.id }.collect { jobId, jobRows ->
            getJobWithSkillsFromRows(jobRows)
        } as Set<Job>
    }

    private static Job getJobWithSkillsFromRows(List<GroovyRowResult> jobRows) {
        if (!jobRows) return null

        GroovyRowResult firstRow = jobRows.first()

        Job job = getJobFromRow(firstRow)

        Set<Skill> skills = jobRows.findResults { row ->
            if (row.skill_id) {
                return new Skill(
                        id: UUID.fromString(row.skill_id.toString()),
                        name: row.skill_name.toString()
                )
            }
            return null
        } as Set<Skill>

        job.desiredSkills = skills

        return job
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
