package zg.acelera.repository.match

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.Match

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.temporal.TemporalAccessor

class MatchRepositoryJDBC implements MatchRepository {
    private final Sql sql

    MatchRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    Match createMatch(Match match) {
        GroovyRowResult createdMatch = sql.firstRow("""
            INSERT INTO matches (candidate_id, company_id, job_id) VALUES (?, ?, ?)
            RETURNING *
        """, [match.candidateId, match.companyId, match.jobId])

        return getMatchByRowResult(createdMatch)
    }

    @Override
    List<Match> findMatchByCandidateId(UUID candidateId) {
        List<GroovyRowResult> matchRowsByCandidateId = sql.rows("""
            SELECT * FROM matches WHERE candidate_id = ?
        """, [candidateId])

        return matchRowsByCandidateId.collect { getMatchByRowResult(it) }
    }

    @Override
    List<Match> findMatchByCompanyId(UUID companyId) {
        List<GroovyRowResult> matchRowsByCompanyId = sql.rows("""
            SELECT * FROM matches WHERE company_id = ?
        """, [companyId])

        return matchRowsByCompanyId.collect { getMatchByRowResult(it) }
    }

    @Override
    List<Match> findMatchByJobId(UUID jobId) {
        List<GroovyRowResult> matchRowsByJobId = sql.rows("""
            SELECT * FROM matches WHERE job_id = ?
        """, [jobId])

        return matchRowsByJobId.collect { getMatchByRowResult(it) }
    }

    @Override
    boolean existsMatch(UUID candidateId, UUID jobId) {
        GroovyRowResult result = sql.firstRow("""
            SELECT EXISTS (
                SELECT 1 FROM matches WHERE candidate_id = ? AND job_id = ?
            ) AS matched
        """, [candidateId, jobId])

        return result.matched as boolean
    }

    @Override
    void deleteMatchByCandidateAndJob(UUID candidateId, UUID jobId) {
        sql.executeUpdate('DELETE FROM matches WHERE candidate_id = ? AND job_id = ?', [candidateId, jobId])
    }

    @Override
    void deleteMatchesByCandidateAndCompany(UUID candidateId, UUID companyId) {
        sql.executeUpdate('DELETE FROM matches WHERE candidate_id = ? AND company_id = ?', [candidateId, companyId])
    }

    private static Match getMatchByRowResult(GroovyRowResult matchRow) {
        new Match(
                id: UUID.fromString(matchRow.id.toString()),
                candidateId: UUID.fromString(matchRow.candidate_id.toString()),
                companyId: UUID.fromString(matchRow.company_id.toString()),
                jobId: UUID.fromString(matchRow.job_id.toString()),
                createdAt: matchRow.created_at ? ((Timestamp) matchRow.created_at).toLocalDateTime() : null
        )
    }
}
