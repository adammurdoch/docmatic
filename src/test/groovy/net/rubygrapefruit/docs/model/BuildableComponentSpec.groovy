package net.rubygrapefruit.docs.model

import spock.lang.Specification

class BuildableComponentSpec extends Specification {
    final BuildableComponent document = new BuildableComponent() {
        String getTypeName() {
            return "component"
        }
    }

    def "normalises id"(String raw, String normalised) {
        when:
        document.id = raw

        then:
        document.id == normalised

        where:
        raw     | normalised
        ''      | null
        '   '   | null
        'abc'   | 'abc'
        ' abc ' | 'abc'
    }

    def "can append level 1 section"() {
        when:
        def section = document.addSection(1)

        then:
        document.contents == [section]
        document.components == [section]

        document.current == section
        section.current == section
    }

    def "adds implicit parent when level 2 section appended"() {
        when:
        def section = document.addSection(2)

        then:
        BuildableSection implicit = document.contents[0]
        implicit.contents == [section]

        document.current == section
        implicit.current == section
        section.current == section
    }

    def "can append level 2 section inside level 1 section"() {
        when:
        def section1 = document.addSection(1)
        def section2 = document.addSection(2)

        then:
        document.contents == [section1]
        document.components == [section1]
        section1.contents == [section2]
        section1.components == [section2]

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
        document.contents == [section1, section3]
        document.components == [section1, section3]
        section1.contents == [section2]
        section1.components == [section2]

        document.current == section3
        section1.current == section2
        section2.current == section2
        section3.current == section3
    }

    def "can append section onto document"() {
        when:
        def section1 = document.addSection()

        then:
        document.contents == [section1]
        document.components == [section1]

        document.current == section1
        section1.current == section1
    }

    def "can append section after level 2 section"() {
        when:
        def section1 = document.addSection(2)
        def section2 = document.addSection()

        then:
        document.contents[1] == section2
        document.components[1] == section2

        document.current == section2
        section1.current == section1
    }

    def "can append section after another section"() {
        when:
        def section1 = document.addSection()
        def section2 = document.addSection()

        then:
        document.contents == [section1, section2]
        document.components == [section1, section2]

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
        document.contents == [section1]
        section1.contents == [section2]
        section2.contents == [section3]

        document.current == section3
        section1.current == section3
        section2.current == section3
        section3.current == section3
    }

    def "can add structural components"() {
        when:
        def part = document.addPart()
        def chapter = document.addChapter()
        def appendix = document.addAppendix()

        then:
        document.contents == [part, chapter, appendix]
        document.components == [part, chapter, appendix]
        document.current == appendix
    }
}
