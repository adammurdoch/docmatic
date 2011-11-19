package net.rubygrapefruit.docs.model

import spock.lang.Specification

class DocumentBuilderSpec extends Specification {
    final BuildableDocument document = new BuildableDocument()
    final DocumentBuilder builder = new DocumentBuilder(document)

    def "can append level 1 section"() {
        when:
        def section = builder.appendSection(1)

        then:
        document.contents[0] == section
    }

    def "adds implicit parent when level 2 section appended"() {
        when:
        def section = builder.appendSection(2)

        then:
        document.contents[0] instanceof Section
        document.contents[0].contents[0] == section
    }

    def "can append level 2 section inside level 1 section"() {
        when:
        def section1 = builder.appendSection(1)
        def section2 = builder.appendSection(2)

        then:
        document.contents[0] == section1
        document.contents[0].contents[0] == section2
    }

    def "can append level 1 section after level 2 section"() {
        when:
        def section1 = builder.appendSection(1)
        def section2 = builder.appendSection(2)
        def section3 = builder.appendSection(1)

        then:
        document.contents[0] == section1
        document.contents[0].contents[0] == section2
        document.contents[1] == section3
    }

    def "appends paragraph to document when there is no current section"() {
        when:
        def para = builder.appendParagraph()

        then:
        document.contents[0] == para
    }

    def "appends paragraph to most recently appended section"() {
        when:
        def section = builder.appendSection(1)
        def para = builder.appendParagraph()

        then:
        section.contents[0] == para
    }
}
