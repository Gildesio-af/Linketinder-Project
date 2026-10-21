package zg.acelera.repository.recruiter

import zg.acelera.domain.Recruiter
import zg.acelera.domain.RecruiterStatus

interface RecruiterRepository {
    Recruiter findById(UUID id)
    Recruiter findByEmail(String email)
    Recruiter save(Recruiter recruiter)
    Recruiter update(Recruiter recruiter, UUID userId)
    Recruiter updateStatus(UUID userId, RecruiterStatus status)
}
