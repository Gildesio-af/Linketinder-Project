import { StorageService } from "../storage/StorageService.js";
import type { Company, Candidate, Match, Job } from "../../models/Domain.js";

export class MatchService {
    static registerLikeCompany(cpf: string): boolean {
        const company: Company = StorageService.getCurrentUser() as Company;
        const candidates: Candidate[] = StorageService.getCandidates();

        const candidate = candidates.find(candidate => candidate.cpf === cpf);

        if(company && candidate) {
            if(!company.likedCandidates) 
                company.likedCandidates = [];

            if(!company.likedCandidates.includes(cpf)) {
                company.likedCandidates.push(cpf);
                StorageService.updateCompany(company);

                return this.saveMatch(company, candidate);
            }
        }

        return false;
    }

    static registerLikeJob(jobName: string): boolean {
        const candidate: Candidate = StorageService.getCurrentUser() as Candidate;
        const jobs: Job[] = StorageService.getJobs();

        const job = jobs.find(job => job.name === jobName);

        if(candidate && job) {
            if(!candidate.likedJobs) 
                candidate.likedJobs = [];

            if(!candidate.likedJobs.includes(jobName)) {
                candidate.likedJobs.push(jobName);
                StorageService.updateCandidate(candidate);
                StorageService.getCompanies()
                    .filter(company => (company.likedCandidates ?? []).includes(candidate.cpf))
                    .forEach(company => this.saveMatch(company, candidate));
                return true;
            }
        }

        return false;
    }

    private static saveMatch(company: Company, candidate: Candidate): boolean {
        const matches: Match[] = StorageService.getMatches();
        const matchingJobs = StorageService.getJobs()
            .filter(job => (candidate.likedJobs ?? []).includes(job.name))
            .map(job => job.name);
        const existsMatch: boolean = matchingJobs.some(jobId =>
            matches.some(match =>
                match.cnpj === company.cnpj && match.cpf === candidate.cpf && match.jobId === jobId
            )
        );
        
        if (!existsMatch && matchingJobs.length > 0) {
            matchingJobs.forEach(jobId => StorageService.saveMatch({
                cnpj: company.cnpj,
                cpf: candidate.cpf,
                jobId
            }));
            return true;
        }
        return false;
    }
}