package zg.acelera.service

import zg.acelera.domain.Candidate
import zg.acelera.domain.Company
import zg.acelera.domain.User
import zg.acelera.repository.candidate.CandidateRepositoryImpl
import zg.acelera.repository.company.CompanyRepositoryImpl
import zg.acelera.repository.candidate.CandidateRepository
import zg.acelera.repository.company.CompanyRepository

//TODO: refactor class to use JDBC repositories on challenge
class MatchService {
    private final CandidateRepository candidateRepository
    private final CompanyRepository companyRepository
    private User currentUser

    MatchService() {
        this.candidateRepository = new CandidateRepositoryImpl()
        this.companyRepository = new CompanyRepositoryImpl()
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

    List<User> getPotentialMatches() {
        if (currentUser instanceof Candidate) {
            return companyRepository.findAll()
        } else if (currentUser instanceof Company) {
            return candidateRepository.findAll()
        }
        return []
    }

    void likeProfile(Set<String> targetsId) {
        List<User> potentials = getPotentialMatches()
        List<User> likedProfiles = []

        if (currentUser instanceof Candidate) {
            likedProfiles = potentials.findAll { targetsId.contains(((Company) it).cnpj) }
            for( Company target : (likedProfiles as List<Company>)) {
                currentUser.like(target.cnpj)
            }
            candidateRepository.update(currentUser as Candidate)
        } else if (currentUser instanceof Company) {
            likedProfiles = potentials.findAll { targetsId.contains(((Candidate) it).cpf) }
            for(Candidate target : (likedProfiles as List<Candidate>)) {
                currentUser.like(target.cpf)
            }
            companyRepository.update(currentUser as Company)
        }

        println("Liked profiles updated successfully.")
    }

    void showMatches() {
        List<User> matches = []
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