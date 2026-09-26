package zg.acelera.app

import groovy.sql.Sql
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
import zg.acelera.service.*
import zg.acelera.user_interface.CandidateJobUI
import zg.acelera.user_interface.CandidateUI
import zg.acelera.user_interface.CompanyJobUI
import zg.acelera.user_interface.CompanyUI
import zg.acelera.utils.reader.AddressConsoleReader
import zg.acelera.utils.reader.InputReader
import zg.acelera.user_interface.JobUI
import zg.acelera.user_interface.MatchUI
import zg.acelera.utils.db.DatabaseManager
import zg.acelera.utils.reader.SkillConsoleReader

class MainMenu {

    static void startApplication() {
        Sql sql = DatabaseManager.getSql()
        InputReader inputReader = new InputReader()

        CountryRepository countryRepository = new CountryRepositoryJDBC(sql)
        AddressRepository addressRepository = new AddressRepositoryJDBC(sql)
        SkillRepository skillRepository = new SkillRepositoryJDBC(sql)
        CandidateRepository candidateRepository = new CandidateRepositoryJDBC(sql, skillRepository)
        CompanyRepository companyRepository = new CompanyRepositoryJDBC(sql, skillRepository)
        JobRepository jobRepository = new JobRepositoryJDBC(sql)

        CountryService countryService = new CountryService(countryRepository)
        AddressService addressService = new AddressService(addressRepository, countryService)
        SkillService skillService = new SkillService(skillRepository)
        CandidateService candidateService = new CandidateService(candidateRepository, addressService, skillService)
        CompanyService companyService = new CompanyService(companyRepository, addressService, skillService)
        JobService jobService = new JobService(jobRepository, addressService, companyService, skillService)

        MatchService matchService = new MatchService()

        AddressConsoleReader addressConsoleReader = new AddressConsoleReader(inputReader, countryService)
        SkillConsoleReader skillConsoleReader = new SkillConsoleReader(inputReader, skillService)

        CandidateJobUI candidateJobUI = new CandidateJobUI(jobService, inputReader, addressConsoleReader, skillConsoleReader)
        CompanyJobUI companyJobUI = new CompanyJobUI(jobService, companyService, inputReader, addressConsoleReader, skillConsoleReader)
        CandidateUI candidateUI = new CandidateUI(candidateService, candidateJobUI, inputReader, addressConsoleReader, skillConsoleReader)
        CompanyUI companyUI = new CompanyUI(companyService, companyJobUI, inputReader, addressConsoleReader, skillConsoleReader)
        MatchUI matchUI = new MatchUI(matchService, inputReader)

        boolean running = true

        while (running) {
            println """
            ========================================
                 LINKETINDER - MAIN MENU
            ========================================
            1. Candidate Area
            2. Company Area
            3. Login (Match System)
            0. Exit System
            ========================================
            """
            String option = inputReader.readString("Choose an option: ")

            switch (option) {
                case "1":
                    candidateUI.showMenu()
                    break
                case "2":
                    companyUI.showMenu()
                    break
                case "3":
                    matchUI.showMenu()
                    break
                case "0":
                    println "Exiting the system. Goodbye!"
                    DatabaseManager.close()
                    running = false
                    break
                default:
                    println "Invalid option. Please try again."
            }
        }
    }
}