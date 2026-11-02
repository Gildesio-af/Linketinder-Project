-- Legacy compatibility: `companies_like` remains untouched and continues to store the
-- legacy company-to-candidate relationship. Recruiter-specific authorization is tracked
-- in `recruiter_job_likes`, which uses a soft-delete (`removed_at`) to preserve audit
-- history while keeping the logical record idempotent via its composite primary key.
-- Match creation/removal remains derived from the active recruiter likes and the existing
-- unique `(candidate_id, job_id)` constraint in `matches`.

CREATE OR REPLACE FUNCTION recruiter_job_invitations_validate_company_job()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.company_id IS DISTINCT FROM (SELECT publisher_id FROM jobs WHERE id = NEW.job_id) THEN
        RAISE EXCEPTION 'company_id must match the publisher_id of the selected job';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

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

CREATE TRIGGER recruiter_job_invitations_company_matches_job
BEFORE INSERT OR UPDATE OF company_id, job_id
ON recruiter_job_invitations
FOR EACH ROW
EXECUTE FUNCTION recruiter_job_invitations_validate_company_job();

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
        FOREIGN KEY (candidate_id) REFERENCES candidates(user_id) ON DELETE CASCADE,
    CONSTRAINT recruiter_job_likes_unique_active_row
        CHECK (removed_at IS NULL OR removed_at >= created_at)
);

CREATE INDEX recruiter_job_likes_job_candidate_idx
    ON recruiter_job_likes (job_id, candidate_id);

CREATE INDEX recruiter_job_likes_candidate_job_idx
    ON recruiter_job_likes (candidate_id, job_id);

CREATE INDEX recruiter_job_likes_active_idx
    ON recruiter_job_likes (recruiter_id, job_id)
    WHERE removed_at IS NULL;
