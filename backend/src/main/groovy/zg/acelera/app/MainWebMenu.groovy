package zg.acelera.app

import groovy.json.JsonSlurper
import groovy.sql.Sql
import zg.acelera.controller.CompanyWebController
import zg.acelera.controller.JobWebController
import zg.acelera.controller.AuthController
import zg.acelera.controller.RecruiterWebController
import zg.acelera.controller.RecruiterInvitationWebController
import zg.acelera.controller.RecruiterLikeWebController
import zg.acelera.repository.address.AddressRepository
import zg.acelera.repository.address.AddressRepositoryJDBC
import zg.acelera.repository.candidate.CandidateRepository
import zg.acelera.repository.candidate.CandidateRepositoryJDBC
import zg.acelera.repository.company.CompanyRepository
import zg.acelera.repository.company.CompanyRepositoryJDBC
import zg.acelera.repository.country.CountryRepository
import zg.acelera.repository.country.CountryRepositoryJDBC
import zg.acelera.repository.job.JobRepository
import zg.acelera.repository.job.JobRepositoryJDBC
import zg.acelera.repository.skill.SkillRepository
import zg.acelera.repository.skill.SkillRepositoryJDBC
import zg.acelera.repository.match.MatchRepository
import zg.acelera.repository.match.MatchRepositoryJDBC
import zg.acelera.repository.like.LikeRepository
import zg.acelera.repository.like.JobLikeRepositoryJDBC
import zg.acelera.repository.like.CompanyLikeRepositoryJDBC
import zg.acelera.repository.recruiter.RecruiterRepository
import zg.acelera.repository.recruiter.RecruiterRepositoryJDBC
import zg.acelera.repository.recruiter.RecruitmentAreaRepository
import zg.acelera.repository.recruiter.RecruitmentAreaRepositoryJDBC
import zg.acelera.repository.recruiter.RecruiterJobInvitationRepository
import zg.acelera.repository.recruiter.RecruiterJobInvitationRepositoryJDBC
import zg.acelera.repository.recruiter.RecruiterJobLikeRepository
import zg.acelera.repository.recruiter.RecruiterJobLikeRepositoryJDBC
import zg.acelera.service.AddressService
import zg.acelera.service.CandidateService
import zg.acelera.service.CompanyService
import zg.acelera.service.CountryService
import zg.acelera.service.JobService
import zg.acelera.service.SkillService
import zg.acelera.service.AuthService
import zg.acelera.service.RecruiterService
import zg.acelera.service.RecruiterInvitationService
import zg.acelera.service.RecruiterJobLikeService
import zg.acelera.service.MatchService
import zg.acelera.service.JobLikeService
import zg.acelera.service.CompanyLikeService
import zg.acelera.utils.config.WebServerConfig
import zg.acelera.controller.CandidateWebController
import zg.acelera.utils.db.DatabaseConfig
import zg.acelera.utils.db.DatabaseConnectionFactory
import zg.acelera.utils.db.DatabaseFactory

class MainWebMenu {
    static void starWebServer() {
        DatabaseConfig config = DatabaseConfig.fromEnvironment()
        DatabaseConnectionFactory factory = new DatabaseFactory(config)
        Sql sql = factory.createSql()
        JsonSlurper jsonSlurper = new JsonSlurper()

        SkillRepository skillRepository = new SkillRepositoryJDBC(sql)
        AddressRepository addressRepository = new AddressRepositoryJDBC(sql)
        CountryRepository countryRepository = new CountryRepositoryJDBC(sql)
        CandidateRepository candidateRepository = new CandidateRepositoryJDBC(sql, skillRepository)
        CompanyRepository companyRepository = new CompanyRepositoryJDBC(sql, skillRepository)
        JobRepository jobRepository = new JobRepositoryJDBC(sql)

        CountryService countryService = new CountryService(countryRepository)
        AddressService addressService = new AddressService(addressRepository, countryService)
        SkillService skillService = new SkillService(skillRepository)
        CandidateService candidateService = new CandidateService(candidateRepository, addressService, skillService)
        CompanyService companyService = new CompanyService(companyRepository, addressService, skillService)
        JobService jobService = new JobService(jobRepository, addressService, companyService, skillService)

        RecruiterRepository recruiterRepository = new RecruiterRepositoryJDBC(sql)
        RecruitmentAreaRepository areaRepository = new RecruitmentAreaRepositoryJDBC(sql)
        RecruiterJobInvitationRepository invitationRepository = new RecruiterJobInvitationRepositoryJDBC(sql)
        RecruiterJobLikeRepository recruiterLikeRepository = new RecruiterJobLikeRepositoryJDBC(sql)
        LikeRepository jobLikeRepository = new JobLikeRepositoryJDBC(sql)
        LikeRepository companyLikeRepository = new CompanyLikeRepositoryJDBC(sql)
        MatchRepository matchRepository = new MatchRepositoryJDBC(sql)
        MatchService matchService = new MatchService(matchRepository, candidateRepository, companyRepository,
                recruiterRepository, recruiterLikeRepository)
        RecruiterService recruiterService = new RecruiterService(recruiterRepository, areaRepository)
        RecruiterInvitationService invitationService = new RecruiterInvitationService(
                invitationRepository, jobRepository, recruiterRepository)
        RecruiterJobLikeService recruiterJobLikeService = new RecruiterJobLikeService(
                recruiterLikeRepository, jobLikeRepository, matchService)
        AuthService authService = new AuthService(candidateRepository, companyRepository, recruiterRepository)

        CandidateWebController candidateController = new CandidateWebController(candidateService, jsonSlurper)
        CompanyWebController companyController = new CompanyWebController(companyService, jsonSlurper)
        JobWebController jobController = new JobWebController(jobService, jsonSlurper)
        AuthController authController = new AuthController(authService, jsonSlurper)
        RecruiterWebController recruiterController = new RecruiterWebController(recruiterService, candidateService, jsonSlurper)
        RecruiterInvitationWebController invitationController = new RecruiterInvitationWebController(invitationService, jsonSlurper)
        RecruiterLikeWebController likeController = new RecruiterLikeWebController(
                recruiterJobLikeService, matchService, jsonSlurper)

        WebServerConfig webServer = new WebServerConfig(8080)
        webServer.start(candidateController, companyController, jobController, authController,
                recruiterController, invitationController, likeController)
    }
}
