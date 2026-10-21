CREATE TABLE recruiter_job_invitations(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recruiter_id UUID NOT NULL,
    company_id UUID NOT NULL,
    job_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP,
    revoked_at TIMESTAMP,
    CONSTRAINT recruiter_job_invitations_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'CANCELLED', 'REVOKED')),
    CONSTRAINT recruiter_job_invitations_recruiter_fk
        FOREIGN KEY (recruiter_id) REFERENCES recruiters(user_id) ON DELETE CASCADE,
    CONSTRAINT recruiter_job_invitations_company_fk
        FOREIGN KEY (company_id) REFERENCES companies(user_id) ON DELETE CASCADE,
    CONSTRAINT recruiter_job_invitations_job_fk
        FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);

CREATE INDEX recruiter_job_invitations_recruiter_status_idx
    ON recruiter_job_invitations (recruiter_id, status);

CREATE INDEX recruiter_job_invitations_job_status_idx
    ON recruiter_job_invitations (job_id, status);

CREATE UNIQUE INDEX recruiter_job_invitations_active_unique_idx
    ON recruiter_job_invitations (recruiter_id, job_id)
    WHERE status IN ('PENDING', 'ACCEPTED');

CREATE TABLE recruiter_job_likes(
    recruiter_id UUID NOT NULL,
    job_id UUID NOT NULL,
    candidate_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    removed_at TIMESTAMP,
    PRIMARY KEY (recruiter_id, job_id, candidate_id),
    CONSTRAINT recruiter_job_likes_recruiter_fk
        FOREIGN KEY (recruiter_id) REFERENCES recruiters(user_id) ON DELETE CASCADE,
    CONSTRAINT recruiter_job_likes_job_fk
        FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT recruiter_job_likes_candidate_fk
        FOREIGN KEY (candidate_id) REFERENCES candidates(user_id) ON DELETE CASCADE
);

CREATE INDEX recruiter_job_likes_job_candidate_idx
    ON recruiter_job_likes (job_id, candidate_id);

CREATE INDEX recruiter_job_likes_candidate_job_idx
    ON recruiter_job_likes (candidate_id, job_id);
