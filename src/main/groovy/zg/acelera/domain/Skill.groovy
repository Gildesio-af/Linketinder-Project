package zg.acelera.domain

enum Skill {
    JAVA("Java"),
    PYTHON("Python"),
    JAVASCRIPT("JavaScript"),
    CSHARP("C#"),
    RUBY("Ruby"),
    PHP("PHP"),
    SWIFT("Swift"),
    KOTLIN("Kotlin"),
    GO("Go"),
    RUST("Rust")

    final String name

    Skill(String name) {
        this.name = name
    }
}