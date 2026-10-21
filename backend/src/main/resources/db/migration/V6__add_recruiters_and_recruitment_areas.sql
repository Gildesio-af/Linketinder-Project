CREATE TABLE recruitment_areas(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO recruitment_areas (name)
VALUES
    ('TI'),
    ('RH'),
    ('Marketing');

CREATE TABLE recruiters(
    user_id UUID PRIMARY KEY,
    recruitment_area_id UUID NOT NULL,
    cpf CHAR(11) NOT NULL UNIQUE,
    birth_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT recruiters_cpf_format
        CHECK (cpf ~ '^[0-9]{11}$'),
    CONSTRAINT recruiters_status
        CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT recruiters_user_fk
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT recruiters_recruitment_area_fk
        FOREIGN KEY (recruitment_area_id) REFERENCES recruitment_areas(id)
);
