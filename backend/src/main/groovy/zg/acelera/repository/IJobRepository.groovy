package zg.acelera.repository

import zg.acelera.domain.Job

interface IJobRepository {
    Job findById(UUID id)
    Set<Job> findAll()
    Set<Job> findByName(String name)
    Set<Job> findBySkill(String skill)
    Set<Job> findByPublisherId(UUID publisherId)
    Job create(Job job, List<UUID> skillIds)
    Job update(Job job)
    void addSkillToJob(UUID jobId, Set<UUID> skillIds)
    void delete(UUID id)
}