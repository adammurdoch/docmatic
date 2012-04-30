package net.rubygrapefruit.docs.html

import spock.lang.Specification
import net.rubygrapefruit.docs.model.Paragraph

class HtmlParserSpec extends Specification {
    final HtmlParser parser = new HtmlParser()

    def "converts p elements to Paragraphs"() {
        when:
        def doc = parse '''
<html>
    <p>some text</p>
    <p>other text</p>
</html>
'''
        then:
        doc.contents.size() == 2
        doc.contents[0] instanceof Paragraph
        doc.contents[0].text == 'some text'
        doc.contents[1] instanceof Paragraph
        doc.contents[1].text == 'other text'
    }

    def "adds implicit paragraph for text"() {
        when:
        def doc = parse '''
<html>
    some text
    <p>other text
</html>
'''
        then:
        doc.contents.size() == 2
        doc.contents[0] instanceof Paragraph
        doc.contents[0].text == 'some text'
        doc.contents[1] instanceof Paragraph
        doc.contents[1].text == 'other text'
    }

    def "handles whitespace in start and end tags"() {
        when:
        def doc = parse '''
<html>
    <p >some text</p  >
    <p
    >other text</  p
    >
</html>
'''
        then:
        doc.contents.size() == 2
        doc.contents[0] instanceof Paragraph
        doc.contents[0].text == 'some text'
        doc.contents[1] instanceof Paragraph
        doc.contents[1].text == 'other text'
    }

    def "handles badly formed start and end tags"() {
        when:
        def doc = parse '''
<html>
    <  p ><<some text< /p
</html>
'''
        then:
        doc.contents.size() == 1
        doc.contents[0] instanceof Paragraph
        doc.contents[0].text == '< p ><<some text< /p'
    }

    def "element names are case-insensitive"() {
        when:
        def doc = parse '''
<HTML>
    <P>some text</P>
</HTML>
'''
        then:
        doc.contents.size() == 1
        doc.contents[0] instanceof Paragraph
        doc.contents[0].text == 'some text'
    }

    def "converts unknown block elements to Error elements"() {
        when:
        def doc = parse '''
<html>
    <unknown>some text</unknown>
    <p>other text</p>
</html>
'''
        then:
        doc.contents.size() == 2
        doc.contents[0] instanceof Error
        doc.contents[0].message == '??'
        doc.contents[1] instanceof Paragraph
        doc.contents[1].text == 'other text'
    }

    def "converts unknown inline elements to Error elements"() {
        when:
        def doc = parse '''
<html>
    <p><unknown></p>
</html>
'''
        then:
        doc.contents.size() == 1
        doc.contents[0] instanceof Paragraph
        doc.contents[0].contents.size() == 1
        doc.contents[0].contents[0] instanceof Error
        doc.contents[0].contents[0].message == '??'
    }

    def "converts unknown text to Error elements"() {
        when:
        def doc = parse '''
<html>
    some text
</html>
'''
        then:
        doc.contents.size() == 1
        doc.contents[0] instanceof Error
        doc.contents[0].message == '??'
    }

    def parse(String string) {
        return parser.parse(string, "doc.html")
    }

}
