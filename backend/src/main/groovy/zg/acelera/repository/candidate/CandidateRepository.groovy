package zg.acelera.repository.candidate

import zg.acelera.domain.Candidate
import zg.acelera.domain.User
import zg.acelera.dto.candidate.AnonymousCandidateResponseDTO
import zg.acelera.dto.job.RecruiterJobResponseDTO

interface CandidateRepository {
    User findById(UUID id)
    List<User> findAll()
    User findByCpf(String cpf)
    List<User> findBySkill(String skill)
    List<RecruiterJobResponseDTO> findAcceptedJobsByRecruiterId(UUID recruiterId)
    boolean hasAcceptedJob(UUID recruiterId, UUID jobId)
    List<AnonymousCandidateResponseDTO> findAnonymousCandidatesByJobId(UUID jobId)

    Candidate save(Candidate user)
    Candidate update(Candidate user, UUID userId)
    void delete(UUID userId)
}