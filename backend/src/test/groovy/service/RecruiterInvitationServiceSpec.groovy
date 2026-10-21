package service

import groovy.json.JsonOutput
import spock.lang.Specification
import zg.acelera.domain.Company
import zg.acelera.domain.Job
import zg.acelera.domain.Recruiter
import zg.acelera.domain.RecruiterJobInvitation
import zg.acelera.repository.job.JobRepository
import zg.acelera.repository.recruiter.RecruiterJobInvitationRepository
import zg.acelera.repository.recruiter.RecruiterRepository
import zg.acelera.service.RecruiterInvitationService

class RecruiterInvitationServiceSpec extends Specification {
    RecruiterJobInvitationRepository invitationRepository = Mock()
    JobRepository jobRepository = Mock()
    RecruiterRepository recruiterRepository = Mock()
    RecruiterInvitationService service

    UUID invitationId = UUID.randomUUID()
    UUID recruiterId = UUID.randomUUID()
    UUID companyId = UUID.randomUUID()
    UUID otherCompanyId = UUID.randomUUID()
    UUID jobId = UUID.randomUUID()

    def setup() {
        service = new RecruiterInvitationService(invitationRepository, jobRepository, recruiterRepository)
    }

    def "invites an existing recruiter only to a job owned by the authenticated company"() {
        given:
        jobRepository.findById(jobId) >> new Job(id: jobId, publisher: new Company(id: companyId))
        recruiterRepository.findById(recruiterId) >> new Recruiter(id: recruiterId)
        1 * invitationRepository.create(_) >> new RecruiterJobInvitation(
                id: invitationId, recruiterId: recruiterId, companyId: companyId,
                jobId: jobId, status: RecruiterJobInvitation.Status.PENDING)

        when:
        def result = service.invite(companyId, recruiterId, jobId)

        then:
        result.status() == RecruiterJobInvitation.Status.PENDING
        result.recruiterId() == recruiterId
        !JsonOutput.toJson(result).contains('password')
        !JsonOutput.toJson(result).contains('cpf')
    }

    def "rejects invitations from a company that does not own the job"() {
        given:
        jobRepository.findById(jobId) >> new Job(id: jobId, publisher: new Company(id: otherCompanyId))

        when:
        service.invite(companyId, recruiterId, jobId)

        then:
        thrown(SecurityException)
        0 * recruiterRepository._
        0 * invitationRepository._
    }

    def "allows only the invited recruiter to accept"() {
        given:
        invitationRepository.findById(invitationId) >> new RecruiterJobInvitation(
                id: invitationId, recruiterId: recruiterId, companyId: companyId,
                jobId: jobId, status: RecruiterJobInvitation.Status.PENDING)
        1 * invitationRepository.accept(invitationId, recruiterId) >> new RecruiterJobInvitation(
                id: invitationId, recruiterId: recruiterId, companyId: companyId,
                jobId: jobId, status: RecruiterJobInvitation.Status.ACCEPTED)

        when:
        def result = service.accept(recruiterId, invitationId)

        then:
        result.status() == RecruiterJobInvitation.Status.ACCEPTED
    }

    def "lists only the authenticated recruiter's invitation projection"() {
        given:
        invitationRepository.findByRecruiterId(recruiterId) >> [
                invitation(RecruiterJobInvitation.Status.PENDING),
                invitation(RecruiterJobInvitation.Status.REVOKED)
        ]

        when:
        def result = service.listForRecruiter(recruiterId)

        then:
        result*.status() == [RecruiterJobInvitation.Status.PENDING, RecruiterJobInvitation.Status.REVOKED]
        result*.toString().every { !it.toLowerCase().contains('password') }
    }

    def "requires authentication for administrative actions"() {
        when:
        service.cancel(null, invitationId)

        then:
        thrown(SecurityException)
        0 * invitationRepository._
    }

    def "rejects a transition by a different user before changing persistence"() {
        given:
        invitationRepository.findById(invitationId) >> new RecruiterJobInvitation(
                id: invitationId, recruiterId: recruiterId, companyId: companyId,
                jobId: jobId, status: RecruiterJobInvitation.Status.PENDING)

        when:
        service.accept(UUID.randomUUID(), invitationId)

        then:
        thrown(SecurityException)
        0 * invitationRepository.accept(_, _)
    }

    private RecruiterJobInvitation invitation(RecruiterJobInvitation.Status status) {
        new RecruiterJobInvitation(
                id: invitationId, recruiterId: recruiterId, companyId: companyId,
                jobId: jobId, status: status)
    }
}
