package zg.acelera.utils.mapper

import groovy.sql.GroovyRowResult
import zg.acelera.domain.Skill

class SkillRowMapper {
    static Skill getDomainFromRow(GroovyRowResult row) {
        if (!row.skill_id) return null

        return new Skill(
                id: UUID.fromString(row.skill_id.toString()),
                name: row.skill_name.toString()
        )
    }
}
