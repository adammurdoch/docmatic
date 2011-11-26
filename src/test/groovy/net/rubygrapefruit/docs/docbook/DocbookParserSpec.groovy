package net.rubygrapefruit.docs.docbook

import net.rubygrapefruit.docs.model.ItemisedList
import net.rubygrapefruit.docs.model.OrderedList
import spock.lang.Specification

class DocbookParserSpec extends Specification {
    final DocbookParser parser = new DocbookParser()

    def "converts chapter elements to sections"() {
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
        doc.contents[0].title.text == 'chapter 1'
        doc.contents[1].title.text == 'chapter 2'
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
        doc.contents[0].contents[0].title.text == 'section 1'

        doc.contents[1].title.text == 'chapter 2'
        doc.contents[1].contents.size() == 1
        doc.contents[1].contents[0].title.text == 'section 2'
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

    def "converts unexpected elements and text"() {
        when:
        def doc = parse '''
<book>
    <para>para is not allowed here</para>
    unexpected
    <section><para><unexpected-inline/></para></section>
</book>'''

        then:
        doc.contents.size() == 3

        doc.contents[0].message == '<para>book.xml, line 3, column 11</para>'

        doc.contents[1].message == '(text book.xml, line 5, column 5)'

        doc.contents[2].contents[0].contents[0].message == '<unexpected-inline>book.xml, line 6, column 1</unexpected-inline>'
    }

    def parse(String string) {
        return parser.parse(string, "book.xml")
    }
}
