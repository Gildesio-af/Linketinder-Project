package zg.acelera.domain

enum SkillEnum {
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

    SkillEnum(String name) {
        this.name = name
    }
}