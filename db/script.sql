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
    address_id UUID NOT NULL ,
    publisher_id UUID NOT NULL ,
    CONSTRAINT jobs_address_fk FOREIGN KEY (address_id) REFERENCES addresses(id),
    CONSTRAINT jobs_publisher FOREIGN KEY (publisher_id) REFERENCES companies(id)
);

CREATE TABLE jobs_skill(
    job_id UUID NOT NULL,
    skill_id UUID NOT NULL,
    PRIMARY KEY(job_id, skill_id),
    FOREIGN KEY (job_id) REFERENCES jobs(id),
    FOREIGN KEY (skill_id) REFERENCES skills(id)
)

CREATE TABLE companies_like(
    company_id UUID,
    liked_user_id UUID,
    PRIMARY KEY (company_id, liked_user_id),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (liked_user_id) REFERENCES candidates(id)
);

CREATE TABLE jobs_like(
    candidate_id UUID,
    job_id UUID,
    PRIMARY KEY (candidate_id, job_id),
    FOREIGN KEY (candidate_id) REFERENCES candidates(id),
    FOREIGN KEY (job_id) REFERENCES jobs(id)
);

CREATE TABLE matches(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    candidate_id UUID NOT NULL,
    company_id UUID NOT NULL,
    job_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT match_candidate_fk FOREIGN KEY (candidate_id) REFERENCES candidates(user_id),
    CONSTRAINT match_company_fk FOREIGN KEY (company_id) REFERENCES companies(user_id),
    CONSTRAINT match_job_fk FOREIGN KEY (job_id) REFERENCES jobs(id),
    CONSTRAINT unique_match UNIQUE (candidate_id, job_id)
);

INSERT INTO countries (id, name, code) VALUES
    ('10000000-0000-4000-8000-000000000001', 'Brazil', 'BR'),
    ('10000000-0000-4000-8000-000000000002', 'United States', 'US'),
    ('10000000-0000-4000-8000-000000000003', 'Canada', 'CA');

INSERT INTO skills (id, name) VALUES
    ('20000000-0000-4000-8000-000000000001', 'JavaScript'),
    ('20000000-0000-4000-8000-000000000002', 'Python'),
    ('20000000-0000-4000-8000-000000000003', 'Java'),
    ('20000000-0000-4000-8000-000000000004', 'C#'),
    ('20000000-0000-4000-8000-000000000005', 'Ruby'),
    ('20000000-0000-4000-8000-000000000006', 'Go'),
    ('20000000-0000-4000-8000-000000000007', 'PHP'),
    ('20000000-0000-4000-8000-000000000008', 'C++'),
    ('20000000-0000-4000-8000-000000000009', 'Swift'),
    ('20000000-0000-4000-8000-000000000010', 'Kotlin');

INSERT INTO addresses (id, cep, street, number, complement, neighborhood, city, state, country_id) VALUES
    ('30000000-0000-4000-8000-000000000001', '01001000', 'Praça da Sé', '1', '', 'Sé', 'São Paulo', 'SP', '10000000-0000-4000-8000-000000000001'),
    ('30000000-0000-4000-8000-000000000002', '20040000', 'Av. Rio Branco', '10', '', 'Centro', 'Rio de Janeiro', 'RJ', '10000000-0000-4000-8000-000000000001'),
    ('30000000-0000-4000-8000-000000000003', '10001000', '5th Avenue', '100', '', 'Manhattan', 'New York', 'NY', '10000000-0000-4000-8000-000000000002'),
    ('30000000-0000-4000-8000-000000000004', '94105000', 'Market Street', '200', '', 'SoMa', 'San Francisco', 'CA', '10000000-0000-4000-8000-000000000002'),
    ('30000000-0000-4000-8000-000000000005', '12345678', 'King Street', '300', '', 'Downtown', 'Toronto', 'ON', '10000000-0000-4000-8000-000000000003');

INSERT INTO addresses (id, cep, street, number, complement, neighborhood, city, state, country_id) VALUES
    ('30000000-0000-4000-8000-000000000006', '04538133', 'Av. Faria Lima', '1000', 'Andar 10', 'Itaim Bibi', 'São Paulo', 'SP', '10000000-0000-4000-8000-000000000001'),
    ('30000000-0000-4000-8000-000000000007', '30130000', 'Av. Afonso Pena', '500', 'Sala 2', 'Centro', 'Belo Horizonte', 'MG', '10000000-0000-4000-8000-000000000001'),
    ('30000000-0000-4000-8000-000000000008', '98052000', 'One Microsoft Way', '1', '', 'Redmond', 'Redmond', 'WA', '10000000-0000-4000-8000-000000000002'),
    ('30000000-0000-4000-8000-000000000009', '94043000', 'Amphitheatre Pkwy', '1600', '', 'Mountain View', 'Mountain View', 'CA', '10000000-0000-4000-8000-000000000002'),
    ('30000000-0000-4000-8000-000000000010', '87654321', 'Water Street', '400', '', 'Gastown', 'Vancouver', 'BC', '10000000-0000-4000-8000-000000000003');

INSERT INTO users (id, name, email, password, description, address_id) VALUES
    ('40000000-0000-4000-8000-000000000001', 'Sandubinha', 'sandubinha@email.com', 'senha123', 'Desenvolvedor Backend buscando oportunidades', '30000000-0000-4000-8000-000000000001'),
    ('40000000-0000-4000-8000-000000000002', 'Maria', 'maria@email.com', 'senha123', 'Analista de Dados Pleno', '30000000-0000-4000-8000-000000000002'),
    ('40000000-0000-4000-8000-000000000003', 'John', 'john@email.com', 'senha123', 'Frontend Developer especialista em React', '30000000-0000-4000-8000-000000000003'),
    ('40000000-0000-4000-8000-000000000004', 'Jane', 'jane@email.com', 'senha123', 'Engenheira de Software e DevOps', '30000000-0000-4000-8000-000000000004'),
    ('40000000-0000-4000-8000-000000000005', 'Carlos', 'carlos@email.com', 'senha123', 'Fullstack JavaScript focado em Node.js', '30000000-0000-4000-8000-000000000005');

INSERT INTO users (id, name, email, password, description, address_id) VALUES
    ('50000000-0000-4000-8000-000000000001', 'Pastelsoft', 'recrutamento@pastelsoft.com', 'senha123', 'Especializada em ERPs para restaurantes', '30000000-0000-4000-8000-000000000006'),
    ('50000000-0000-4000-8000-000000000002', 'Tech Corp', 'vagas@techcorp.com', 'senha123', 'Consultoria de TI e inovação', '30000000-0000-4000-8000-000000000007'),
    ('50000000-0000-4000-8000-000000000003', 'Global Dev', 'hr@globaldev.com', 'senha123', 'Fábrica de software sob medida', '30000000-0000-4000-8000-000000000008'),
    ('50000000-0000-4000-8000-000000000004', 'Inova Cloud', 'jobs@inovacloud.com', 'senha123', 'Soluções em nuvem e infraestrutura', '30000000-0000-4000-8000-000000000009'),
    ('50000000-0000-4000-8000-000000000005', 'Data Systems', 'join@datasystems.ca', 'senha123', 'Especialistas em Big Data e Inteligência Artificial', '30000000-0000-4000-8000-000000000010');

INSERT INTO candidates (user_id, cpf, last_name, birth_date) VALUES
    ('40000000-0000-4000-8000-000000000001', '11111111111', 'Silva', '2000-05-15'),
    ('40000000-0000-4000-8000-000000000002', '22222222222', 'Oliveira', '1995-10-20'),
    ('40000000-0000-4000-8000-000000000003', '33333333333', 'Doe', '1998-02-28'),
    ('40000000-0000-4000-8000-000000000004', '44444444444', 'Smith', '1990-12-10'),
    ('40000000-0000-4000-8000-000000000005', '55555555555', 'Santos', '2002-07-07');

INSERT INTO companies (user_id, cnpj) VALUES
    ('50000000-0000-4000-8000-000000000001', '11111111000111'),
    ('50000000-0000-4000-8000-000000000002', '22222222000122'),
    ('50000000-0000-4000-8000-000000000003', '33333333000133'),
    ('50000000-0000-4000-8000-000000000004', '44444444000144'),
    ('50000000-0000-4000-8000-000000000005', '55555555000155');

