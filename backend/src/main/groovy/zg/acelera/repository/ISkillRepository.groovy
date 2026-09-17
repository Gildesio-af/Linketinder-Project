package zg.acelera.repository

import zg.acelera.domain.Skill

interface ISkillRepository {
    Skill findById(UUID id)
    Skill save(Skill skill)
    Skill update(Skill skill, UUID id)
    void deleteById(UUID id)
}
