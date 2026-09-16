CREATE TABLE countries(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(75) NOT NULL,
    code CHAR(2) UNIQUE NOT NULL
);

CREATE TABLE skills(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE addresses(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    CEP VARCHAR(20) NOT NULL,
    street VARCHAR(100) NOT NULL,
    number VARCHAR(10) NOT NULL,
    complement VARCHAR(100),
    neighborhood VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country_id UUID,
    CONSTRAINT country_address_fk FOREIGN KEY (country_id) REFERENCES countries(id)
);

CREATE TABLE users(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) CHECK (LENGTH(password) >= 6),
    description TEXT NOT NULL,
    address_id UUID NOT NULL,
    CONSTRAINT country_user_fk FOREIGN KEY (address_id) REFERENCES addresses(id)
);

CREATE TABLE companies(
    user_id UUID PRIMARY KEY,
    cnpj CHAR(14) NOT NULL UNIQUE,
    CONSTRAINT user_company_fk FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE candidates(
    user_id UUID PRIMARY KEY,
    cpf CHAR(12) NOT NULL UNIQUE,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    CONSTRAINT user_candidate_fk FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE users_skill(
    user_id UUID,
    skill_id UUID,
    PRIMARY KEY(user_id, skill_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (skill_id) REFERENCES skills(id)
);

CREATE TABLE jobs(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(100) NOT NULL,
    address_id UUID NOT NULL,
    publisher_id UUID NOT NULL,
    CONSTRAINT jobs_address_fk FOREIGN KEY (address_id) REFERENCES addresses(id),
    CONSTRAINT jobs_publisher FOREIGN KEY (publisher_id) REFERENCES companies(user_id)
);

CREATE TABLE jobs_skill(
    job_id UUID NOT NULL,
    skill_id UUID NOT NULL,
    PRIMARY KEY(job_id, skill_id),
    FOREIGN KEY (job_id) REFERENCES jobs(id),
    FOREIGN KEY (skill_id) REFERENCES skills(id)
);