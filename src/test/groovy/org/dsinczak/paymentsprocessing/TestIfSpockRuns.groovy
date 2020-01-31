package org.dsinczak.paymentsprocessing

import spock.lang.Specification

class TestIfSpockRuns extends Specification {

    def "it works"() {
        when:
            println("it works")
        then:
            1 == 1
    }
}
