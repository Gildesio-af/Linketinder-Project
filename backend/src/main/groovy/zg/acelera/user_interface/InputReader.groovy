package zg.acelera.user_interface

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class InputReader {
    private final Scanner scanner

    InputReader() {
        this.scanner = new Scanner(System.in)
    }

    String readString(String prompt, boolean required = true) {
        while (true) {
            print prompt
            String input = scanner.nextLine().trim()

            if (required && input.isEmpty()) {
                println "Error: This field is required. Please enter a value."
            } else {
                return input.isEmpty() ? null : input
            }
        }
    }

    Integer readInt(String prompt, boolean required = true) {
        while (true) {
            String input = readString(prompt, required)
            if (input == null) return null

            try {
                return Integer.parseInt(input)
            } catch (NumberFormatException e) {
                println "Error: Invalid number format. Please enter an integer value."
            }
        }
    }

    LocalDate readDate(String prompt, boolean required = true) {
        while (true) {
            String input = readString(prompt, required)
            if (input == null) return null

            try {
                return LocalDate.parse(input, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            } catch (DateTimeParseException e) {
                println "Error: Invalid date format. Please use the format dd/MM/yyyy (e.g. 15/03/1990)."
            }
        }
    }

    Set<String> readSkills(boolean required = true) {
        String prompt = "Skills separated by commas (ex: JAVA, PYTHON): "
        String input = readString(prompt, required)

        if (input == null) return null

        return input.split(',')
                .collect { it.trim().toUpperCase() }
                .findAll { !it.isEmpty() } as Set
    }

    String readOption(String prompt, List<String> options, boolean required = true) {
        if (!options || options.isEmpty()) {
            println "No options available."
            return null
        }

        println prompt
        options.eachWithIndex { option, index ->
            println "  ${index + 1}. ${option}"
        }

        while (true) {
            Integer choice = readInt("Choose a number: ", required)
            if (choice == null) return null

            if (choice >= 1 && choice <= options.size()) {
                return options[choice - 1]
            }
            println "Error: Please choose a number between 1 and ${options.size()}."
        }
    }
}
