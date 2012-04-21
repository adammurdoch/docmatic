package net.rubygrapefruit.docs.docbook

import spock.lang.Specification
import net.rubygrapefruit.docs.model.*

class DocbookParserSpec extends Specification {
    final DocbookParser parser = new DocbookParser()

    def "converts book title"() {
        when:
        def doc = parse '''
<book>
    <title>some book</title>
</book>'''

        then:
        doc.title.text == 'some book'
    }

    def "converts bookinfo to book title (4.5)"() {
        when:
        def doc = parse '''
<book>
    <bookinfo>
        <title>some book</title>
    </bookinfo>
</book>'''

        then:
        doc.title.text == 'some book'
    }

    def "converts part elements to part"() {
        when:
        def doc = parse '''
<book>
    <part>
        <title>part 1</title>
    </part>
    <part>
        <title>part 2</title>
    </part>
</book>'''

        then:
        doc.contents.size() == 2

        doc.contents[0] instanceof Part
        doc.contents[0].title.text == 'part 1'

        doc.contents[1] instanceof Part
        doc.contents[1].title.text == 'part 2'
    }

    def "converts chapter elements to chapters"() {
        when:
        def doc = parse '''
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

        doc.contents[0] instanceof Chapter
        doc.contents[0].title.text == 'chapter 1'

        doc.contents[1] instanceof Chapter
        doc.contents[1].title.text == 'chapter 2'
    }

    def "converts appendix elements to appendix"() {
        when:
        def doc = parse '''
<book>
    <appendix>
        <title>appendix 1</title>
    </appendix>
    <appendix>
        <title>appendix 2</title>
    </appendix>
</book>'''

        then:
        doc.contents.size() == 2

        doc.contents[0] instanceof Appendix
        doc.contents[0].title.text == 'appendix 1'

        doc.contents[1] instanceof Appendix
        doc.contents[1].title.text == 'appendix 2'
    }

    def "converts section elements to sections"() {
        when:
        def doc = parse '''
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

        doc.contents[0].title.text == 'chapter 1'
        doc.contents[0].contents.size() == 1
        doc.contents[0].contents[0] instanceof Section
        doc.contents[0].contents[0].title.text == 'section 1'

        doc.contents[1].title.text == 'chapter 2'
        doc.contents[1].contents.size() == 1
        doc.contents[1].contents[0] instanceof Section
        doc.contents[1].contents[0].title.text == 'section 2'
    }

    def "builds component structure"() {
        when:
        def doc = parse '''
<book>
    <part>
        <title>part 1</title>
        <chapter>
            <title>chapter 1</title>
            <section>
                <title>section 1</title>
                <section><title>section 1.1</title></section>
            </section>
        </chapter>
    </part>
</book>'''

        then:

        doc.contents[0] instanceof Part
        doc.contents[0].title.text == 'part 1'

        doc.contents[0].contents[0] instanceof Chapter
        doc.contents[0].contents[0].title.text == 'chapter 1'

        doc.contents[0].contents[0].contents[0] instanceof Section
        doc.contents[0].contents[0].contents[0].title.text == 'section 1'

        doc.contents[0].contents[0].contents[0].contents[0] instanceof Section
        doc.contents[0].contents[0].contents[0].contents[0].title.text == 'section 1.1'
    }

    def "attaches specified ids to component elements"() {
        when:
        def doc = parse '''
<book id="book">
    <part id="part"/>
    <chapter id="chapter">
        <section id="section"/>
    </chapter>
    <appendix id="appendix"/>
</book>'''

        then:

        doc.id == 'book'
        doc.contents[0].id == 'part'
        doc.contents[1].id == 'chapter'
        doc.contents[1].contents[0].id == 'section'
        doc.contents[2].id == 'appendix'
    }

    def "normalises text in titles"() {
        when:
        def doc = parse '''
<book>
    <chapter>
        <title>
chapter
1
</title>
    </chapter>
</book>'''

        then:
        doc.contents[0].title.text == 'chapter 1'
    }

    def "converts para elements to paragraphs"() {
        when:
        def doc = parse '''
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

    def "normalises text in paragraphs"() {
        when:
        def doc = parse '''
<book>
    <chapter>
        <para>
    para
    1
</para>
    </chapter>
</book>'''

        then:
        doc.contents[0].contents[0].text == 'para 1'
    }

    def "converts inline elements"() {
        when:
        def doc = parse '''
<book>
    <chapter>
        <para><code>code</code><literal>literal</literal><emphasis>emphasis</emphasis></para>
    </chapter>
</book>'''

        then:
        doc.contents[0].contents[0].contents.size() == 3
        doc.contents[0].contents[0].contents[0] instanceof Code
        doc.contents[0].contents[0].contents[0].text == 'code'
        doc.contents[0].contents[0].contents[1] instanceof Literal
        doc.contents[0].contents[0].contents[1].text == 'literal'
        doc.contents[0].contents[0].contents[2] instanceof Emphasis
        doc.contents[0].contents[0].contents[2].text == 'emphasis'
    }

    def "converts a mix of text and inline elements"() {
        when:
        def doc = parse '''
<book>
    <chapter>
        <para>
            text
            <code>code</code>
            <literal>literal</literal>
            text</para>
    </chapter>
</book>'''

        then:
        def para = doc.contents[0].contents[0]
        para.contents.size() == 5
        para.contents[0] instanceof Text
        para.contents[0].text == 'text '
        para.contents[1] instanceof Code
        para.contents[1].text == 'code'
        para.contents[2] instanceof Text
        para.contents[2].text == ' '
        para.contents[3] instanceof Literal
        para.contents[3].text == 'literal'
        para.contents[4] instanceof Text
        para.contents[4].text == ' text'
    }

    def "normalises whitespace in para with inline elements"() {
        when:
        def doc = parse '''
<book>
    <chapter>
        <para>
            <code>code</code>
            text
            text
            <literal>literal</literal>
            text
        </para>
    </chapter>
</book>'''

        then:
        def para = doc.contents[0].contents[0]
        para.contents.size() == 4
        para.contents[0] instanceof Code
        para.contents[0].text == 'code'
        para.contents[1] instanceof Text
        para.contents[1].text == ' text text '
        para.contents[2] instanceof Literal
        para.contents[2].text == 'literal'
        para.contents[3] instanceof Text
        para.contents[3].text == ' text'
    }

    def "normalises nested whitespace in inline elements in para"() {
        when:
        def doc = parse '''
<book>
    <chapter>
        <para>
            <code>  code  </code>
            text
            text<literal>
                literal
            </literal>text
        </para>
    </chapter>
</book>'''

        then:
        def para = doc.contents[0].contents[0]
        para.contents.size() == 4
        para.contents[0] instanceof Code
        para.contents[0].text == 'code'
        para.contents[1] instanceof Text
        para.contents[1].text == ' text text'
        para.contents[2] instanceof Literal
        para.contents[2].text == ' literal '
        para.contents[3] instanceof Text
        para.contents[3].text == 'text'
    }

    def "converts itemizedlist elements to itemized list"() {
        when:
        def doc = parse '''
<book>
    <chapter>
        <itemizedlist>
            <listitem><para>item 1</para></listitem>
            <listitem><para>item 2</para></listitem>
        </itemizedlist>
    </chapter>
</book>'''

        then:
        doc.contents[0].contents.size() == 1
        doc.contents[0].contents[0] instanceof ItemisedList
        doc.contents[0].contents[0].items.size() == 2
        doc.contents[0].contents[0].items[0].contents[0].text == 'item 1'
        doc.contents[0].contents[0].items[1].contents[0].text == 'item 2'
    }

    def "converts orderedlist elements to ordered list"() {
        when:
        def doc = parse '''
<book>
    <chapter>
        <orderedlist>
            <listitem><para>item 1</para></listitem>
            <listitem><para>item 2</para></listitem>
        </orderedlist>
    </chapter>
</book>'''

        then:
        doc.contents[0].contents.size() == 1
        doc.contents[0].contents[0] instanceof OrderedList
        doc.contents[0].contents[0].items.size() == 2
        doc.contents[0].contents[0].items[0].contents[0].text == 'item 1'
        doc.contents[0].contents[0].items[1].contents[0].text == 'item 2'
    }

    def "converts xref elements"() {
        when:
        def doc = parse '''
<book>
    <chapter>
        <para><xref linkend="chapter2"/></para>
    </chapter>
    <chapter id="chapter2"><title>chapter 2</title></chapter>
</book>'''

        then:
        def para = doc.contents[0].contents[0]
        para.contents.size() == 1
        para.contents[0] instanceof CrossReference
        para.contents[0].target == doc.contents[1]
        para.contents[0].text == 'chapter 2'
    }

    def "converts link elements"() {
        when:
        def doc = parse '''
<book xmlns:xlink="http://www.w3.org/1999/xlink">
    <chapter>
        <para>
            <link linkend="chapter2">another chapter</link>
            <link xlink:href="http://somehost/">some link</link>
        </para>
    </chapter>
    <chapter id="chapter2"/>
</book>'''

        then:
        def para = doc.contents[0].contents[0]
        para.contents.size() == 2
        para.contents[0] instanceof CrossReference
        para.contents[0].target == doc.contents[1]
        para.contents[0].text == 'another chapter'
        para.contents[1] instanceof Link
        para.contents[1].target == new URI("http://somehost/")
        para.contents[1].text == 'some link'
    }

    def "converts ulink elements"() {
        when:
        def doc = parse '''
<book>
    <chapter>
        <para>
            <ulink url="http://somehost/">some link</ulink>
            <ulink url="http://somehost/"/>
        </para>
    </chapter>
</book>'''

        then:
        def para = doc.contents[0].contents[0]
        para.contents.size() == 2
        para.contents[0] instanceof Link
        para.contents[0].target == new URI("http://somehost/")
        para.contents[0].text == 'some link'
        para.contents[1] instanceof Link
        para.contents[1].target == new URI("http://somehost/")
        para.contents[1].text == 'http://somehost/'
    }

    def "converts unexpected elements and text"() {
        when:
        def doc = parse '''
<book>
    <para>para is not allowed here</para>
    unexpected
    <chapter>
        <para><unexpected-inline/></para>
    </chapter>
</book>'''

        then:
        doc.contents.size() == 3
        doc.contents[0].message == '<para>book.xml, line 3, column 11</para>'
        doc.contents[1].message == '(text book.xml, line 5, column 5)'
        doc.contents[2].contents[0].contents[0].message == '<unexpected-inline>book.xml, line 6, column 35</unexpected-inline>'
    }

    def "converts badly formed links"() {
        when:
        def doc = parse '''
<book xmlns:xlink="http://www.w3.org/1999/xlink">
    <chapter>
        <para>
            <xref/>
            <xref linkend="unknown"/>
            <link/>
            <link linkend="unknown"/>
            <link linkend="12" xlink:xhref="12"/>
            <ulink/>
        </para>
    </chapter>
</book>'''

        then:
        def para = doc.contents[0].contents[0]
        para instanceof Paragraph
        para.contents[0].message == '<xref>no "linkend" attribute specified in book.xml, line 4, column 22</xref>'
        para.contents[1].message == '<xref>unknown linkend "unknown" in book.xml, line 4, column 47</xref>'
        para.contents[2].message == '<link>no "linkend" or "href" attribute specified in book.xml, line 4, column 22</link>'
        para.contents[3].message == '<link>unknown linkend "unknown" in book.xml, line 4, column 47</link>'
        para.contents[4].message == '<link>both "linkend" and "href" attribute specified in book.xml, line 4, column 47</link>'
        para.contents[5].message == '<ulink>no "url" attribute specified in book.xml, line 4, column 22</ulink>'
    }

    def parse(String string) {
        return parser.parse(string, "book.xml")
    }
}
