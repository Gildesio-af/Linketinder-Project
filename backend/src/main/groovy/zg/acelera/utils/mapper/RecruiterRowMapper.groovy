package zg.acelera.utils.mapper

import groovy.sql.GroovyRowResult
import zg.acelera.domain.Recruiter
import zg.acelera.domain.RecruiterStatus

import java.time.LocalDate

class RecruiterRowMapper {
    static Recruiter fromRow(GroovyRowResult row) {
        if (!row) throw new IllegalArgumentException('Recruiter row cannot be null.')

        new Recruiter(
                id: UUID.fromString((row.id ?: row.user_id).toString()),
                name: row.name?.toString(),
                email: row.email?.toString(),
                description: row.description?.toString(),
                recruitmentAreaId: UUID.fromString(row.recruitment_area_id.toString()),
                cpf: row.cpf?.toString()?.trim(),
                birthDate: row.birth_date ? LocalDate.parse(row.birth_date.toString()) : null,
                status: RecruiterStatus.valueOf(row.status.toString())
        )
    }
}
