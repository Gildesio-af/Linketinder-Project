package zg.acelera.dto.company

record CompanyUpdateDTO(
        String cnpj,
        String name,
        String corporateEmail,
        String state,
        String cep,
        String description,
        Set<String> skills
) {
    public CompanyUpdateDTO() {
        if (name != null && name.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid name.")
        if (corporateEmail != null && corporateEmail.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid email.")
        if (state != null && state.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid state.")
        if (cep != null && (cep.trim().isEmpty() || cep.length() != 8))
            throw new IllegalArgumentException("Please provide a valid CEP.")
        if (description != null && description.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid description.")
        if (skills != null && skills.isEmpty())
            throw new IllegalArgumentException("Please provide a valid set of skills.")
    }
}
