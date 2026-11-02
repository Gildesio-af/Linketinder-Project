// Cole este arquivo no Console do navegador com o front-end aberto.
// Ele recria um cenário completo para validar a feature de recrutador.
(() => {
    const areaIds = {
        ti: "746f34a8-0602-4b86-8426-eac894be1f03",
        rh: "a62f518f-e4bb-49c4-a0c2-740ba38c328f",
        marketing: "0e62da83-e188-4e7b-b5ae-be4a9a2d1f93"
    };

    const companyOne = {
        name: "Nexa Tecnologia",
        cnpj: "12345678000190",
        email: "empresa@nexa.test",
        localization: "São Paulo - SP",
        password: "Empresa123",
        confirmPassword: "Empresa123",
        description: "Empresa de tecnologia com produtos digitais.",
        skills: ["Java", "SQL", "Cloud"],
        likedCandidates: ["11111111111"]
    };

    const companyTwo = {
        name: "Orbital Marketing",
        cnpj: "98765432000110",
        email: "contato@orbital.test",
        localization: "Curitiba - PR",
        password: "Empresa456",
        confirmPassword: "Empresa456",
        description: "Agência especializada em crescimento e comunicação.",
        skills: ["Marketing", "SEO", "Analytics"],
        likedCandidates: []
    };

    const candidateOne = {
        name: "Ana Souza",
        cpf: "11111111111",
        age: 29,
        email: "ana.candidata@test",
        password: "Candidata123",
        description: "Desenvolvedora full-stack interessada em produtos digitais.",
        localization: "São Paulo - SP",
        skills: ["Java", "SQL", "TypeScript"],
        likedJobs: ["job-nexa-backend"]
    };

    const candidateTwo = {
        name: "Bruno Lima",
        cpf: "22222222222",
        age: 34,
        email: "bruno.candidato@test",
        password: "Candidato456",
        description: "Profissional de dados com experiência em análise e métricas.",
        localization: "Curitiba - PR",
        skills: ["SQL", "Analytics", "Python"],
        likedJobs: ["job-nexa-backend", "job-orbital-growth"]
    };

    const candidateThree = {
        name: "Carla Mendes",
        cpf: "33333333333",
        age: 26,
        email: "carla.candidata@test",
        password: "Candidata789",
        description: "Especialista em comunicação e campanhas digitais.",
        localization: "Belo Horizonte - MG",
        skills: ["Marketing", "SEO", "Conteúdo"],
        likedJobs: []
    };

    const jobs = [
        {
            name: "job-nexa-backend",
            description: "Desenvolvedor(a) backend para APIs e integrações.",
            salary: 9500,
            location: "São Paulo - SP",
            skills: ["Java", "SQL", "Cloud"],
            jobType: "CLT"
        },
        {
            name: "job-nexa-frontend",
            description: "Desenvolvedor(a) frontend para aplicações web.",
            salary: 8500,
            location: "São Paulo - SP",
            skills: ["TypeScript", "HTML", "CSS"],
            jobType: "CLT"
        },
        {
            name: "job-orbital-growth",
            description: "Analista de marketing para aquisição e crescimento.",
            salary: 7000,
            location: "Curitiba - PR",
            skills: ["Marketing", "SEO", "Analytics"],
            jobType: "PJ"
        }
    ];

    const recruiterOne = {
        userId: "recruiter-ana-001",
        name: "Marina Recrutamento",
        email: "marina.recrutadora@test",
        recruitmentAreaId: areaIds.ti,
        recruitmentArea: "TI",
        description: "Recrutadora focada em tecnologia e produto.",
        status: "ACTIVE",
        cpf: "44444444444",
        birthDate: "1988-05-10",
        password: "Recrutadora123"
    };

    const recruiterTwo = {
        userId: "recruiter-inactive-002",
        name: "Rafael Recrutamento",
        email: "rafael.inativo@test",
        recruitmentAreaId: areaIds.rh,
        recruitmentArea: "RH",
        description: "Conta usada para validar bloqueio de usuário inativo.",
        status: "INACTIVE",
        cpf: "55555555555",
        birthDate: "1985-09-20",
        password: "Inativo123"
    };

    const now = new Date().toISOString();
    const invitations = [
        {
            id: "inv-accepted-backend",
            recruiterId: recruiterOne.userId,
            companyId: companyOne.cnpj,
            jobId: "job-nexa-backend",
            status: "ACCEPTED",
            createdAt: now,
            respondedAt: now,
            revokedAt: null
        },
        {
            id: "inv-pending-frontend",
            recruiterId: recruiterOne.userId,
            companyId: companyOne.cnpj,
            jobId: "job-nexa-frontend",
            status: "PENDING",
            createdAt: now,
            respondedAt: null,
            revokedAt: null
        },
        {
            id: "inv-cancelled-growth",
            recruiterId: recruiterOne.userId,
            companyId: companyTwo.cnpj,
            jobId: "job-orbital-growth",
            status: "CANCELLED",
            createdAt: now,
            respondedAt: null,
            revokedAt: null
        },
        {
            id: "inv-revoked-growth",
            recruiterId: recruiterOne.userId,
            companyId: companyTwo.cnpj,
            jobId: "job-orbital-growth",
            status: "REVOKED",
            createdAt: now,
            respondedAt: now,
            revokedAt: now
        }
    ];

    const keysToReset = [
        "lt_candidates",
        "lt_companies",
        "lt_jobs",
        "lt_current_user",
        "lt_matches",
        "lt_recruiter_session",
        "lt_recruiter_invitations",
        "lt_recruiter_invitation_records",
        "lt_recruiter_likes"
    ];

    keysToReset.forEach((key) => localStorage.removeItem(key));
    localStorage.setItem("lt_candidates", JSON.stringify([candidateOne, candidateTwo, candidateThree]));
    localStorage.setItem("lt_companies", JSON.stringify([companyOne, companyTwo]));
    localStorage.setItem("lt_jobs", JSON.stringify(jobs));
    localStorage.setItem("lt_recruiters", JSON.stringify([recruiterOne, recruiterTwo]));
    localStorage.setItem("lt_recruiter_invitation_records", JSON.stringify(invitations));
    localStorage.setItem("lt_recruiter_likes", JSON.stringify([]));
    localStorage.removeItem("lt_matches");
    localStorage.removeItem("lt_current_user");
    localStorage.removeItem("lt_recruiter_session");

    console.info("Cenário de recrutador criado.", {
        recruiterLogin: "marina.recrutadora@test / Recrutadora123",
        inactiveRecruiterLogin: "rafael.inativo@test / Inativo123",
        companyOneLogin: "12345678000190 / Empresa123",
        companyTwoLogin: "98765432000110 / Empresa456",
        candidateOneLogin: "11111111111 / Candidata123",
        candidateTwoLogin: "22222222222 / Candidato456"
    });
})();
