package net.rubygrapefruit.docs.docbook

import spock.lang.Specification

class DocbookParserSpec extends Specification {
    final DocbookParser parser = new DocbookParser()

    def "converts chapter elements to sections"() {
        when:
        def doc = parser.parse '''
<book>
    <chapter>
        <title>chapter 1</title>
    </chapter>
    <chapter>
        <title>chapter 2</title>
    </chapter>
</book>'''

        then:
        doc.contents.size() == 2
        doc.contents[0].title == 'chapter 1'
        doc.contents[1].title == 'chapter 2'
    }

    def "converts section elements to sections"() {
        when:
        def doc = parser.parse '''
<book>
    <chapter>
        <title>chapter 1</title>
        <section><title>section 1</title></section>
    </chapter>
    <chapter>
        <title>chapter 2</title>
        <section><title>section 2</title></section>
    </chapter>
</book>'''

        then:
        doc.contents.size() == 2

        doc.contents[0].title == 'chapter 1'
        doc.contents[0].contents.size() == 1
        doc.contents[0].contents[0].title == 'section 1'

        doc.contents[1].title == 'chapter 2'
        doc.contents[1].contents.size() == 1
        doc.contents[1].contents[0].title == 'section 2'
    }

    def "converts para elements to paragraphs"() {
        when:
        def doc = parser.parse '''
<book>
    <chapter>
        <para>para 1</para>
        <para>para 2</para>
    </chapter>
</book>'''

        then:
        doc.contents[0].contents.size() == 2
        doc.contents[0].contents[0].text == 'para 1'
        doc.contents[0].contents[1].text == 'para 2'
    }

}
