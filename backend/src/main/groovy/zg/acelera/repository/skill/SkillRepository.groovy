package zg.acelera.repository.skill

import zg.acelera.domain.Skill

interface SkillRepository {
    Skill findById(UUID id)
    Skill findByName(String name)
    Set<Skill> findAll()
    Skill save(Skill skill)
    Skill update(Skill skill, UUID id)
    void deleteById(UUID id)
}
