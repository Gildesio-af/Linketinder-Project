package zg.acelera.controller

import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.service.MatchService

class MatchController {
    private final MatchService matchService

    MatchController(MatchService matchService) {
        this.matchService = matchService
    }

    List<MatchResponseDTO> getMatchesByCandidate(UUID candidateId) {
        return matchService.getMatchesByCandidate(candidateId)
    }

    List<MatchResponseDTO> getMatchesByCompany(UUID companyId) {
        return matchService.getMatchesByCompany(companyId)
    }

    List<MatchResponseDTO> getMatchesByJob(UUID jobId) {
        return matchService.getMatchesByJob(jobId)
    }
}
