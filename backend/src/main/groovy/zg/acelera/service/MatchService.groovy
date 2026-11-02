package zg.acelera.service

import zg.acelera.domain.Match
import zg.acelera.domain.Recruiter
import zg.acelera.dto.auth.AuthResponseDTO
import zg.acelera.dto.match.AuthorizedMatchResponseDTO
import zg.acelera.dto.match.MatchCreateDTO
import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.repository.candidate.CandidateRepository
import zg.acelera.repository.company.CompanyRepository
import zg.acelera.repository.match.MatchRepository
import zg.acelera.repository.recruiter.RecruiterJobLikeRepository
import zg.acelera.repository.recruiter.RecruiterRepository

class MatchService {
    private final MatchRepository matchRepository
    private final CandidateRepository candidateRepository
    private final CompanyRepository companyRepository
    private final RecruiterRepository recruiterRepository
    private final RecruiterJobLikeRepository recruiterJobLikeRepository

    MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository
        this.candidateRepository = null
        this.companyRepository = null
        this.recruiterRepository = null
        this.recruiterJobLikeRepository = null
    }

    MatchService(MatchRepository matchRepository,
                 CandidateRepository candidateRepository,
                 CompanyRepository companyRepository,
                 RecruiterRepository recruiterRepository,
                 RecruiterJobLikeRepository recruiterJobLikeRepository) {
        this.matchRepository = matchRepository
        this.candidateRepository = candidateRepository
        this.companyRepository = companyRepository
        this.recruiterRepository = recruiterRepository
        this.recruiterJobLikeRepository = recruiterJobLikeRepository
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

    AuthorizedMatchResponseDTO getAuthorizedMatch(UUID candidateId, UUID jobId, UUID requesterId, String requesterRole,
                                                  UUID recruiterId = null) {
        if (candidateId == null || jobId == null || requesterId == null || !requesterRole)
            throw new SecurityException('Match and requester authentication are required.')
        if (!candidateRepository || !companyRepository || !recruiterRepository || !recruiterJobLikeRepository)
            throw new IllegalStateException('Match authorization is not configured.')

        Match match = matchRepository.findMatchByCandidateId(candidateId)
                .find { it.jobId == jobId }
        if (!match)
            throw new SecurityException('Match not found or not authorized.')
        Recruiter recruiter = null
        switch (requesterRole) {
            case AuthResponseDTO.CANDIDATE_ROLE:
                if (match.candidateId != requesterId)
                    throw new SecurityException('The authenticated user is not authorized for this match.')
                if (recruiterId != null) {
                    if (!recruiterJobLikeRepository.existsActive(recruiterId, match.jobId, match.candidateId) ||
                            !recruiterJobLikeRepository.hasAcceptedAssignment(recruiterId, match.jobId))
                        throw new SecurityException('The recruiter is not authorized for this match.')
                    recruiter = recruiterRepository.findById(recruiterId)
                }
                break
            case AuthResponseDTO.COMPANY_ROLE:
                if (companyRepository.findByJobId(match.jobId)?.id != requesterId)
                    throw new SecurityException('Only the company that owns the job may access this match.')
                break
            case AuthResponseDTO.RECRUITER_ROLE:
                if (requesterId != recruiterId ||
                        !recruiterJobLikeRepository.existsActive(requesterId, match.jobId, match.candidateId) ||
                        !recruiterJobLikeRepository.hasAcceptedAssignment(requesterId, match.jobId))
                    throw new SecurityException('The authenticated recruiter is not authorized for this match.')
                recruiter = recruiterRepository.findById(requesterId)
                break
            default:
                throw new SecurityException('Unsupported requester role.')
        }

        def candidate = candidateRepository.findById(match.candidateId)
        def company = companyRepository.findById(match.companyId)
        AuthorizedMatchResponseDTO.fromMatch(match, candidate.name, candidate.email, candidate.description,
                company.name, company.email, company.description, recruiter)
    }

    void deleteMatchByCandidateAndJob(UUID candidateId, UUID jobId) {
        matchRepository.deleteMatchByCandidateAndJob(candidateId, jobId)
    }

    void deleteMatchIfNoActiveRecruiterLike(UUID candidateId, UUID jobId) {
        matchRepository.deleteMatchByCandidateAndJobIfNoActiveRecruiterLike(candidateId, jobId)
    }

    void deleteMatchesByCandidateAndCompany(UUID candidateId, UUID companyId) {
        matchRepository.deleteMatchesByCandidateAndCompany(candidateId, companyId)
    }
}