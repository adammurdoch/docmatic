package net.rubygrapefruit.docs.docbook

import spock.lang.Specification

class DocbookParserSpec extends Specification {
    final DocbookParser parser = new DocbookParser()

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
        doc.paragraphs.size() == 2
        doc.paragraphs[0].text == 'para 1'
        doc.paragraphs[1].text == 'para 2'
    }

}
