package zg.acelera.repository

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.Company
import zg.acelera.domain.IPerson
import zg.acelera.dto.company.CompanyDTO
import zg.acelera.dto.company.CompanyUpdateDTO
import zg.acelera.utils.exception.EntityNotFoundException

class CompanyRepositoryJDBC implements ICompanyRepository {
    final Sql sql

    CompanyRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    IPerson findById(UUID id) {
        GroovyRowResult row = sql.firstRow("""
            SELECT us.*, co.* FROM users us
            INNER JOIN companies co ON us.id = co.user_id
            WHERE us.id = ?
        """, [id.toString()])
        if (row) return getCompanyFromRow(row)

        throw new EntityNotFoundException("Company with ID ${id} not found")
    }

    @Override
    List<IPerson> findAll() {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, co.*
            FROM users us
            INNER JOIN companies co ON us.id = co.user_id
        """)
        return rows.collect { row -> getCompanyFromRow(row) }
    }

    @Override
    IPerson findByCnpj(String cnpj) {
        GroovyRowResult row = sql.firstRow("""
            SELECT us.*, co.* FROM users us
            INNER JOIN companies co ON us.id = co.user_id
            WHERE co.cnpj = ?
        """, [cnpj])
        if (row) return getCompanyFromRow(row)

        throw new EntityNotFoundException("Company with CNPJ ${cnpj} not found")
    }

    @Override
    List<IPerson> findBySkill(String skill) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, co.* FROM users us
            INNER JOIN companies co ON us.id = co.user_id
            INNER JOIN users_skill usk ON us.id = usk.user_id
            INNER JOIN skills sk ON usk.skill_id = sk.id
            WHERE lower(sk.name) = lower(?)
        """, [skill])
        return rows.collect { row -> getCompanyFromRow(row) }
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

            savedCompany = getCompanyFromUserRowAndCompanyRow(rowGenericUser, rowCompany)
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
