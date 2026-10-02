package zg.acelera.repository.company

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.Company
import zg.acelera.domain.User
import zg.acelera.domain.Skill
import zg.acelera.repository.skill.SkillRepository
import zg.acelera.utils.exception.EntityNotFoundException
import zg.acelera.utils.mapper.SkillRowMapper

class CompanyRepositoryJDBC implements CompanyRepository {
    final Sql sql
    final SkillRepository skillRepository

    CompanyRepositoryJDBC(Sql sql, SkillRepository skillRepository) {
        this.sql = sql
        this.skillRepository = skillRepository
    }

    @Override
    User findById(UUID id) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, co.*, sk.id AS skill_id, sk.name AS skill_name
            FROM users us
            INNER JOIN companies co ON us.id = co.user_id
            LEFT JOIN users_skill usk ON us.id = usk.user_id
            LEFT JOIN skills sk ON sk.id = usk.skill_id
            WHERE us.id = ?
        """, [id])

        if (!rows) throw new EntityNotFoundException("Company with ID ${id} not found")

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

        if (!rows) throw new EntityNotFoundException("Company with Job ID ${uuid} not found")

        return getCompaniesWithSkillsFromRows(rows).first()
    }

    @Override
    List<User> findAll() {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, co.*, sk.id AS skill_id, sk.name AS skill_name
            FROM users us
            INNER JOIN companies co ON us.id = co.user_id
            LEFT JOIN users_skill usk ON us.id = usk.user_id
            LEFT JOIN skills sk ON sk.id = usk.skill_id
        """)

        return getCompaniesWithSkillsFromRows(rows).toList() as List<User>
    }

    @Override
    User findByCnpj(String cnpj) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, co.*, sk.id AS skill_id, sk.name AS skill_name
            FROM users us
            INNER JOIN companies co ON us.id = co.user_id
            LEFT JOIN users_skill usk ON us.id = usk.user_id
            LEFT JOIN skills sk ON sk.id = usk.skill_id
            WHERE co.cnpj = ?
        """, [cnpj])

        if (!rows) throw new EntityNotFoundException("Company with CNPJ ${cnpj} not found")

        return getCompaniesWithSkillsFromRows(rows).first()
    }

    @Override
    List<User> findBySkill(String skill) {
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

        return getCompaniesWithSkillsFromRows(rows).toList() as List<User>
    }

    @Override
    Company save(Company company) {
        Company savedCompany = null

        sql.withTransaction {
            GroovyRowResult rowGenericUser = insertInGenericUser(company)
            UUID generatedUserId = UUID.fromString(rowGenericUser.id.toString())
            GroovyRowResult rowCompany = insertInCompany(generatedUserId, company)
            insertInUsersSkill(company.skills, generatedUserId)

            savedCompany = getCompanyFromUserRowAndCompanyRow(rowGenericUser, rowCompany)
            savedCompany.skills = skillRepository.findByUserId(generatedUserId)
        }

        return savedCompany
    }

    @Override
    Company update(Company company, UUID userId) {
        Company updatedCompany = null

        sql.withTransaction {
            GroovyRowResult rowGenericUser = updateGenericUser(company, userId)

            GroovyRowResult rowCompany = updateCompany(company, userId)

            updatedCompany = getCompanyFromUserRowAndCompanyRow(rowGenericUser, rowCompany)
            updatedCompany.skills = skillRepository.findByUserId(userId)
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

        Set<Skill> skills = rows.findResults { row -> SkillRowMapper.getDomainFromRowToOtherEntities(row)} as Set<Skill>

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

    private GroovyRowResult insertInGenericUser(Company company) {
        return sql.firstRow("""
                INSERT INTO users (name, email, password, description)
                VALUES (?, ?, ?, ?)
                RETURNING *
            """, [company.name, company.email, company.password, company.description])
    }

    private GroovyRowResult insertInCompany(UUID generatedUserId, Company company) {
        return sql.firstRow("""
                INSERT INTO companies (user_id, cnpj) VALUES (?, ?)
                RETURNING *
            """, [generatedUserId, company.cnpj])
    }

    private void insertInUsersSkill(Set<Skill> skills, UUID userId) {
        if (!skills) return

        skills.each { skill ->
            sql.execute("""
                INSERT INTO users_skill (user_id, skill_id)
                VALUES (?, ?)
            """, [userId, skill.id])

        }
    }

    private GroovyRowResult updateCompany(Company company, UUID userId) {
        sql.firstRow("""
                UPDATE companies
                SET cnpj = COALESCE(?, cnpj)
                WHERE user_id = ?
                RETURNING *
            """, [company.cnpj, userId])
    }

    private GroovyRowResult updateGenericUser(Company company, UUID userId) {
        return sql.firstRow("""
                UPDATE users
                SET name = COALESCE(?, name), email = COALESCE(?, email), description = COALESCE(?, description), password = COALESCE(?, password)
                WHERE id = ?
                RETURNING *
            """, [company.name, company.email, company.description, company.password, userId])
    }
}
