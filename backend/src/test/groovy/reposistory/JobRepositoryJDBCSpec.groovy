package reposistory

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Specification
import zg.acelera.domain.Address
import zg.acelera.domain.Company
import zg.acelera.domain.Job
import zg.acelera.domain.Skill
import zg.acelera.repository.job.JobRepositoryJDBC
import zg.acelera.utils.exception.EntityNotFoundException

class JobRepositoryJDBCSpec extends Specification {

    Sql sqlMock
    JobRepositoryJDBC repository

    UUID jobId = UUID.randomUUID()
    UUID addressId = UUID.randomUUID()
    UUID publisherId = UUID.randomUUID()
    UUID skillId = UUID.randomUUID()

    def setup() {
        sqlMock = Mock(Sql)
        repository = new JobRepositoryJDBC(sqlMock)
    }

    private GroovyRowResult createJobRow(UUID id = jobId) {
        return [
                id          : id,
                name        : "Java Developer",
                description : "Develop Java apps",
                address_id  : addressId,
                publisher_id: publisherId,
                skill_id    : skillId,
                skill_name  : "Java"
        ] as GroovyRowResult
    }

    def "findById should return a job when found"() {
        given:
        sqlMock.rows(_, [jobId]) >> [createJobRow()]

        when:
        Job result = repository.findById(jobId)

        then:
        result.id == jobId
        result.name == "Java Developer"
        result.description == "Develop Java apps"
        result.desiredSkills.size() == 1
        result.desiredSkills.first().name == "Java"
    }

    def "findById should throw EntityNotFoundException when job not found"() {
        given:
        sqlMock.rows(_, [jobId]) >> []

        when:
        repository.findById(jobId)

        then:
        thrown(EntityNotFoundException)
    }

    def "findAll should return a set of jobs"() {
        given:
        sqlMock.rows(_) >> [createJobRow()]

        when:
        Set<Job> result = repository.findAll()

        then:
        result.size() == 1
    }

    def "findAll should return empty set when no jobs exist"() {
        given:
        sqlMock.rows(_) >> []

        when:
        Set<Job> result = repository.findAll()

        then:
        result.isEmpty()
    }

    def "findByName should return jobs matching the name"() {
        given:
        sqlMock.rows(_, ["%Java%"]) >> [createJobRow()]

        when:
        Set<Job> result = repository.findByName("Java")

        then:
        result.size() == 1
        result.first().name == "Java Developer"
    }

    def "findByName should return empty set when no match"() {
        given:
        sqlMock.rows(_, ["%Rust%"]) >> []

        when:
        Set<Job> result = repository.findByName("Rust")

        then:
        result.isEmpty()
    }

    def "findBySkill should return jobs with the given skill"() {
        given:
        sqlMock.rows(_, ["Java"]) >> [createJobRow()]

        when:
        Set<Job> result = repository.findBySkill("Java")

        then:
        result.size() == 1
    }

    def "findByPublisherId should return jobs for the given publisher"() {
        given:
        sqlMock.rows(_, [publisherId]) >> [createJobRow()]

        when:
        Set<Job> result = repository.findByPublisherId(publisherId)

        then:
        result.size() == 1
    }

    def "findByPublisherId should return empty set when no jobs for publisher"() {
        given:
        sqlMock.rows(_, [publisherId]) >> []

        when:
        Set<Job> result = repository.findByPublisherId(publisherId)

        then:
        result.isEmpty()
    }

    def "create should insert job and return it with skills"() {
        given:
        Job job = new Job(
                name: "Java Developer", description: "Develop Java apps",
                address: new Address(id: addressId),
                publisher: new Company(id: publisherId)
        )
        List<UUID> skillIds = [skillId]

        GroovyRowResult insertedRow = [id: jobId, name: "Java Developer", description: "Develop Java apps",
                                       address_id: addressId, publisher_id: publisherId] as GroovyRowResult

        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }
        sqlMock.firstRow(_, _) >> insertedRow
        sqlMock.rows(_, [jobId]) >> [createJobRow()]

        when:
        Job result = repository.create(job, skillIds)

        then:
        result.name == "Java Developer"
        result.desiredSkills.size() == 1
    }

    def "delete should delete job and its address within transaction"() {
        given:
        GroovyRowResult deleteRow = [address_id: addressId] as GroovyRowResult
        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }
        sqlMock.firstRow(_, [jobId]) >> deleteRow

        when:
        repository.delete(jobId)

        then:
        1 * sqlMock.executeUpdate(_, [addressId])
    }

    def "delete should not delete address when job has no address"() {
        given:
        GroovyRowResult deleteRow = [address_id: null] as GroovyRowResult
        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }
        sqlMock.firstRow(_, [jobId]) >> deleteRow

        when:
        repository.delete(jobId)

        then:
        0 * sqlMock.executeUpdate(_, _)
    }

    def "update should update job and return it with skills"() {
        given:
        Job jobToUpdate = new Job(id: jobId, name: "Updated Job", description: "Updated desc")

        GroovyRowResult updatedRow = [id: jobId, name: "Updated Job", description: "Updated desc",
                                      address_id: addressId, publisher_id: publisherId] as GroovyRowResult

        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }
        sqlMock.firstRow({ it.contains("UPDATE jobs") }, _) >> updatedRow
        sqlMock.rows(_, [jobId]) >> [
                [id: jobId, name: "Updated Job", description: "Updated desc",
                 address_id: addressId, publisher_id: publisherId,
                 skill_id: skillId, skill_name: "Java"] as GroovyRowResult
        ]

        when:
        Job result = repository.update(jobToUpdate)

        then:
        result.name == "Updated Job"
        result.description == "Updated desc"
    }

    def "addSkillToJob should insert skill associations for the given job"() {
        given:
        UUID skill1 = UUID.randomUUID()
        UUID skill2 = UUID.randomUUID()
        Set<UUID> skillIds = [skill1, skill2] as Set

        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }

        when:
        repository.addSkillToJob(jobId, skillIds)

        then:
        2 * sqlMock.executeUpdate(_, _)
    }
}
