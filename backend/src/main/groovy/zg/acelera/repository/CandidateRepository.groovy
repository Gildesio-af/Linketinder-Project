package zg.acelera.repository

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import zg.acelera.domain.Candidate
import zg.acelera.domain.IPerson
import zg.acelera.domain.Skill
import zg.acelera.domain.SkillEnum
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO

import java.nio.file.Paths

class CandidateRepository implements ICandidateRepository {
    private final String userFileName
    private final File file

    CandidateRepository(String fileName = "candidates.json") {
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
    IPerson findById(UUID id) {
        return null
    }

    @Override
    List<IPerson> findAll() {
        if (file.text.trim().isEmpty()) return []

        def slurper = new JsonSlurper()
        def jsonList = slurper.parse(file)

        List<IPerson> candidates = []

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
    List<IPerson> findBySkill(String skill) {
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