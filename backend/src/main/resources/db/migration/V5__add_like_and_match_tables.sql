CREATE TABLE companies_like(
    company_id UUID NOT NULL,
    liked_candidate_id UUID NOT NULL,
    PRIMARY KEY (company_id, liked_candidate_id),
    CONSTRAINT fk_companies_likes_company FOREIGN KEY (company_id) REFERENCES companies(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_companies_likes_candidate FOREIGN KEY (liked_candidate_id) REFERENCES candidates(user_id) ON DELETE CASCADE
);

CREATE TABLE jobs_like(
    candidate_id UUID NOT NULL,
    job_id UUID NOT NULL,
    PRIMARY KEY (candidate_id, job_id),
    CONSTRAINT fk_jobs_like_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_jobs_like_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);

CREATE TABLE matches(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    candidate_id UUID NOT NULL,
    company_id UUID NOT NULL,
    job_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_match_candidate_job UNIQUE (candidate_id, job_id),
    CONSTRAINT fk_matches_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_matches_company FOREIGN KEY (company_id) REFERENCES companies(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_matches_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);