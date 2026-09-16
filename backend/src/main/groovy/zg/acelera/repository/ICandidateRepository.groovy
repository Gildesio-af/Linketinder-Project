package zg.acelera.repository

import zg.acelera.domain.Candidate
import zg.acelera.domain.IPerson
import zg.acelera.domain.SkillEnum
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO

interface ICandidateRepository {
    List<IPerson> findAll()
    IPerson findByCpf(String cpf)
    List<IPerson> findBySkill(SkillEnum skill)
    Candidate save(CandidateDTO user)
    Candidate update(CandidateUpdateDTO user)
    void update(Candidate candidate)
    void delete(String cpf)
}