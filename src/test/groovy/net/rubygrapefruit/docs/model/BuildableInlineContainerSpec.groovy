package net.rubygrapefruit.docs.model

import spock.lang.Specification

class BuildableInlineContainerSpec extends Specification {
    final BuildableInlineContainer container = new BuildableInlineContainer()

    def "can append text"() {
        when:
        container.append("text")

        then:
        container.contents.size() == 1
        container.contents[0].text == 'text'
    }

    def "can append text after text"() {
        when:
        container.append("1")
        container.append(" 2 ")
        container.append("3")

        then:
        container.contents.size() == 1
        container.contents[0].text == '1 2 3'
    }

    def "normalises leading and trailing whitespace"() {
        when:
        container.append(" \t\r\n1  ")
        container.append(" 2")
        container.append("  \n")

        then:
        container.contents.size() == 1
        container.contents[0].text == '1 2'
    }

    def "can append unknown after text"() {
        when:
        container.append("text")
        container.addError("message")

        then:
        container.contents.size() == 2
        container.contents[0].text == 'text'
        container.contents[1].message == 'message'
    }

    def "can append text after unknown"() {
        when:
        container.addError("message")
        container.append("text")

        then:
        container.contents.size() == 2
        container.contents[0].message == 'message'
        container.contents[1].text == 'text'
    }

    def "can append text before code element"() {
        expect: false
    }

    def "can append text after code element"() {
        expect: false
    }

    def "preserves whitespace around inline elements"() {
        when:
        container.append(" 1 \t")
        container.addError("unknown")
        container.append("  \n 2\r\n")

        then:
        container.contents.size() == 3
        container.contents[0].text == '1 '
        container.contents[2].text == ' 2'
    }
}

