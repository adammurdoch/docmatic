package net.rubygrapefruit.docs.model

import spock.lang.Specification

class BuildableComponentSpec extends Specification {
    final BuildableComponent document = new BuildableComponent()

    def "can append level 1 section"() {
        when:
        def section = document.addSection(1)

        then:
        document.contents[0] == section

        document.current == section
        section.current == section
    }

    def "adds implicit parent when level 2 section appended"() {
        when:
        def section = document.addSection(2)

        then:
        BuildableSection implicit = document.contents[0]
        implicit.contents[0] == section

        document.current == section
        implicit.current == section
        section.current == section
    }

    def "can append level 2 section inside level 1 section"() {
        when:
        def section1 = document.addSection(1)
        def section2 = document.addSection(2)

        then:
        document.contents[0] == section1
        section1.contents[0] == section2

        document.current == section2
        section1.current == section2
        section2.current == section2
    }

    def "can append level 1 section after level 2 section"() {
        when:
        def section1 = document.addSection(1)
        def section2 = document.addSection(2)
        def section3 = document.addSection(1)

        then:
        document.contents[0] == section1
        section1.contents[0] == section2
        document.contents[1] == section3

        document.current == section3
        section1.current == section2
        section2.current == section2
        section3.current == section3
    }

    def "can append section onto document"() {
        when:
        def section1 = document.addSection()

        then:
        document.contents[0] == section1

        document.current == section1
        section1.current == section1
    }

    def "can append section after level 2 section"() {
        when:
        def section1 = document.addSection(2)
        def section2 = document.addSection()

        then:
        document.contents[1] == section2

        document.current == section2
        section1.current == section1
    }

    def "can append section after another section"() {
        when:
        def section1 = document.addSection()
        def section2 = document.addSection()

        then:
        document.contents[0] == section1
        document.contents[1] == section2

        document.current == section2
        section1.current == section1
        section2.current == section2
    }

    def "can build tree of sections"() {
        when:
        def section1 = document.addSection()
        def section2 = section1.addSection()
        def section3 = section2.addSection()

        then:
        document.contents[0] == section1
        section1.contents[0] == section2
        section2.contents[0] == section3

        document.current == section3
        section1.current == section3
        section2.current == section3
        section3.current == section3
    }
}
