package reposistory

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Specification
import zg.acelera.repository.like.CompanyLikeRepositoryJDBC

class CompanyLikeRepositoryJDBCSpec extends Specification {

    Sql sqlMock
    CompanyLikeRepositoryJDBC repository

    UUID candidateId = UUID.randomUUID()
    UUID companyId = UUID.randomUUID()

    def setup() {
        sqlMock = Mock(Sql)
        repository = new CompanyLikeRepositoryJDBC(sqlMock)
    }

    def "like should insert company id and liked candidate id in this order"() {
        when:
        repository.like(candidateId, companyId)

        then:
        1 * sqlMock.executeInsert('INSERT INTO companies_like (company_id, liked_candidate_id) VALUES (?, ?)', [companyId, candidateId])
    }

    def "unlike should delete by company id and liked candidate id"() {
        when:
        repository.unlike(candidateId, companyId)

        then:
        1 * sqlMock.executeUpdate('DELETE FROM companies_like WHERE company_id = ? AND liked_candidate_id = ?', [companyId, candidateId])
    }

    def "existsLike should return #expected when query returns #liked"() {
        when:
        boolean result = repository.existsLike(candidateId, companyId)

        then:
        1 * sqlMock.firstRow({ it.toString().contains("FROM companies_like") }, [companyId, candidateId]) >> ([liked: liked] as GroovyRowResult)
        result == expected

        where:
        liked || expected
        true  || true
        false || false
    }
}
