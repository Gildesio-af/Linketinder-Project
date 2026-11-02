package zg.acelera.dto.auth

record AuthResponseDTO(
        UUID id,
        String role
) {
    static final String CANDIDATE_ROLE = "CANDIDATE"
    static final String COMPANY_ROLE = "COMPANY"
    static final String RECRUITER_ROLE = "RECRUITER"
}
