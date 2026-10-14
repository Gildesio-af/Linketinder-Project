package zg.acelera.app

import groovy.json.JsonSlurper
import groovy.sql.Sql
import zg.acelera.repository.address.AddressRepository
import zg.acelera.repository.address.AddressRepositoryJDBC
import zg.acelera.repository.candidate.CandidateRepository
import zg.acelera.repository.candidate.CandidateRepositoryJDBC
import zg.acelera.repository.country.CountryRepository
import zg.acelera.repository.country.CountryRepositoryJDBC
import zg.acelera.repository.skill.SkillRepository
import zg.acelera.repository.skill.SkillRepositoryJDBC
import zg.acelera.service.AddressService
import zg.acelera.service.CandidateService
import zg.acelera.service.CountryService
import zg.acelera.service.SkillService
import zg.acelera.utils.config.WebServer
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
        CandidateRepository candidateRepository = new CandidateRepositoryJDBC(sql, skillRepository)
        AddressRepository addressRepository = new AddressRepositoryJDBC(sql)
        CountryRepository countryRepository = new CountryRepositoryJDBC(sql)

        CountryService countryService = new CountryService(countryRepository)
        AddressService addressService = new AddressService(addressRepository, countryService)
        SkillService skillService = new SkillService(skillRepository)
        CandidateService candidateService = new CandidateService(candidateRepository, addressService, skillService)
        WebServer webServer = new WebServer(8080)
        CandidateWebController candidateController = new CandidateWebController(candidateService, jsonSlurper)
        webServer.start(candidateController)
    }
}
