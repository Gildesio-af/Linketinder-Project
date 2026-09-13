import { StorageService } from "../storage/StorageService.js";
export class MatchService {
    static registerLikeCompany(cpf) {
        const company = StorageService.getCurrentUser();
        const candidates = StorageService.getCandidates();
        const candidate = candidates.find(candidate => candidate.cpf === cpf);
        if (company && candidate) {
            if (!company.likedCandidates)
                company.likedCandidates = [];
            if (!company.likedCandidates.includes(cpf)) {
                company.likedCandidates.push(cpf);
                StorageService.updateCompany(company);
                return this.saveMatch(company, candidate);
            }
        }
        return false;
    }
    static registerLikeJob(jobName) {
        const candidate = StorageService.getCurrentUser();
        const jobs = StorageService.getJobs();
        const job = jobs.find(job => job.name === jobName);
        if (candidate && job) {
            if (!candidate.likedJobs)
                candidate.likedJobs = [];
            if (!candidate.likedJobs.includes(jobName)) {
                candidate.likedJobs.push(jobName);
                StorageService.updateCandidate(candidate);
                return true;
            }
        }
        return false;
    }
    static saveMatch(company, candidate) {
        const matches = StorageService.getMatches();
        const existsMatch = matches.some(match => match.cnpj === company.cnpj && match.cpf === candidate.cpf);
        if (!existsMatch) {
            const match = {
                cnpj: company.cnpj,
                cpf: candidate.cpf
            };
            StorageService.saveMatch(match);
            return true;
        }
        return false;
    }
}
//# sourceMappingURL=MatchService.js.map