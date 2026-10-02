import type { User, Candidate, Company, Job, Match } from "../../models/Domain";

export class StorageService {
    private static KEYS = {
        CANDIDATES: "lt_candidates",
        COMPANIES: "lt_companies",
        JOBS: "lt_jobs",
        CURRENT_USER: "lt_current_user",
        MATCHES: "lt_matches"
    }

    static getCandidates(): Candidate[] {
        const data = localStorage.getItem(this.KEYS.CANDIDATES);
        return data ? JSON.parse(data) : [];
    }

    static saveCandidate(candidate: Candidate): void {
        const candidates = this.getCandidates();

        if (candidates.some(c => c.email === candidate.email || c.cpf === candidate.cpf))
            throw new Error("User already exists with the email or CPF provided.");

        candidates.push(candidate)

        localStorage.setItem(this.KEYS.CANDIDATES, JSON.stringify(candidates))
    }

    static updateCandidate(candidate: Candidate): Candidate {
        const candidates = this.getCandidates();
        const index = candidates.findIndex(c => c.cpf === candidate.cpf);

        if (index == -1) throw new Error("Candidate not found.");

        candidates[index] = candidate;
        localStorage.setItem(this.KEYS.CANDIDATES, JSON.stringify(candidates));
        return candidates[index];
    }

    static deleteCandidate(cpf: string): void {
        const candidates = this.getCandidates();
        const filtered = candidates.filter(c => c.cpf !== cpf);

        localStorage.setItem(this.KEYS.CANDIDATES, JSON.stringify(filtered));
    }

    static getCompanies(): Company[] {
        const data = localStorage.getItem(this.KEYS.COMPANIES);
        return data ? JSON.parse(data) : [];
    }

    static saveCompany(company: Company): void {
        const companies = this.getCompanies();

        if (companies.some(c => c.email === company.email || c.cnpj === company.cnpj))
            throw new Error("User already exists with the email or CNPJ provided.");

        companies.push(company);

        localStorage.setItem(this.KEYS.COMPANIES, JSON.stringify(companies));
    }

    static updateCompany(company: Company): Company {
        const companies = this.getCompanies();
        const index = companies.findIndex(c => c.cnpj === company.cnpj);
        if (index == -1) throw new Error("Company not found.");

        company.password = companies[index]?.password || "";

        companies[index] = company;
        localStorage.setItem(this.KEYS.COMPANIES, JSON.stringify(companies));
        return companies[index];
    }

    static deleteCompany(cnpj: string): void {
        const companies = this.getCompanies();
        const filtered = companies.filter(c => c.cnpj !== cnpj);

        localStorage.setItem(this.KEYS.COMPANIES, JSON.stringify(filtered));
    }

    static getJobs(): Job[] {
        const data = localStorage.getItem(this.KEYS.JOBS);
        return data ? JSON.parse(data) : [];
    }

    static saveJob(job: Job): void {
        const jobs = this.getJobs();

        if (jobs.some(j => j.name === job.name))
            throw new Error("Job already exists with the name provided.");

        jobs.push(job);

        localStorage.setItem(this.KEYS.JOBS, JSON.stringify(jobs));
    }

    static updateJob(job: Job): Job {
        const jobs = this.getJobs();
        const index = jobs.findIndex(j => j.name === job.name);

        if (index == -1) throw new Error("Job not found.");

        jobs[index] = job;
        localStorage.setItem(this.KEYS.JOBS, JSON.stringify(jobs));
        return jobs[index];
    }

    static deleteJob(job: Job): void {
        const jobs = this.getJobs();
        const filtered = jobs.filter(j => j.name !== job.name);

        localStorage.setItem(this.KEYS.JOBS, JSON.stringify(filtered));
    }

    static getCurrentUser(): User | null {
        const data = localStorage.getItem(this.KEYS.CURRENT_USER);
        return data ? JSON.parse(data) : null;
    }

    static setCurrentUser(user: User): void {
        localStorage.setItem(this.KEYS.CURRENT_USER, JSON.stringify(user));
    }

    static deleteCurrentUser(): void {
        localStorage.removeItem(this.KEYS.CURRENT_USER);
    }

    static getTopSkills(limit: number = 5): { name: string, count: number }[] {
    const candidates: Candidate[] = this.getCandidates();
    const skillCounts: Record<string, number> = {};
    const skillNames: Record<string, string> = {};

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
        .map(([lowerSkill, count]) => ({ name: skillNames[lowerSkill] as string, count })) 
        .sort((a, b) => b.count - a.count)         
        .slice(0, limit);                              

    return topSkills;
    }

    static getMatches(): Match[] {
        const data = localStorage.getItem(this.KEYS.MATCHES);
        return data ? JSON.parse(data) : [];
    }

    static saveMatch(match: Match): void {
        const matches = this.getMatches();
        matches.push(match);
        localStorage.setItem(this.KEYS.MATCHES, JSON.stringify(matches));
    }

    static deleteMatch(match: Match): void {
        const matches = this.getMatches();
        const filtered = matches.filter(m => m.cnpj !== match.cnpj && m.cpf !== match.cpf);
        localStorage.setItem(this.KEYS.MATCHES, JSON.stringify(filtered));
    }
}