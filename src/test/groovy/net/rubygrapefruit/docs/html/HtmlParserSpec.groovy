package net.rubygrapefruit.docs.html

import spock.lang.Specification
import net.rubygrapefruit.docs.model.Paragraph
import net.rubygrapefruit.docs.model.Error

class HtmlParserSpec extends Specification {
    final HtmlParser parser = new HtmlParser()

    def "parses whitespace-only document"() {
        when:
        def doc = parse '''
    \t\f \n \r \r\n\t
'''
        then:
        doc.contents.size() == 0
    }

    def "parses doctype only documment"() {
        when:
        def doc = parse '<!doctype html>'
        then:
        doc.contents.size() == 0
    }

    def "parses document with empty <html> element"() {
        when:
        def doc = parse '<!doctype html><html></html>'
        then:
        doc.contents.size() == 0
    }

    def "parses document with empty <body> element"() {
        when:
        def doc = parse '<html><body></body></html>'
        then:
        doc.contents.size() == 0
    }

    def "converts <p> elements to paragraphs"() {
        when:
        def doc = parse '''
<html>
<body>
    <p>some text</p>
    <p>other text</p>
</body>
</html>
'''
        then:
        doc.contents.size() == 2
        doc.contents[0] instanceof Paragraph
        doc.contents[0].text == 'some text'
        doc.contents[1] instanceof Paragraph
        doc.contents[1].text == 'other text'
    }

    def "adds implicit paragraph for text inside <html> element"() {
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

    def "allows missing doctype declaration"() {
        when:
        def doc = parse '''
<html>
<body>
    <p>some text</p>
</body>
</html>
'''
        then:
        doc.contents.size() == 1
        doc.contents[0] instanceof Paragraph
        doc.contents[0].text == 'some text'
    }

    def "allows missing <html> element"() {
        when:
        def doc = parse '''
    some text
    <p>other text
'''
        then:
        doc.contents.size() == 2
        doc.contents[0] instanceof Paragraph
        doc.contents[0].text == 'some text'
        doc.contents[1] instanceof Paragraph
        doc.contents[1].text == 'other text'
    }

    def "allows missing <body> element"() {
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

    def "handles comments"() {
        when:
        def doc = parse '''
<!-- comment 1 -->
<!doctype html>
<!-- comment 2 -->
<html>
    <!-- a comment -->
<!--
this is another comment.
<p>this should be ignored.</p>
-->
    <p>some <!-- ignored: </p><p> --> text</p>
    <!-- -- -> -->
</html>
<!-- a trailing comment -->
'''
        then:
        doc.contents.size() == 1
        doc.contents[0] instanceof Paragraph
        doc.contents[0].text == 'some text'
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

    def "handles whitespace in doctype declaration"() {
        when:
        def doc = parse '''
<!doctype   html   >
<html>
    <p>some text</p>
</html>
'''
        then:
        doc.contents.size() == 1
        doc.contents[0] instanceof Paragraph
        doc.contents[0].text == 'some text'
    }

    def "doctype declaration is case-insensitive"() {
        when:
        def doc = parse '''<!doCTYpe HtmL>
<html>
    <p>some text</p>
</html>
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

    def "does not allow text outside of <html> element when doctype has been declared"() {
        when:
        def doc = parse '''
    some text
<!doctype html>
some text
<html>
</html>
some text
'''
        then:
        doc.contents.size() == 3
        doc.contents[0] instanceof Error
        doc.contents[0].message == 'unexpected text in doc.html, line 2, column 5.'
        doc.contents[1] instanceof Error
        doc.contents[1].message == 'unexpected text in doc.html, line 4, column 1.'
        doc.contents[2] instanceof Error
        doc.contents[2].message == 'unexpected text in doc.html, line 7, column 1.'
    }

    def "does not allow text outside of <html> element when present"() {
        when:
        def doc = parse '''
some text
<html>
</html>
some text
'''
        then:
        doc.contents.size() == 2
        doc.contents[0] instanceof Error
        doc.contents[0].message == 'unexpected text in doc.html, line 2, column 1.'
        doc.contents[1] instanceof Error
        doc.contents[1].message == 'unexpected text in doc.html, line 5, column 1.'
    }

    def "does not allow multiple <html> elements"() {
        when:
        def doc = parse '''
<html>
</html>
<html></html>
'''
        then:
        doc.contents.size() == 1
        doc.contents[0] instanceof Error
        doc.contents[0].message == '??'
    }

    def "handles unterminated comment"() {
        expect: false
    }

    def "handles badly-formed doctype"() {
        expect: false
    }

    def parse(String string) {
        return parser.parse(string, "doc.html")
    }
}
