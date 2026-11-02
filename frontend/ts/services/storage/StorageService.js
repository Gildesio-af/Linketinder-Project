export class StorageService {
    static KEYS = {
        CANDIDATES: "lt_candidates",
        COMPANIES: "lt_companies",
        JOBS: "lt_jobs",
        CURRENT_USER: "lt_current_user",
        MATCHES: "lt_matches",
        RECRUITER_SESSION: "lt_recruiter_session",
        RECRUITER_INVITATIONS: "lt_recruiter_invitations"
    };
    static getCandidates() {
        const data = localStorage.getItem(this.KEYS.CANDIDATES);
        return data ? JSON.parse(data) : [];
    }
    static saveCandidate(candidate) {
        const candidates = this.getCandidates();
        if (candidates.some(c => c.email === candidate.email || c.cpf === candidate.cpf))
            throw new Error("User already exists with the email or CPF provided.");
        candidates.push(candidate);
        localStorage.setItem(this.KEYS.CANDIDATES, JSON.stringify(candidates));
    }
    static updateCandidate(candidate) {
        const candidates = this.getCandidates();
        const index = candidates.findIndex(c => c.cpf === candidate.cpf);
        if (index == -1)
            throw new Error("Candidate not found.");
        candidates[index] = candidate;
        localStorage.setItem(this.KEYS.CANDIDATES, JSON.stringify(candidates));
        return candidates[index];
    }
    static deleteCandidate(cpf) {
        const candidates = this.getCandidates();
        const filtered = candidates.filter(c => c.cpf !== cpf);
        localStorage.setItem(this.KEYS.CANDIDATES, JSON.stringify(filtered));
    }
    static getCompanies() {
        const data = localStorage.getItem(this.KEYS.COMPANIES);
        return data ? JSON.parse(data) : [];
    }
    static saveCompany(company) {
        const companies = this.getCompanies();
        if (companies.some(c => c.email === company.email || c.cnpj === company.cnpj))
            throw new Error("User already exists with the email or CNPJ provided.");
        companies.push(company);
        localStorage.setItem(this.KEYS.COMPANIES, JSON.stringify(companies));
    }
    static updateCompany(company) {
        const companies = this.getCompanies();
        const index = companies.findIndex(c => c.cnpj === company.cnpj);
        if (index == -1)
            throw new Error("Company not found.");
        company.password = companies[index]?.password || "";
        companies[index] = company;
        localStorage.setItem(this.KEYS.COMPANIES, JSON.stringify(companies));
        return companies[index];
    }
    static deleteCompany(cnpj) {
        const companies = this.getCompanies();
        const filtered = companies.filter(c => c.cnpj !== cnpj);
        localStorage.setItem(this.KEYS.COMPANIES, JSON.stringify(filtered));
    }
    static getJobs() {
        const data = localStorage.getItem(this.KEYS.JOBS);
        return data ? JSON.parse(data) : [];
    }
    static saveJob(job) {
        const jobs = this.getJobs();
        if (jobs.some(j => j.name === job.name))
            throw new Error("Job already exists with the name provided.");
        jobs.push(job);
        localStorage.setItem(this.KEYS.JOBS, JSON.stringify(jobs));
    }
    static updateJob(job) {
        const jobs = this.getJobs();
        const index = jobs.findIndex(j => j.name === job.name);
        if (index == -1)
            throw new Error("Job not found.");
        jobs[index] = job;
        localStorage.setItem(this.KEYS.JOBS, JSON.stringify(jobs));
        return jobs[index];
    }
    static deleteJob(job) {
        const jobs = this.getJobs();
        const filtered = jobs.filter(j => j.name !== job.name);
        localStorage.setItem(this.KEYS.JOBS, JSON.stringify(filtered));
    }
    static getCurrentUser() {
        const data = localStorage.getItem(this.KEYS.CURRENT_USER);
        return data ? JSON.parse(data) : null;
    }
    static setCurrentUser(user) {
        localStorage.setItem(this.KEYS.CURRENT_USER, JSON.stringify(user));
    }
    static deleteCurrentUser() {
        localStorage.removeItem(this.KEYS.CURRENT_USER);
    }
    static getRecruiterSession() {
        const data = localStorage.getItem(this.KEYS.RECRUITER_SESSION);
        return data ? JSON.parse(data) : null;
    }
    static setRecruiterSession(session) {
        const safeSession = {
            userId: session.userId,
            name: session.name,
            email: session.email,
            recruitmentAreaId: session.recruitmentAreaId,
            recruitmentArea: session.recruitmentArea,
            description: session.description,
            status: session.status
        };
        localStorage.setItem(this.KEYS.RECRUITER_SESSION, JSON.stringify(safeSession));
    }
    static deleteRecruiterSession() {
        localStorage.removeItem(this.KEYS.RECRUITER_SESSION);
        this.clearRecruiterInvitationCache();
    }
    static getRecruiterInvitationCache() {
        const data = localStorage.getItem(this.KEYS.RECRUITER_INVITATIONS);
        if (!data)
            return null;
        try {
            const cache = JSON.parse(data);
            if (typeof cache.recruiterId !== "string" || !Array.isArray(cache.invitations)) {
                this.clearRecruiterInvitationCache();
                return null;
            }
            return { recruiterId: cache.recruiterId, invitations: cache.invitations };
        }
        catch {
            this.clearRecruiterInvitationCache();
            return null;
        }
    }
    static setRecruiterInvitationCache(recruiterId, invitations) {
        localStorage.setItem(this.KEYS.RECRUITER_INVITATIONS, JSON.stringify({ recruiterId, invitations }));
    }
    static clearRecruiterInvitationCache() {
        localStorage.removeItem(this.KEYS.RECRUITER_INVITATIONS);
    }
    static getTopSkills(limit = 5) {
        const candidates = this.getCandidates();
        const skillCounts = {};
        const skillNames = {};
        candidates.forEach(candidate => {
            if (candidate.skills && candidate.skills.length > 0) {
                candidate.skills.forEach(skill => {
                    const cleanSkill = skill.trim();
                    const lowerSkill = cleanSkill.toLocaleLowerCase();
                    skillCounts[lowerSkill] = (skillCounts[lowerSkill] || 0) + 1;
                    if (!skillNames[lowerSkill]) {
                        skillNames[lowerSkill] = cleanSkill;
                    }
                });
            }
        });
        const topSkills = Object.entries(skillCounts)
            .map(([lowerSkill, count]) => ({ name: skillNames[lowerSkill], count }))
            .sort((a, b) => b.count - a.count)
            .slice(0, limit);
        return topSkills;
    }
    static getMatches() {
        const data = localStorage.getItem(this.KEYS.MATCHES);
        return data ? JSON.parse(data) : [];
    }
    static saveMatch(match) {
        const matches = this.getMatches();
        const alreadyExists = matches.some(existing => existing.cnpj === match.cnpj
            && existing.cpf === match.cpf
            && existing.jobId === match.jobId);
        if (alreadyExists)
            return;
        matches.push(match);
        localStorage.setItem(this.KEYS.MATCHES, JSON.stringify(matches));
    }
    static deleteMatch(match) {
        const matches = this.getMatches();
        const filtered = matches.filter(m => !(m.cnpj === match.cnpj && m.cpf === match.cpf && m.jobId === match.jobId));
        localStorage.setItem(this.KEYS.MATCHES, JSON.stringify(filtered));
    }
}
//# sourceMappingURL=StorageService.js.map