package net.rubygrapefruit.docs.model

import spock.lang.Specification

class BuildableDocumentSpec extends Specification {
    final BuildableDocument doc = new BuildableDocument()

    def "uses assigned id for components when specified"() {
        def chapter = doc.addChapter()
        def section = chapter.addSection()

        doc.id = 'book'
        chapter.id = 'chapter'
        section.id = 'section'

        when:
        doc.finish()

        then:
        doc.id == 'book'
        chapter.id == 'chapter'
        section.id == 'section'
    }

    def "uses title as id for components when no id specified"() {
        def chapter = doc.addChapter()
        def section = chapter.addSection()

        doc.title.append('a simple book')
        chapter.title.append('a chapter')
        section.title.append('section ')
        section.title.addCode().append('with code')

        when:
        doc.finish()

        then:
        doc.id == 'a_simple_book'
        chapter.id == 'a_chapter'
        section.id == 'section_with_code'
    }

    def "uses type as id for components when no id or title specified"() {
        def chapter1 = doc.addChapter()
        def chapter2 = doc.addChapter()
        def chapter3 = doc.addChapter()
        def chapter4 = doc.addChapter()

        chapter2.id = 'second_chapter'
        chapter3.title.append('the third chapter')

        when:
        doc.finish()

        then:
        doc.id == 'book1'
        chapter1.id == 'chapter1'
        chapter2.id == 'second_chapter'
        chapter3.id == 'the_third_chapter'
        chapter4.id == 'chapter4'
    }

    def "qualifies ids generated from title when they overlap with assigned ids"() {
        def chapter1 = doc.addChapter()
        def chapter2 = doc.addChapter()
        def chapter3 = doc.addChapter()

        chapter1.title.append('chapter')
        chapter2.id = 'chapter'
        chapter3.id = 'chapter_1'

        when:
        doc.finish()

        then:
        chapter1.id == 'chapter_2'
        chapter2.id == 'chapter'
        chapter3.id == 'chapter_1'
    }

    def "qualifies id generated from type when they overlap with assigned ids"() {
        def chapter1 = doc.addChapter()
        def chapter2 = doc.addChapter()

        chapter2.id = 'chapter1'

        when:
        doc.finish()

        then:
        chapter1.id == 'chapter1_1'
        chapter2.id == 'chapter1'
    }
    
    def "resolves links"() {
        LinkResolver resolver = Mock()
        LinkTarget target = Mock()
        
        def para = doc.addParagraph()
        def xref = para.addCrossReference(resolver)

        when:
        doc.finish()

        then:
        xref.target == target

        and:
        1 * resolver.resolve() >> target
    }
}
