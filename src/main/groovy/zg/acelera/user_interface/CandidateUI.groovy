package zg.acelera.user_interface

import zg.acelera.dto.CandidateDTO
import zg.acelera.dto.CandidateUpdateDTO
import zg.acelera.service.CandidateService

class CandidateUI {
    private final CandidateService candidateService
    private final InputReader input

    CandidateUI(CandidateService candidateService, InputReader inputReader) {
        this.candidateService = candidateService
        this.input = inputReader
    }

    void showMenu() {
        boolean running = true

        while (running) {
            println """
            ========================================
                   LINKETINDER - CANDIDATE MENU
            ========================================
            1. Show all candidates
            2. Search candidate by CPF
            3. Search candidates by Skill
            4. Register candidate
            5. Update candidate
            6. Delete candidate
            0. Go back to Main Menu
            ========================================
            """
            String option = input.readString("Choose an option: ")

            switch (option) {
                case "1": listAllCandidates(); break
                case "2": searchCandidateByCpf(); break
                case "3": searchCandidatesBySkill(); break
                case "4": registerCandidate(); break
                case "5": updateCandidate(); break
                case "6": deleteCandidate(); break
                case "0":
                    println "Going back to the Main Menu..."
                    running = false
                    break
                default:
                    println "Invalid option. Please try again."
            }
        }
    }

    void registerCandidate() {
        println "\n=== REGISTER CANDIDATE ==="
        try {
            String cpf = input.readString("CPF (11 digits, without punctuation): ")
            String name = input.readString("Name: ")
            String email = input.readString("E-mail: ")
            Integer age = input.readInt("Age: ")
            String state = input.readString("State (UF): ")
            String cep = input.readString("CEP (8 digits, without punctuation): ")
            String description = input.readString("Description/Bio: ")
            Set<String> skills = input.readSkills()

            CandidateDTO dto = new CandidateDTO(cpf, name, email, age, state, cep, description, skills)
            candidateService.registerCandidate(dto)

        } catch (IllegalArgumentException e) {
            println "Validation error: ${e.message}"
        }
    }

    void updateCandidate() {
        println "\n=== UPDATE CANDIDATE ==="
        String cpf = input.readString("Enter the CPF of the candidate you want to update: ")

        println "--- Enter the new values or press ENTER to keep the current value ---"
        try {
            String name = input.readString("New Name: ", false)
            String email = input.readString("New E-mail: ", false)
            Integer age = input.readInt("New Age: ", false)
            String state = input.readString("New State: ", false)
            String cep = input.readString("New CEP: ", false)
            String description = input.readString("New Description: ", false)

            println "Do you want to update the skills? (Press ENTER to skip)"
            Set<String> skills = input.readSkills(false)

            CandidateUpdateDTO dto = new CandidateUpdateDTO(cpf, name, email, age, state, cep, description, skills)
            candidateService.updateCandidate(dto)

        } catch (IllegalArgumentException e) {
            println "Validation error: ${e.message}"
        }
    }

    void searchCandidateByCpf() {
        println "\n=== SEARCH CANDIDATE ==="
        String cpf = input.readString("Enter the CPF: ")
        candidateService.listCandidateByCpf(cpf)
    }

    void searchCandidatesBySkill() {
        println "\n=== FILTER BY SKILL ==="
        String skill = input.readString("Enter the name of the skill (JAVA): ")
        candidateService.listCandidatesBySkill(skill)
    }

    void deleteCandidate() {
        println "\n=== DELETE CANDIDATE ==="
        String cpf = input.readString("Enter the CPF of the candidate you want to delete: ")
        candidateService.deleteCandidate(cpf)
    }

    void listAllCandidates() {
        println "\n=== ALL CANDIDATES ==="
        candidateService.listAllCandidates()
    }
}
