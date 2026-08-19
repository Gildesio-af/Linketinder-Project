package zg.acelera.user_interface

import zg.acelera.dto.company.CompanyDTO
import zg.acelera.dto.company.CompanyUpdateDTO
import zg.acelera.service.CompanyService

class CompanyUI {
    private final CompanyService companyService
    private final InputReader input

    CompanyUI(CompanyService companyService, InputReader inputReader) {
        this.companyService = companyService
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
            String corporateEmail = input.readString("Corporate E-mail: ")
            String state = input.readString("State (UF): ")
            String cep = input.readString("CEP (8 digits, without punctuation): ")
            String description = input.readString("Description/Bio: ")
            Set<String> skills = input.readSkills()

            CompanyDTO dto = new CompanyDTO(cnpj, name, corporateEmail, state, cep, description, skills)
            companyService.registerCompany(dto)

        } catch (IllegalArgumentException e) {
            println "Validation error: ${e.message}"
        }
    }

    void updateCompany() {
        println "\n=== UPDATE COMPANY ==="
        String cnpj = input.readString("Enter the CNPJ of the company you want to update: ")

        println "--- Enter the new values or press ENTER to keep the current value ---"
        try {
            String name = input.readString("New Name: ", false)
            String corporateEmail = input.readString("New Corporate E-mail: ", false)
            String state = input.readString("New State: ", false)
            String cep = input.readString("New CEP: ", false)
            String description = input.readString("New Description: ", false)

            println "Do you want to update the required skills? (Press ENTER to skip)"
            Set<String> skills = input.readSkills(false)

            CompanyUpdateDTO dto = new CompanyUpdateDTO(cnpj, name, corporateEmail, state, cep, description, skills)
            companyService.updateCompany(dto)

        } catch (IllegalArgumentException e) {
            println "Validation error: ${e.message}"
        }
    }

    void searchCompanyByCnpj() {
        println "\n=== SEARCH COMPANY ==="
        String cnpj = input.readString("Enter the CNPJ: ")
        companyService.listCompanyByCnpj(cnpj)
    }

    void searchCompaniesBySkill() {
        println "\n=== FILTER BY SKILL ==="
        String skill = input.readString("Enter the name of the skill (ex: JAVA): ")
        companyService.listCompaniesBySkill(skill)
    }

    void deleteCompany() {
        println "\n=== DELETE COMPANY ==="
        String cnpj = input.readString("Enter the CNPJ of the company you want to delete: ")
        companyService.deleteCompany(cnpj)
    }

    void listAllCompanies() {
        println "\n=== ALL COMPANIES ==="
        companyService.listAllCompanies()
    }
}
