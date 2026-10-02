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

                return true;
            }
        }

        return false;
    }

    private static saveMatch(company: Company, candidate: Candidate): boolean {
        const matches: Match[] = StorageService.getMatches();
        const existsMatch: boolean = matches.some(match => match.cnpj === company.cnpj && match.cpf === candidate.cpf);
        
        if(!existsMatch) {
            const match: Match = {
                cnpj: company.cnpj,
                cpf: candidate.cpf
            };

            StorageService.saveMatch(match);
            return true;
        }
        return false;
    }
}