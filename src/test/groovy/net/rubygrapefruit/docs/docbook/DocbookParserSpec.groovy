package net.rubygrapefruit.docs.docbook

import spock.lang.Specification
import net.rubygrapefruit.docs.model.Paragraph

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
        def paras = doc.getContents(Paragraph)
        paras.size() == 2
        paras[0].text == 'para 1'
        paras[1].text == 'para 2'
    }
}
