package zg.acelera.service

import zg.acelera.domain.Match
import zg.acelera.dto.match.MatchCreateDTO
import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.repository.match.MatchRepository

class MatchService {
    private final MatchRepository matchRepository

    MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository
    }

    MatchResponseDTO createMatch(MatchCreateDTO matchCreateDTO) {
        if (matchRepository.existsMatch(matchCreateDTO.candidateId(), matchCreateDTO.jobId())) return null

        Match createdMatch = matchRepository.createMatch(matchCreateDTO.toDomain())
        return MatchResponseDTO.fromDomain(createdMatch)
    }

    List<MatchResponseDTO> getMatchesByCandidate(UUID candidateId) {
        return matchRepository.findMatchByCandidateId(candidateId).collect { MatchResponseDTO.fromDomain(it) }
    }

    List<MatchResponseDTO> getMatchesByCompany(UUID companyId) {
        return matchRepository.findMatchByCompanyId(companyId).collect { MatchResponseDTO.fromDomain(it) }
    }

    List<MatchResponseDTO> getMatchesByJob(UUID jobId) {
        return matchRepository.findMatchByJobId(jobId).collect { MatchResponseDTO.fromDomain(it) }
    }

    void deleteMatchByCandidateAndJob(UUID candidateId, UUID jobId) {
        matchRepository.deleteMatchByCandidateAndJob(candidateId, jobId)
    }

    void deleteMatchesByCandidateAndCompany(UUID candidateId, UUID companyId) {
        matchRepository.deleteMatchesByCandidateAndCompany(candidateId, companyId)
    }
}