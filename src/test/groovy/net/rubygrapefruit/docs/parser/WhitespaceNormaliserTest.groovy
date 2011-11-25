package net.rubygrapefruit.docs.parser

import spock.lang.Specification

class WhitespaceNormaliserTest extends Specification {
    def "normalises empty string"() {
        expect:
        normalise("") == ""
    }

    def "normalises whitespace only string"() {
        expect:
        normalise("  \t  \r\n  ") == ""
        normalise(" ", "\t ", "\r\n\n") == ""
    }

    def "removes leading whitespace"() {
        expect:
        normalise("  \t  \r\n  abc") == "abc"
        normalise(" ", "\t ", "abc", "d") == "abcd"
    }

    def "removes trailing whitespace"() {
        expect:
        normalise("abc  \t  \r\n  ") == "abc"
        normalise("abc", "d  ", "\r\n") == "abcd"
    }

    def "removes internal whitespace"() {
        expect:
        normalise("  abc  \t def \r\n ghi ") == "abc def ghi"
        normalise("  abc ", "  de fg", "  hij ") == "abc de fg hij"
    }

    def normalise(String... values) {
        def result = new WhitespaceNormaliser()
        values.each { result.append(it) }
        return result.getText().toString()
    }
}
