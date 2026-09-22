package zg.acelera.user_interface

import zg.acelera.dto.address.AddressDTO
import zg.acelera.dto.company.CompanyDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.company.CompanyUpdateDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.service.CompanyService
import zg.acelera.service.CountryService
import zg.acelera.service.SkillService
import zg.acelera.utils.DataManager

class CompanyUI {
    private final CompanyService companyService
    private final SkillService skillService
    private final CountryService countryService
    private final JobUI jobUI
    private final InputReader input

    CompanyUI(CompanyService companyService, SkillService skillService, CountryService countryService, JobUI jobUI, InputReader inputReader) {
        this.companyService = companyService
        this.skillService = skillService
        this.countryService = countryService
        this.jobUI = jobUI
        this.input = inputReader
    }

    void showMenu() {
        boolean running = true

        while (running) {
            println """
            ========================================
                   LINKETINDER - COMPANY MENU
            ========================================
            1. Show all companies
            2. Search company by CNPJ
            3. Search companies by Skill
            4. Register company
            5. Update company
            6. Delete company
            7. Manage Jobs (requires login)
            0. Go back to Main Menu
            ========================================
            """
            String option = input.readString("Choose an option: ")

            switch (option) {
                case "1": listAllCompanies(); break
                case "2": searchCompanyByCnpj(); break
                case "3": searchCompaniesBySkill(); break
                case "4": registerCompany(); break
                case "5": updateCompany(); break
                case "6": deleteCompany(); break
                case "7": jobUI.manageJobs(); break
                case "0":
                    println "Going back to the Main Menu..."
                    running = false
                    break
                default:
                    println "Invalid option. Please try again."
            }
        }
    }

    void registerCompany() {
        println "\n=== REGISTER COMPANY ==="
        try {
            String cnpj = input.readString("CNPJ (14 digits, without punctuation): ")
            String name = input.readString("Name: ")
            String email = input.readString("Corporate E-mail: ")
            String password = input.readString("Password (min 6 characters): ")
            String description = input.readString("Description/Bio: ")

            Set<String> skillNames = DataManager.readSkillsFromDatabase()
            if (!skillNames || skillNames.isEmpty()) {
                println "Error: No skills selected. Registration canceled."
                return
            }

            Set<String> skillIds = companyService.resolveSkillNamesToIds(skillNames)

            CompanyDTO dto = new CompanyDTO(cnpj, name, email, password, description, skillIds)

            println "\n--- ADDRESS DATA ---"
            AddressDTO addressDTO = DataManager.readAddressData()

            CompanyResponseDTO result = companyService.registerCompany(dto, addressDTO)
            if (result) {
                println "\nCompany registered successfully!"
                printCompanyResponse(result)
            }

        } catch (IllegalArgumentException e) {
            println "Validation error: ${e.message}"
        } catch (Exception e) {
            println "Error: ${e.message}"
        }
    }

    void updateCompany() {
        println "\n=== UPDATE COMPANY ==="
        String cnpj = input.readString("Enter the CNPJ of the company you want to update: ")

        println "--- Enter the new values or press ENTER to keep the current value ---"
        try {
            String name = input.readString("New Name: ", false)
            String email = input.readString("New Corporate E-mail: ", false)
            String password = input.readString("New Password: ", false)
            String description = input.readString("New Description: ", false)

            CompanyUpdateDTO dto = new CompanyUpdateDTO(cnpj, name, email, password, description)
            CompanyResponseDTO result = companyService.updateCompany(dto)
            if (result) {
                println "\nCompany updated successfully!"
                printCompanyResponse(result)
            }

        } catch (IllegalArgumentException e) {
            println "Validation error: ${e.message}"
        }
    }

    void searchCompanyByCnpj() {
        println "\n=== SEARCH COMPANY ==="
        String cnpj = input.readString("Enter the CNPJ: ")
        CompanyResponseDTO result = companyService.listCompanyByCnpj(cnpj)
        if (result) {
            printCompanyResponse(result)
        } else {
            println "No company found with CNPJ: ${cnpj}"
        }
    }

    void searchCompaniesBySkill() {
        println "\n=== FILTER BY SKILL ==="
        String skill = input.readString("Enter the name of the skill (e.g. JAVA): ")
        List<CompanyResponseDTO> results = companyService.listCompaniesBySkill(skill)
        if (results && !results.isEmpty()) {
            results.each { printCompanyResponse(it) }
        } else {
            println "No companies found with skill: ${skill}"
        }
    }

    void deleteCompany() {
        println "\n=== DELETE COMPANY ==="
        String cnpj = input.readString("Enter the CNPJ of the company you want to delete: ")
        companyService.deleteCompany(cnpj)
        println "Company deleted successfully (if found)."
    }

    void listAllCompanies() {
        println "\n=== ALL COMPANIES ==="
        List<CompanyResponseDTO> companies = companyService.listAllCompanies()
        if (companies && !companies.isEmpty()) {
            companies.each { printCompanyResponse(it) }
        } else {
            println "No companies registered."
        }
    }

    private static void printCompanyResponse(CompanyResponseDTO company) {
        println "------------------------------"
        println "  ID: ${company.id()}"
        println "  Name: ${company.name()}"
        println "  CNPJ: ${company.cnpj()}"
        println "  E-mail: ${company.email()}"
        println "  Description: ${company.description()}"
        if (company.skills() && !company.skills().isEmpty()) {
            String skillNames = company.skills().collect { it.name() }.join(', ')
            println "  Desired Skills: ${skillNames}"
        } else {
            println "  Desired Skills: (none)"
        }
        if (company.addresses()) {
            company.addresses().each { addr ->
                println "  Address: ${addr.street()}, ${addr.number()} - ${addr.neighborhood()}, ${addr.city()} - ${addr.state()}, ${addr.cep()}"
                if (addr.country()) {
                    println "  Country: ${addr.country().name()} (${addr.country().code()})"
                }
            }
        }
        println "------------------------------"
    }
}
