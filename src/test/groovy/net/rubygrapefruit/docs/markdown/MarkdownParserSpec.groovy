package net.rubygrapefruit.docs.markdown

import spock.lang.Specification

class MarkdownParserSpec extends Specification {
    final MarkdownParser parser = new MarkdownParser()

    def "an empty string converts to an empty document"() {
        when:
        def doc = parser.parse ""

        then:
        doc.paragraphs.empty
    }

    def "an whitespace only string converts to an empty document"() {
        when:
        def doc = parser.parse "  \n  \r\n  "

        then:
        doc.paragraphs.empty
    }

    def "a white-space only line separates paragraphs"() {
        when:
        def doc = parser.parse '''
para 1. sentence 1.2

para 2. sentence 2.2
'''

        then:
        doc.paragraphs.size() == 2
        doc.paragraphs[0].text == '''para 1. sentence 1.2
'''
        doc.paragraphs[1].text == '''para 2. sentence 2.2
'''
    }

    def "a paragraph can span multiple lines"() {

        when:
        def doc = parser.parse '''
sentence 1.1
    sentence 1.2
sentence 1.3

'''

        then:
        doc.paragraphs.size() == 1
        doc.paragraphs[0].text == '''sentence 1.1
    sentence 1.2
sentence 1.3
'''
    }

    def "last paragraph does not need trailing end-of-line"() {
        when:
        def doc = parser.parse '''para 1. sentence 1.2'''

        then:
        doc.paragraphs.size() == 1
        doc.paragraphs[0].text == 'para 1. sentence 1.2'
    }

    def "can parse from Reader"() {
        when:
        def doc = parser.parse new StringReader('''para 1. sentence 1.2''')

        then:
        doc.paragraphs.size() == 1
        doc.paragraphs[0].text == 'para 1. sentence 1.2'
    }
}
