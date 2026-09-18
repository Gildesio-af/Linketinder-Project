package zg.acelera.repository

import zg.acelera.domain.Candidate
import zg.acelera.domain.IPerson
import zg.acelera.domain.Skill
import zg.acelera.domain.SkillEnum
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO

interface ICandidateRepository {
    IPerson findById(UUID id)
    List<IPerson> findAll()
    IPerson findByCpf(String cpf)
    List<IPerson> findBySkill(String skill)

    Candidate save(Candidate user)
    Candidate update(Candidate user, UUID userId)
    void delete(UUID userId)
}