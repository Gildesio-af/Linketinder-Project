INSERT INTO countries (id, name, code)
VALUES ('11111111-0000-0000-0000-000000000000', 'Brasil', 'BR')
ON CONFLICT (id) DO NOTHING;

INSERT INTO skills (id, name) VALUES
    ('22222222-0000-0000-0000-000000000001', 'Java'),
    ('22222222-0000-0000-0000-000000000002', 'Spring Boot'),
    ('22222222-0000-0000-0000-000000000003', 'PostgreSQL'),
    ('22222222-0000-0000-0000-000000000004', 'React');

INSERT INTO users (id, name, email, password, description) VALUES
    ('33333333-0000-0000-0000-000000000001', 'Ana Silva', 'ana@email.com', 'senha123', 'Desenvolvedora Backend'),
    ('33333333-0000-0000-0000-000000000002', 'Bruno Costa', 'bruno@email.com', 'senha123', 'Desenvolvedor Frontend'),
    ('33333333-0000-0000-0000-000000000003', 'Carla Dias', 'carla@email.com', 'senha123', 'Engenheira de Dados'),
    ('33333333-0000-0000-0000-000000000004', 'Diego Souza', 'diego@email.com', 'senha123', 'DevOps Engineer'),
    ('33333333-0000-0000-0000-000000000005', 'Elena Rocha', 'elena@email.com', 'senha123', 'Tech Lead');

INSERT INTO candidates (user_id, cpf, last_name, birth_date) VALUES
    ('33333333-0000-0000-0000-000000000001', '11111111111', 'Silva', '1995-05-10'),
    ('33333333-0000-0000-0000-000000000002', '22222222222', 'Costa', '1998-08-20'),
    ('33333333-0000-0000-0000-000000000003', '33333333333', 'Dias', '1992-11-05'),
    ('33333333-0000-0000-0000-000000000004', '44444444444', 'Souza', '1990-01-30'),
    ('33333333-0000-0000-0000-000000000005', '55555555555', 'Rocha', '1988-12-15');

INSERT INTO users (id, name, email, password, description) VALUES
    ('44444444-0000-0000-0000-000000000001', 'Tech Corp', 'contato@techcorp.com', 'senha123', 'Consultoria de Software'),
    ('44444444-0000-0000-0000-000000000002', 'Inova Sistemas', 'rh@inova.com.br', 'senha123', 'Fábrica de Software'),
    ('44444444-0000-0000-0000-000000000003', 'Cloud Solutions', 'vagas@cloudsol.com', 'senha123', 'Especialistas em Nuvem'),
    ('44444444-0000-0000-0000-000000000004', 'Data Insights', 'jobs@data.com', 'senha123', 'Big Data e IA'),
    ('44444444-0000-0000-0000-000000000005', 'Secure Bank', 'tech@securebank.com', 'senha123', 'Fintech de pagamentos');

INSERT INTO companies (user_id, cnpj) VALUES
    ('44444444-0000-0000-0000-000000000001', '11111111000111'),
    ('44444444-0000-0000-0000-000000000002', '22222222000122'),
    ('44444444-0000-0000-0000-000000000003', '33333333000133'),
    ('44444444-0000-0000-0000-000000000004', '44444444000144'),
    ('44444444-0000-0000-0000-000000000005', '55555555000155');

INSERT INTO addresses (CEP, street, number, neighborhood, city, state, country_id, user_id) VALUES
    ('01001000', 'Praça da Sé', '1', 'Sé', 'São Paulo', 'SP', '11111111-0000-0000-0000-000000000000', '44444444-0000-0000-0000-000000000001'),
    ('20040002', 'Avenida Rio Branco', '156', 'Centro', 'Rio de Janeiro', 'RJ', '11111111-0000-0000-0000-000000000000', '33333333-0000-0000-0000-000000000001');