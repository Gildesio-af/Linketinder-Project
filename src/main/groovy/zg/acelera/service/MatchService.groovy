package zg.acelera.service

import zg.acelera.domain.Candidate
import zg.acelera.domain.Company
import zg.acelera.domain.IPerson
import zg.acelera.repository.CandidateRepository
import zg.acelera.repository.CompanyRepository
import zg.acelera.repository.ICandidateRepository
import zg.acelera.repository.ICompanyRepository

class MatchService {
    private final ICandidateRepository candidateRepository
    private final ICompanyRepository companyRepository
    private IPerson currentUser

    MatchService() {
        this.candidateRepository = new CandidateRepository()
        this.companyRepository = new CompanyRepository()
    }

    void login(String id) {
        String cleanId = id?.replaceAll("\\D", "")

        if (!cleanId) throw new IllegalArgumentException("Please, provide a valid CPF or CNPJ.")


        if (cleanId.length() == 11) {
            def candidate = candidateRepository.findAll().find { ((Candidate) it).cpf == id }
            if (candidate) {
                currentUser = candidate
                return
            }
            throw new IllegalArgumentException("Any candidate found with CPF: ${id}")
        }

        if (cleanId.length() == 14) {
            def company = companyRepository.findAll().find { ((Company) it).cnpj == id }
            if (company) {
                currentUser = company
                return

            }
            throw new IllegalArgumentException("Any company found with CNPJ: ${id}")
        }

        throw new IllegalArgumentException("Invalid format. Please provide a valid CPF (11 digits) or CNPJ (14 digits).")
    }

    List<IPerson> getPotentialMatches() {
        if (currentUser instanceof Candidate) {
            return companyRepository.findAll()
        } else if (currentUser instanceof Company) {
            return candidateRepository.findAll()
        }
        return []
    }

    void likeProfile(Set<String> targetsId) {
        List<IPerson> potentials = getPotentialMatches()
        List<IPerson> likedProfiles = []

        if (currentUser instanceof Candidate) {
            likedProfiles = potentials.findAll { targetsId.contains(((Company) it).cnpj) }
            for( Company target : likedProfiles) {
                currentUser.like(target.cnpj)
            }
            candidateRepository.update(currentUser as Candidate)
        } else if (currentUser instanceof Company) {
            likedProfiles = potentials.findAll { targetsId.contains(((Candidate) it).cpf) }
            for( Candidate target : likedProfiles) {
                currentUser.like(target.cpf)
            }
            companyRepository.update(currentUser as Company)
        }

        println("Liked profiles updated successfully.")
    }

    void showMatches() {
        List<IPerson> matches = []
        if(currentUser instanceof Candidate) {
            matches = searchCandidateMatches()
        } else if(currentUser instanceof Company) {
            matches = searchCompanyMatches()
        } else {
            println("No user is currently logged in.")
        }

        println("--- MATCHES ---")
        matches.each { it.showDetails() }
    }

    List<Company> searchCandidateMatches() {
        List<Company> allCompanies = companyRepository.findAll() as List<Company>

        allCompanies.findAll { it.cnpj in currentUser.liked && currentUser.cpf in it.liked}
    }

    List<Candidate> searchCompanyMatches() {
        List<Candidate> allCandidates = candidateRepository.findAll() as List<Candidate>

        allCandidates.findAll { candidate ->
            candidate.cpf in currentUser.liked && currentUser.cnpj in candidate.liked
        }
    }
}