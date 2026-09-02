package zg.acelera.user_interface

import zg.acelera.domain.Candidate
import zg.acelera.domain.Company
import zg.acelera.domain.IPerson
import zg.acelera.service.MatchService

class MatchUI {
    private final MatchService matchService
    private final InputReader input

    MatchUI(MatchService matchService, InputReader inputReader) {
        this.matchService = matchService
        this.input = inputReader
    }

    void showMenu() {
        println "\n=== LINKETINDER - MATCH SYSTEM ==="
        String id = input.readString("Enter your CPF or CNPJ to login: ")

        try {
            matchService.login(id)
            println "\nLogin successful!"

            matchLoop()
        } catch (Exception e) {
            println "Error: ${e.message}"
        }

    }

    private void matchLoop() {
        boolean running = true

        while (running) {
            println """
            ========================================
                     LINKETINDER - MATCH FEED
            ========================================
            1. View potential profiles
            2. Like profiles
            3. Show my matches
            0. Go back to Main Menu
            ========================================
            """
            String option = input.readString("Choose an option: ")

            switch (option) {
                case "1":
                    viewPotentials()
                    break
                case "2":
                    likeProfiles()
                    break
                case "3":
                    matchService.showMatches()
                    break
                case "0":
                    println "Logging out..."
                    running = false
                    break
            }
        }
    }

    private void viewPotentials() {
        println "\n--- POTENTIAL MATCHES ---"
        List<IPerson> potentials = matchService.getPotentialMatches()

        if (potentials.isEmpty()) {
            println "No profiles available for you at the moment."
            return
        }

        potentials.each { person ->
            String id = person instanceof Candidate ? ((Candidate) person).cpf : ((Company) person).cnpj
            println "[ID: ${id}] - ${person.name} | Skills: ${person.skills.join(', ')} | State: ${person.state} | Description: ${person.description}"
        }
    }

    private void likeProfiles() {
        println "\n--- LIKE PROFILES ---"
        String idsInput = input.readString("Enter the IDs (CPF/CNPJ) you want to like, separated by commas: ")

        Set<String> targetsId = idsInput.split(',')
                .collect { it.trim() }
                .findAll { !it.isEmpty() } as Set<String>

        if (targetsId.isEmpty()) {
            println "No valid IDs provided. Action canceled."
            return
        }


            matchService.likeProfile(targetsId)
    }
}
