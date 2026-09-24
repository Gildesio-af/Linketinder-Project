package zg.acelera.repository.candidate

import zg.acelera.domain.Candidate
import zg.acelera.domain.Person

interface CandidateRepository {
    Person findById(UUID id)
    List<Person> findAll()
    Person findByCpf(String cpf)
    List<Person> findBySkill(String skill)

    Candidate save(Candidate user)
    Candidate update(Candidate user, UUID userId)
    void delete(UUID userId)
}