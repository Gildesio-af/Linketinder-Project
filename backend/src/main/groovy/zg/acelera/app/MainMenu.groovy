package zg.acelera.app

import groovy.sql.Sql
import zg.acelera.repository.*
import zg.acelera.service.*
import zg.acelera.user_interface.CandidateUI
import zg.acelera.user_interface.CompanyUI
import zg.acelera.user_interface.InputReader
import zg.acelera.user_interface.JobUI
import zg.acelera.user_interface.MatchUI
import zg.acelera.utils.db.DatabaseManager

class MainMenu {

    static void startApplication() {
        Sql sql = DatabaseManager.getSql()
        InputReader inputReader = new InputReader()

        ICountryRepository countryRepository = new CountryRepositoryJDBC(sql)
        IAddressRepository addressRepository = new AddressRepositoryJDBC(sql)
        ISkillRepository skillRepository = new SkillRepositoryJDBC(sql)
        ICandidateRepository candidateRepository = new CandidateRepositoryJDBC(sql)
        ICompanyRepository companyRepository = new CompanyRepositoryJDBC(sql)
        IJobRepository jobRepository = new JobRepositoryJDBC(sql)

        CountryService countryService = new CountryService(countryRepository)
        AddressService addressService = new AddressService(addressRepository, countryService)
        SkillService skillService = new SkillService(skillRepository)
        CandidateService candidateService = new CandidateService(candidateRepository, addressService, skillService)
        CompanyService companyService = new CompanyService(companyRepository, addressService, skillService)
        JobService jobService = new JobService(jobRepository, addressService, companyService, skillService)

        MatchService matchService = new MatchService()

        JobUI jobUI = new JobUI(jobService, companyService, skillService, countryService, inputReader)
        CandidateUI candidateUI = new CandidateUI(candidateService, skillService, countryService, jobUI, inputReader)
        CompanyUI companyUI = new CompanyUI(companyService, skillService, countryService, jobUI, inputReader)
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