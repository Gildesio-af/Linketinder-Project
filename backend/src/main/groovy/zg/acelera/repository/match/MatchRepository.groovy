package zg.acelera.repository.match

import zg.acelera.domain.Match

interface MatchRepository {
    Match createMatch(Match match)
    List<Match> findMatchByCandidateId(UUID candidateId)
    List<Match> findMatchByCompanyId(UUID companyId)
    List<Match> findMatchByJobId(UUID jobId)
    boolean existsMatch(UUID candidateId, UUID jobId)
    void deleteMatchByCandidateAndJob(UUID candidateId, UUID jobId)
    void deleteMatchesByCandidateAndCompany(UUID candidateId, UUID companyId)
}