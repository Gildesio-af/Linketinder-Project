package zg.acelera.repository.like

import groovy.sql.GroovyRowResult
import groovy.sql.Sql

class CompanyLikeRepositoryJDBC implements LikeRepository {
    private final Sql sql

    CompanyLikeRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    void like(UUID entityLikedId, UUID userId) {
        sql.executeInsert('INSERT INTO companies_like (company_id, liked_candidate_id) VALUES (?, ?)', [userId, entityLikedId])
    }

    @Override
    void unlike(UUID entityLikedId, UUID userId) {
        sql.executeUpdate('DELETE FROM companies_like WHERE company_id = ? AND liked_candidate_id = ?', [userId, entityLikedId])
    }

    @Override
    boolean existsLike(UUID entityLikedId, UUID userId) {
        GroovyRowResult result = sql.firstRow("""
            SELECT EXISTS (
                SELECT 1 FROM companies_like WHERE company_id = ? AND liked_candidate_id = ?
            ) AS liked
        """, [userId, entityLikedId])

        return result.liked as boolean
    }
}