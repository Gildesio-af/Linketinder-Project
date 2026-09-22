package zg.acelera.repository

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.Company
import zg.acelera.domain.IPerson
import zg.acelera.domain.Skill
import zg.acelera.utils.exception.EntityNotFoundException

class CompanyRepositoryJDBC implements ICompanyRepository {
    final Sql sql

    CompanyRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    IPerson findById(UUID id) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, co.*, sk.id AS skill_id, sk.name AS skill_name
            FROM users us
            INNER JOIN companies co ON us.id = co.user_id
            LEFT JOIN users_skill usk ON us.id = usk.user_id
            LEFT JOIN skills sk ON sk.id = usk.skill_id
            WHERE us.id = ?
        """, [id])
        if (!rows) {
            throw new EntityNotFoundException("Company with ID ${id} not found")
        }

        return getCompaniesWithSkillsFromRows(rows).first()
    }

    @Override
    Company findByJobId(UUID uuid) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, co.*, sk.id AS skill_id, sk.name AS skill_name
            FROM users us
            INNER JOIN companies co ON us.id = co.user_id
            INNER JOIN jobs jb ON jb.publisher_id = co.user_id
            LEFT JOIN users_skill usk ON us.id = usk.user_id
            LEFT JOIN skills sk ON sk.id = usk.skill_id
            WHERE jb.id = ?
        """, [uuid])
        if (!rows) {
            return null
        }

        return getCompaniesWithSkillsFromRows(rows).first()
    }

    @Override
    List<IPerson> findAll() {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, co.*, sk.id AS skill_id, sk.name AS skill_name
            FROM users us
            INNER JOIN companies co ON us.id = co.user_id
            LEFT JOIN users_skill usk ON us.id = usk.user_id
            LEFT JOIN skills sk ON sk.id = usk.skill_id
        """)
        return getCompaniesWithSkillsFromRows(rows).toList()
    }

    @Override
    IPerson findByCnpj(String cnpj) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, co.*, sk.id AS skill_id, sk.name AS skill_name
            FROM users us
            INNER JOIN companies co ON us.id = co.user_id
            LEFT JOIN users_skill usk ON us.id = usk.user_id
            LEFT JOIN skills sk ON sk.id = usk.skill_id
            WHERE co.cnpj = ?
        """, [cnpj])
        if (!rows) {
            throw new EntityNotFoundException("Company with CNPJ ${cnpj} not found")
        }

        return getCompaniesWithSkillsFromRows(rows).first()
    }

    @Override
    List<IPerson> findBySkill(String skill) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, co.*, sk.id AS skill_id, sk.name AS skill_name
            FROM users us
            INNER JOIN companies co ON us.id = co.user_id
            LEFT JOIN users_skill usk ON us.id = usk.user_id
            LEFT JOIN skills sk ON sk.id = usk.skill_id
            WHERE us.id IN (
                SELECT DISTINCT usk2.user_id
                FROM users_skill usk2
                INNER JOIN skills sk2 ON sk2.id = usk2.skill_id
                WHERE lower(sk2.name) = lower(?)
            )
        """, [skill])
        return getCompaniesWithSkillsFromRows(rows).toList()
    }

    @Override
    Company save(Company company) {
        Company savedCompany = null

        sql.withTransaction {
            GroovyRowResult rowGenericUser = sql.firstRow("""
                INSERT INTO users (name, email, password, description)
                VALUES (?, ?, ?, ?)
                RETURNING *
            """, [company.name, company.email, company.password, company.description])

            UUID generatedUserId = UUID.fromString(rowGenericUser.id.toString())

            GroovyRowResult rowCompany = sql.firstRow("""
                INSERT INTO companies (user_id, cnpj) VALUES (?, ?)
                RETURNING *
            """, [generatedUserId, company.cnpj])

            if (company.skills) {
                company.skills.each { skill ->
                    if (skill.id) {
                        sql.executeInsert("""
                            INSERT INTO users_skill (user_id, skill_id)
                            VALUES (?, ?)
                        """, [generatedUserId, skill.id])
                    }
                }
            }

            savedCompany = getCompanyFromUserRowAndCompanyRow(rowGenericUser, rowCompany)

            Set<Skill> fullSkills = [] as Set<Skill>
            if (company.skills) {
                List<GroovyRowResult> skillRows = sql.rows("""
                    SELECT sk.id AS skill_id, sk.name AS skill_name
                    FROM users_skill usk
                    INNER JOIN skills sk ON sk.id = usk.skill_id
                    WHERE usk.user_id = ?
                """, [generatedUserId])
                fullSkills = skillRows.collect { row ->
                    new Skill(id: UUID.fromString(row.skill_id.toString()), name: row.skill_name.toString())
                } as Set<Skill>
            }
            savedCompany.skills = fullSkills
        }

        return savedCompany
    }

    @Override
    Company update(Company company, UUID userId) {
        Company updatedCompany = null

        sql.withTransaction {
            GroovyRowResult rowGenericUser = sql.firstRow("""
                UPDATE users
                SET name = COALESCE(?, name), email = COALESCE(?, email), description = COALESCE(?, description), password = COALESCE(?, password)
                WHERE id = ?
                RETURNING *
            """, [company.name, company.email, company.description, company.password, userId])

            GroovyRowResult rowCompany = sql.firstRow("""
                UPDATE companies
                SET cnpj = COALESCE(?, cnpj)
                WHERE user_id = ?
                RETURNING *
            """, [company.cnpj, userId])

            updatedCompany = getCompanyFromUserRowAndCompanyRow(rowGenericUser, rowCompany)
            updatedCompany.skills = company.skills ?: [] as Set<Skill>
        }

        return updatedCompany
    }

    @Override
    void delete(UUID userId) {
        sql.withTransaction {
            sql.execute("DELETE FROM companies WHERE user_id = ?", [userId])
            sql.execute("DELETE FROM users WHERE id = ?", [userId])
        }
    }

    private static Set<Company> getCompaniesWithSkillsFromRows(List<GroovyRowResult> rows) {
        if (!rows) return [] as Set<Company>

        return rows.groupBy { it.id }.collect { userId, userRows ->
            getCompanyWithSkillsFromRows(userRows)
        } as Set<Company>
    }

    private static Company getCompanyWithSkillsFromRows(List<GroovyRowResult> rows) {
        if (!rows) return null

        Company company = getCompanyFromRow(rows.first())

        Set<Skill> skills = rows.findResults { row ->
            if (row.skill_id) {
                return new Skill(
                    id: UUID.fromString(row.skill_id.toString()),
                    name: row.skill_name.toString()
                )
            }
            return null
        } as Set<Skill>

        company.skills = skills
        return company
    }

    private static Company getCompanyFromRow(GroovyRowResult row) {
        return new Company(
                id: UUID.fromString(row.id.toString()),
                name: row.name,
                email: row.email,
                password: row.password,
                description: row.description,
                cnpj: row.cnpj
        )
    }

    private static Company getCompanyFromUserRowAndCompanyRow(GroovyRowResult userRow, GroovyRowResult companyRow) {
        return getCompanyFromRow(userRow + companyRow as GroovyRowResult)
    }
}
