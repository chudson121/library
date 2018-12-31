package io.pillopl.library.lending.domain.patron

import io.pillopl.library.lending.domain.resource.Resource
import io.pillopl.library.lending.domain.resource.ResourceFixture
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.vavr.control.Either
import spock.lang.Specification

import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronResourcesFixture.*
import static PatronResourcesEvent.*
import static io.pillopl.library.lending.domain.resource.ResourceFixture.circulatingResource
import static io.pillopl.library.lending.domain.resource.ResourceFixture.circulatingResourceAt
import static io.pillopl.library.lending.domain.resource.ResourceFixture.resourceOnHold
import static java.util.Collections.emptySet

class RegularPatronRequestingCirculatingResourcesTest extends Specification {

    def 'a regular patron cannot place on hold resource which is on hold'() {
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = regularPatron().placeOnHold(resourceOnHold())
        then:
            hold.isLeft()
            ResourceHoldFailed e = hold.getLeft()
            e.reason.contains("resource is not available")

    }

    //TODO: per month
    def 'a regular patron cannot place on hold more than 5 resources'() {
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = regularPatronWithHolds(holds).placeOnHold(circulatingResource())
        then:
            hold.isLeft()
            ResourceHoldFailed e = hold.getLeft()
            e.reason.contains("patron cannot hold more resources")
        where:
            holds << [5, 6, 3000]

    }

    //TODO: per month
    def 'a regular patron can place on hold resource when he did not place on hold more than 4 resources'() {
        given:
            Resource resource = circulatingResource()
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = regularPatronWithHolds(holds).placeOnHold(resource)
        then:
            hold.isRight()
        where:
            holds << [0, 1, 2, 3, 4]

    }

    def 'a regular patron cannot place on hold resources anymore when he has at least two overdue checkouts'() {
        given:
            LibraryBranchId libraryBranchId = anyBranch()
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold =
                    regularPatronWithOverdueCheckouts(libraryBranchId, resources).placeOnHold(circulatingResourceAt(libraryBranchId))
        then:
            hold.isLeft()
            ResourceHoldFailed e = hold.getLeft()
            e.reason.contains("cannot place on hold when there are overdue checkouts")
        where:
            resources << [
                    [ResourceFixture.anyResourceId(), ResourceFixture.anyResourceId()] as Set,
                    [ResourceFixture.anyResourceId(), ResourceFixture.anyResourceId(), ResourceFixture.anyResourceId()] as Set
            ]


    }

    def 'a regular patron can place on hold resources when he does not have 2 overdues'() {
        given:
            Resource resource = circulatingResource()
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = regularPatronWithOverdueCheckouts(anyBranch(), resources).placeOnHold(resource)
        then:
            hold.isRight()
        where:
            resources <<  [[ResourceFixture.anyResourceId()] as Set,
                           emptySet()]
    }


}
