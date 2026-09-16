package zg.acelera.dto.job
 record JobDTO (
        UUID id = null,
        String title,
        String description,
        List<String> skills
 ) {
     JobDTO() {
        if (!title || title().trim().isEmpty())
            throw new IllegalArgumentException("Job title must be provided and cannot be empty")
        if (!description || description().trim().isEmpty())
            throw new IllegalArgumentException("Job description must be provided and cannot be empty")
        if (skills == null || skills.isEmpty())
            throw new IllegalArgumentException("Job skills must be provided and cannot be empty")
    }
}
