package zg.acelera.repository

import zg.acelera.domain.Candidate
import zg.acelera.domain.Company
import zg.acelera.domain.IPerson
import zg.acelera.domain.Skill
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO

import java.nio.file.Paths

class CandidateRepository implements ICandidateRepository {
    private static final String USER_FILE_NAME = "candidates.json"
    private final File file

    CandidateRepository() {
        this.file = Paths.get(USER_FILE_NAME).toFile()
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

        List<IPerson> candidates = []

        jsonList.each { map ->
            Set<Skill> loadedSkills = map.skills?.collect { Skill.valueOf(it.toString()) } as HashSet
            Set<String> loadedLikes = map.liked ? (map.liked as HashSet) : new HashSet<String>()

            candidates += Candidate.builder()
                    .name(map.name)
                    .email(map.email)
                    .state(map.state)
                    .cep(map.cep)
                    .description(map.description)
                    .cpf(map.cpf)
                    .age(map.age)
                    .skills(loadedSkills)
                    .liked(loadedLikes)
                    .build()
        }

        candidates
    }

    private void rewriteFile(List<IPerson> candidates) {
        def builder = new JsonBuilder()

        builder candidates.collect { person ->
            def c = (Candidate) person
            [
                    cpf: c.cpf,
                    name: c.name,
                    email: c.email,
                    age: c.age,
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
    IPerson findByCpf(String cpf) {
        return findAll().find { ((Candidate) it).cpf == cpf }
    }

    @Override
    List<IPerson> findBySkill(Skill skill) {
        return findAll().findAll { it.skills.contains(skill) }
    }

    @Override
    Candidate save(CandidateDTO user) {
        if (findByCpf(user.cpf()))
            throw new IllegalArgumentException("Already exists a candidate with this CPF: ${user.cpf()}")

        List<IPerson> all = findAll()
        all.add(user.toCandidate())
        rewriteFile(all)

        return user.toCandidate()
    }

    @Override
    Candidate update(CandidateUpdateDTO user) {
        List<IPerson> all = findAll()
        int index = all.findIndexOf { ((Candidate) it).cpf == user.cpf() }

        if (index == -1) throw new IllegalArgumentException("Candidate not found with CPF: ${user.cpf()}")

        all[index] = user.updateCandidate(all[index] as Candidate)
        rewriteFile(all)

        return all[index] as Candidate
    }

    @Override
    void update(Candidate candidate) {
        List<IPerson> all = findAll()
        int index = all.findIndexOf { ((Candidate) it).cpf == candidate.cpf}

        all[index] = candidate
        rewriteFile(all)
    }

    @Override
    void delete(String cpf) {
        List<IPerson> all = findAll()
        if (all.removeIf { ((Candidate) it).cpf == cpf }) {
            rewriteFile(all)
        }
    }
}