package zg.acelera.repository.like

import groovy.sql.GroovyRowResult
import groovy.sql.Sql

class JobLikeRepositoryJDBC implements LikeRepository {
    private final Sql sql

    JobLikeRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    void like(UUID entityLikedId, UUID userId) {
        sql.executeInsert('INSERT INTO jobs_like (job_id, candidate_id) VALUES (?, ?)', [entityLikedId, userId])
    }

    @Override
    void unlike(UUID entityLikedId, UUID userId) {
        sql.executeUpdate('DELETE FROM jobs_like WHERE job_id = ? AND candidate_id = ?', [entityLikedId, userId])
    }

    @Override
    boolean existsLike(UUID entityLikedId, UUID userId) {
        GroovyRowResult result = sql.firstRow("""
            SELECT EXISTS (
                SELECT 1 FROM jobs_like WHERE job_id = ? AND candidate_id = ?
            ) AS liked
        """, [entityLikedId, userId])

        return result.liked as boolean
    }
}
