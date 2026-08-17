package zg.acelera.user_interface

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

    Set<String> readSkills(boolean required = true) {
        String prompt = "Skills separated by commas (ex: JAVA, PYTHON): "
        String input = readString(prompt, required)

        if (input == null) return null

        return input.split(',')
                .collect { it.trim().toUpperCase() }
                .findAll { !it.isEmpty() } as Set
    }
}
