package zg.acelera.repository.company

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import zg.acelera.domain.Company
import zg.acelera.domain.User

import java.nio.file.Paths

class CompanyRepositoryImpl implements CompanyRepository {
    private final String companyFileName
    private final File file

    CompanyRepositoryImpl(String fileName = "companies.json") {
        this.companyFileName = fileName
        this.file = Paths.get(companyFileName).toFile()
        initializeFile()
    }

    private void initializeFile() {
        if (!file.exists()) {
            file.createNewFile()
            file.text = "[]"
        }
    }

    @Override
    User findById(UUID id) {
        return findAll().find { it.id == id }
    }

    @Override
    Company findByJobId(UUID uuid) {
        return null
    }

    @Override
    List<User> findAll() {
        if (file.text.trim().isEmpty()) return []

        def slurper = new JsonSlurper()
        def jsonList = slurper.parse(file)

        return jsonList.collect { map ->
            Company.builder()
                    .id(map.id ? UUID.fromString(map.id.toString()) : null)
                    .name(map.name)
                    .email(map.email)
                    .password(map.password)
                    .description(map.description)
                    .cnpj(map.cnpj)
                    .skills((map.skills ?: []).collect { skillName -> new zg.acelera.domain.Skill(name: skillName.toString()) } as Set)
                    .liked((map.liked ?: []) as Set)
                    .build()
        }
    }

    private void rewriteFile(List<User> companies) {
        def builder = new JsonBuilder()

        builder companies.collect { person ->
            def c = (Company) person
            [
                    id: c.id?.toString(),
                    cnpj: c.cnpj,
                    name: c.name,
                    email: c.email,
                    password: c.password,
                    description: c.description,
                    skills: c.skills.collect { it.name ?: it.id?.toString() },
                    liked: c.liked
            ]
        }

        file.text = builder.toPrettyString()
    }

    @Override
    User findByCnpj(String cnpj) {
        return findAll().find { ((Company) it).cnpj == cnpj }
    }

    @Override
    List<User> findBySkill(String skill) {
        return findAll().findAll { it.skills.any { it.name?.equalsIgnoreCase(skill) } }
    }

    @Override
    Company save(Company company) {
        if (findByCnpj(company.cnpj)) {
            throw new IllegalArgumentException("Already exists a company with this CNPJ: ${company.cnpj}")
        }

        company.id = company.id ?: UUID.randomUUID()
        List<User> all = findAll()
        all.add(company)
        rewriteFile(all)
        return company
    }

    @Override
    Company update(Company company, UUID userId) {
        List<User> all = findAll()
        int index = all.findIndexOf { ((Company) it).id == userId }

        if (index == -1) throw new IllegalArgumentException("Company not found with ID: ${userId}")

        Company existing = all[index] as Company
        if (company.name) existing.name = company.name
        if (company.email) existing.email = company.email
        if (company.password) existing.password = company.password
        if (company.description) existing.description = company.description
        if (company.cnpj) existing.cnpj = company.cnpj
        if (company.skills) existing.skills = company.skills

        all[index] = existing
        rewriteFile(all)
        return existing
    }

    @Override
    void delete(UUID userId) {
        List<User> all = findAll()
        if (all.removeIf { ((Company) it).id == userId }) {
            rewriteFile(all)
        }
    }
}