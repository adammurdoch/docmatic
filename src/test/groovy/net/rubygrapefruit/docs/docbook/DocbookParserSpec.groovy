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
        <para><code>code</code></para>
        <para><literal>literal</literal></para>
        <para><emphasis>emphasis</emphasis></para>
        <para><classname>class-name</classname></para>
    </chapter>
</book>'''

        then:
        def chapter = doc.contents[0]
        chapter.contents[0].contents.size() == 1
        chapter.contents[0].contents[0] instanceof Code
        chapter.contents[0].contents[0].text == 'code'
        chapter.contents[1].contents.size() == 1
        chapter.contents[1].contents[0] instanceof Literal
        chapter.contents[1].contents[0].text == 'literal'
        chapter.contents[2].contents.size() == 1
        chapter.contents[2].contents[0] instanceof Emphasis
        chapter.contents[2].contents[0].text == 'emphasis'
        chapter.contents[3].contents.size() == 1
        chapter.contents[3].contents[0] instanceof ClassName
        chapter.contents[3].contents[0].text == 'class-name'
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
        para.contents[2].text == 'literal'
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
            <link linkend="chapter2"><classname>A Class</classname></link>
        </para>
    </chapter>
    <chapter id="chapter2"/>
</book>'''

        then:
        def para = doc.contents[0].contents[0]
        para.contents.size() == 5
        para.contents[0] instanceof CrossReference
        para.contents[0].target == doc.contents[1]
        para.contents[0].text == 'another chapter'
        para.contents[2] instanceof Link
        para.contents[2].target == new URI("http://somehost/")
        para.contents[2].text == 'some link'
        para.contents[4] instanceof CrossReference
        para.contents[4].target == doc.contents[1]
        para.contents[4].contents.size() == 1
        para.contents[4].contents[0] instanceof ClassName
        para.contents[4].contents[0].text == 'A Class'
    }

    def "converts ulink elements"() {
        when:
        def doc = parse '''
<book>
    <chapter>
        <para>
            <ulink url="http://somehost/">some link</ulink>
            <ulink url="http://somehost/"/>
            <ulink url="http://somehost/"><classname>A Class</classname></ulink>
        </para>
    </chapter>
</book>'''

        then:
        def para = doc.contents[0].contents[0]
        para.contents.size() == 5
        para.contents[0] instanceof Link
        para.contents[0].target == new URI("http://somehost/")
        para.contents[0].text == 'some link'
        para.contents[2] instanceof Link
        para.contents[2].target == new URI("http://somehost/")
        para.contents[2].text == 'http://somehost/'
        para.contents[4] instanceof Link
        para.contents[4].target == new URI("http://somehost/")
        para.contents[4].contents.size() == 1
        para.contents[4].contents[0] instanceof ClassName
        para.contents[4].contents[0].text == 'A Class'
    }

    def "converts programlisting elements"() {
        when:
        def doc = parse '''
<book>
    <chapter>
        <programlisting>
a  bc
    d e f

        </programlisting>
    </chapter>
</book>'''

        then:
        def listing = doc.contents[0].contents[0]
        listing instanceof ProgramListing
        listing.text == '''a  bc
    d e f'''
    }

    def "converts example elements"() {
        when:
        def doc = parse '''
<book>
    <chapter>
        <example>
            <title>an example</title>
            <para>some content</para>
        </example>
    </chapter>
</book>'''

        then:
        def example = doc.contents[0].contents[0]
        example instanceof Example
        example.title.text == 'an example'
        example.contents.size() == 1
        example.contents[0].text == 'some content'
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
            <link linkend="12" xlink:href="12"/>
            <ulink/>
        </para>
    </chapter>
</book>'''

        then:
        def para = doc.contents[0].contents[0]
        para instanceof Paragraph
        def contents = para.contents.findAll { it instanceof Error }
        contents[0].message == '<xref>no "linkend" attribute specified in book.xml, line 5, column 20</xref>'
        contents[1].message == '<xref>unknown linkend "unknown" in book.xml, line 6, column 38</xref>'
        contents[2].message == '<link>no "linkend" or "href" attribute specified in book.xml, line 7, column 20</link>'
        contents[3].message == '<link>unknown linkend "unknown" in book.xml, line 8, column 38</link>'
        contents[4].message == '<link>both "linkend" and "href" attribute specified in book.xml, line 9, column 49</link>'
        contents[5].message == '<ulink>no "url" attribute specified in book.xml, line 10, column 21</ulink>'
    }

    def parse(String string) {
        return parser.parse(string, "book.xml")
    }
}
