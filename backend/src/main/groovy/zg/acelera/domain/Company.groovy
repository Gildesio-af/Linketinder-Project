package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder

@Canonical
@EqualsAndHashCode(includes = ['cnpj'])
@Builder(includeSuperProperties = true)
class Company extends UserBase{
    String cnpj
    Set<Candidate> likedCandidates = [] as HashSet<Candidate>
}
