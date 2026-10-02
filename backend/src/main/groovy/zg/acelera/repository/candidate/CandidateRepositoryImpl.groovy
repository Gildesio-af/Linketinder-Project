package zg.acelera.repository.candidate

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import zg.acelera.domain.Candidate
import zg.acelera.domain.User
import zg.acelera.domain.SkillEnum

import java.nio.file.Paths

class CandidateRepositoryImpl implements CandidateRepository {
    private final String userFileName
    private final File file

    CandidateRepositoryImpl(String fileName = "candidates.json") {
        userFileName = fileName
        this.file = Paths.get(userFileName).toFile()
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
        return null
    }

    @Override
    List<User> findAll() {
        if (file.text.trim().isEmpty()) return []

        def slurper = new JsonSlurper()
        def jsonList = slurper.parse(file)

        List<User> candidates = []

        jsonList.each { map ->
            Set<SkillEnum> loadedSkills = map.skills?.collect { SkillEnum.valueOf(it.toString()) } as HashSet
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

    private void rewriteFile(List<User> candidates) {
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
    User findByCpf(String cpf) {
        return findAll().find { ((Candidate) it).cpf == cpf }
    }

    @Override
    List<User> findBySkill(String skill) {
        return null
    }

    @Override
    Candidate save(Candidate user) {
        return null
    }

    @Override
    Candidate update(Candidate user, UUID userId) {
        return null
    }

    @Override
    void delete(UUID userId) {

    }
}