package zg.acelera.repository.candidate

import zg.acelera.domain.Candidate
import zg.acelera.domain.User

interface CandidateRepository {
    User findById(UUID id)
    List<User> findAll()
    User findByCpf(String cpf)
    List<User> findBySkill(String skill)

    Candidate save(Candidate user)
    Candidate update(Candidate user, UUID userId)
    void delete(UUID userId)
}