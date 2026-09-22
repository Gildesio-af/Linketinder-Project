ALTER TABLE jobs DROP CONSTRAINT jobs_publisher;

ALTER TABLE jobs
ADD CONSTRAINT jobs_publisher
FOREIGN KEY (publisher_id) REFERENCES companies(user_id) ON DELETE CASCADE;

ALTER TABLE jobs_skill DROP CONSTRAINT IF EXISTS jobs_skill_job_id_fkey;

ALTER TABLE jobs_skill
ADD CONSTRAINT jobs_skill_job_id_fkey
FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE;