package zg.acelera.app

import zg.acelera.service.CandidateService
import zg.acelera.user_interface.CandidateUI
import zg.acelera.user_interface.InputReader

class MainMenu {
    static InputReader inputReader = new InputReader()
    static CandidateService candidateService = new CandidateService()
    static CandidateUI candidateUI = new CandidateUI(candidateService, inputReader)

    static void startApplication() {
        boolean running = true

        while (running) {
            println """
            ========================================
                 LINKETINDER - MAIN MENU
            ========================================
            1. Candidate Area
            2. Company Area (TODO)
            0. Exit System
            ========================================
            """
            String option = inputReader.readString("Choose an option: ")

            switch (option) {
                case "1":
                    candidateUI.showMenu()
                    break
                case "2":
                    println("As soon as possible, the company area will be available. Stay tuned!")
                    break
                case "0":
                    println "Exiting the system. Goodbye!"
                    running = false
                    break
                default:
                    println "Invalid option. Please try again."
            }
        }
    }
}
