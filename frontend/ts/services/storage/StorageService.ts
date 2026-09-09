import type { Candidate, Company, Job } from "../../models/Domain";

export class StorageService {
    private static KEYS = {
        CANDIDATES: "lt_candidates",
        COMPANIES: "lt_companies",
        JOBS: "lt_jobs",
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
}