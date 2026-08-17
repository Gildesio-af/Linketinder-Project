package zg.acelera.repository

import zg.acelera.domain.Candidate
import zg.acelera.domain.IPerson
import zg.acelera.domain.Skill
import zg.acelera.dto.CandidateDTO
import zg.acelera.dto.CandidateUpdateDTO

interface ICandidateRepository {
    List<IPerson> findAll()
    IPerson findByCpf(String cpf)
    List<IPerson> findBySkill(Skill skill)
    Candidate save(CandidateDTO user)
    Candidate update(CandidateUpdateDTO user)
    void delete(String cpf)
}