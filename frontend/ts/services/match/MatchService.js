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
                StorageService.getCompanies()
                    .filter(company => (company.likedCandidates ?? []).includes(candidate.cpf))
                    .forEach(company => this.saveMatch(company, candidate));
                return true;
            }
        }
        return false;
    }
    static saveMatch(company, candidate) {
        const matches = StorageService.getMatches();
        const matchingJobs = StorageService.getJobs()
            .filter(job => (candidate.likedJobs ?? []).includes(job.name))
            .map(job => job.name);
        const existsMatch = matchingJobs.some(jobId => matches.some(match => match.cnpj === company.cnpj && match.cpf === candidate.cpf && match.jobId === jobId));
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
//# sourceMappingURL=MatchService.js.map