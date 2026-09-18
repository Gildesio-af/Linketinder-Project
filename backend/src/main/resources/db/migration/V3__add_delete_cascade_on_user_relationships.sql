ALTER TABLE candidates DROP CONSTRAINT user_candidate_fk;
ALTER TABLE candidates
    ADD CONSTRAINT user_candidate_fk
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE companies DROP CONSTRAINT user_company_fk;
ALTER TABLE companies
    ADD CONSTRAINT user_company_fk
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE users_skill DROP CONSTRAINT IF EXISTS users_skill_user_id_fkey;
ALTER TABLE users_skill
    ADD CONSTRAINT users_skill_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


ALTER TABLE addresses DROP CONSTRAINT user_address_fk;

ALTER TABLE addresses
    ADD CONSTRAINT user_address_fk
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;