package zg.acelera.controller

import zg.acelera.dto.match.AuthorizedMatchResponseDTO
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

    AuthorizedMatchResponseDTO getAuthorizedMatch(UUID candidateId, UUID jobId, UUID requesterId, String requesterRole,
                                                  UUID recruiterId = null) {
        return matchService.getAuthorizedMatch(candidateId, jobId, requesterId, requesterRole, recruiterId)
    }
}
