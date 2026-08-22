package zg.acelera.repository

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import zg.acelera.domain.Candidate
import zg.acelera.domain.Company
import zg.acelera.domain.IPerson
import zg.acelera.domain.Skill
import zg.acelera.dto.company.CompanyDTO
import zg.acelera.dto.company.CompanyUpdateDTO

import java.nio.file.Paths

class CompanyRepository implements ICompanyRepository {
    private final String candidateFileName
    private final File file

    CompanyRepository(String fileName = "companies.json") {
        this.candidateFileName = fileName
        this.file = Paths.get(candidateFileName).toFile()
        initializeFile()
    }

    private void initializeFile() {
        if (!file.exists()) {
            file.createNewFile()
            file.text = "[]"
        }
    }

    @Override
    List<IPerson> findAll() {
        if (file.text.trim().isEmpty()) return []

        def slurper = new JsonSlurper()
        def jsonList = slurper.parse(file)

        List<IPerson> companies = []

        jsonList.each { map ->
            Set<Skill> loadedSkills = map.skills?.collect { Skill.valueOf(it.toString()) } as HashSet
            Set<String> loadedLikes = map.liked ? (map.liked as HashSet) : new HashSet<String>()

            companies += Company.builder()
                    .name(map.name)
                    .email(map.email)
                    .state(map.state)
                    .cep(map.cep)
                    .description(map.description)
                    .cnpj(map.cnpj)
                    .skills(loadedSkills)
                    .liked(loadedLikes)
                    .build()
        }

        companies
    }

    private void rewriteFile(List<IPerson> companies) {
        def builder = new JsonBuilder()

        builder companies.collect { person ->
            def c = (Company) person
            [
                    cnpj: c.cnpj,
                    name: c.name,
                    email: c.email,
                    state: c.state,
                    cep: c.cep,
                    description: c.description,
                    skills: c.skills.collect { it.name() },
                    liked: c.liked
            ]
        }

        file.text = builder.toPrettyString()
    }

    @Override
    IPerson findByCnpj(String cnpj) {
        findAll().find { ((Company) it).cnpj == cnpj }
    }

    @Override
    List<IPerson> findBySkill(Skill skill) {
        findAll().findAll { it.skills.contains(skill) }
    }

    @Override
    Company save(CompanyDTO user) {
        if (findByCnpj(user.cnpj()))
            throw new IllegalArgumentException("Already exists a company with this CNPJ: ${user.cnpj()}")

        List<IPerson> all = findAll()
        all.add(user.toCompany())
        rewriteFile(all)

        user.toCompany()
    }

    @Override
    Company update(CompanyUpdateDTO user) {
        List<IPerson> all = findAll()
        int index = all.findIndexOf { ((Company) it).cnpj == user.cnpj() }

        if (index == -1) throw new IllegalArgumentException("Company not found with CNPJ: ${user.cnpj()}")

        def existing = all[index] as Company

        if (user.name() != null) existing.name = user.name()
        if (user.corporateEmail() != null) existing.email = user.corporateEmail()
        if (user.state() != null) existing.state = user.state()
        if (user.cep() != null) existing.cep = user.cep()
        if (user.description() != null) existing.description = user.description()
        if (user.skills() != null && !user.skills().isEmpty()) {
            existing.skills = user.skills().collect { Skill.valueOf(it) } as Set<Skill>
        }

        all[index] = existing
        rewriteFile(all)

        existing
    }

    @Override
    void update(Company company) {
        List<IPerson> all = findAll()
        int index = all.findIndexOf {((Company) it).cnpj == company.cnpj}

        all[index] = company
        rewriteFile(all)
    }

    @Override
    void delete(String cnpj) {
        List<IPerson> all = findAll()
        if (all.removeIf { ((Company) it).cnpj == cnpj }) {
            rewriteFile(all)
        }
    }
}