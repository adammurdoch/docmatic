package net.rubygrapefruit.docs.markdown

import net.rubygrapefruit.docs.model.Paragraph
import spock.lang.Specification

class MarkdownParserSpec extends Specification {
    final MarkdownParser parser = new MarkdownParser()

    def "an empty string converts to an empty document"() {
        when:
        def doc = parse ""

        then:
        def paras = doc.getContents(Paragraph)
        paras.empty
    }

    def "an whitespace only string converts to an empty document"() {
        when:
        def doc = parse "  \n  \r\n  "

        then:
        def paras = doc.getContents(Paragraph)
        paras.empty
    }

    def "a white-space only line separates paragraphs"() {
        when:
        def doc = parse '''
para 1. sentence 1.2

para 2. sentence 2.2
'''

        then:
        def paras = doc.getContents(Paragraph)
        paras.size() == 2
        paras[0].text == 'para 1. sentence 1.2'
        paras[1].text == 'para 2. sentence 2.2'
    }

    def "a paragraph can span multiple lines"() {

        when:
        def doc = parse '''
sentence 1.1
    sentence 1.2
sentence 1.3
sentence 1.4
'''

        then:
        def paras = doc.getContents(Paragraph)
        paras.size() == 1
        paras[0].text == '''sentence 1.1 sentence 1.2 sentence 1.3 sentence 1.4'''
    }

    def "last paragraph does not need trailing end-of-line"() {
        when:
        def doc = parse '''para 1. sentence 1.2'''

        then:
        def paras = doc.getContents(Paragraph)
        paras.size() == 1
        paras[0].text == 'para 1. sentence 1.2'
    }

    def "underline with equals creates a level 1 section"() {
        when:
        def doc = parse '''
title
=====
para 1. sentence 1.2

para 2

title 2
=======
'''

        then:
        doc.contents.size() == 2

        doc.contents[0].title == 'title'
        doc.contents[0].contents.size() == 2
        doc.contents[0].contents[0].text == 'para 1. sentence 1.2'
        doc.contents[0].contents[1].text == 'para 2'

        doc.contents[1].title == 'title 2'
        doc.contents[1].contents.empty
    }

    def "underline with dash creates a level 2 section"() {
        when:
        def doc = parse '''
title
=====

section 2
-------
para 1. sentence 1.2

para 2

title 2
=======
'''

        then:
        doc.contents.size() == 2

        doc.contents[0].title == 'title'
        doc.contents[0].contents.size() == 1
        doc.contents[0].contents[0].title == 'section 2'
        doc.contents[0].contents[0].contents.size() == 2
        doc.contents[0].contents[0].contents[0].text == 'para 1. sentence 1.2'
        doc.contents[0].contents[0].contents[1].text == 'para 2'

        doc.contents[1].title == 'title 2'
        doc.contents[1].contents.empty
    }

    def "can have equals and dash characters inside heading"() {
        when:
        def doc = parse '''
===
===

---
===

a-b
===
'''

        then:
        doc.contents.size() == 3
        doc.contents[0].title == '==='
        doc.contents[1].title == '---'
        doc.contents[2].title == 'a-b'
    }

    def "can have equals and dash characters inside paragraph"() {
        when:
        def doc = parse '''
para == -- para

para
--- ===
para

===
para

---
para

a ====

para
== --
'''

        then:
        doc.contents.size() == 6
        doc.contents[0].text == 'para == -- para'
        doc.contents[1].text == 'para --- === para'
        doc.contents[2].text == '=== para'
        doc.contents[3].text == '--- para'
        doc.contents[4].text == 'a ===='
        doc.contents[5].text == 'para == --'
    }

    def "can have equals and dash characters at end of paragraph"() {
        when:
        def doc = parse '''
para ==

para
para
====

para
para
----

---

====
'''

        then:
        doc.contents.size() == 5
        doc.contents[0].text == 'para =='
        doc.contents[1].text == 'para para ===='
        doc.contents[2].text == 'para para ----'
        doc.contents[3].text == '---'
        doc.contents[4].text == '===='
    }

    def "can parse from Reader"() {
        when:
        def doc = parse new StringReader('''para 1. sentence 1.2''')

        then:
        def paras = doc.getContents(Paragraph)
        paras.size() == 1
        paras[0].text == 'para 1. sentence 1.2'
    }

    def parse(def text) {
        return parser.parse(text, "document.md")
    }
}
