package net.rubygrapefruit.docs.theme

import net.rubygrapefruit.docs.model.BuildableDocument
import net.rubygrapefruit.docs.renderer.RenderableDocument
import net.rubygrapefruit.docs.renderer.TitleBlock
import spock.lang.Specification

class MultipleChunkBuilderTest extends Specification {
    final MultipleChunkBuilder builder = new MultipleChunkBuilder()
    final BuildableDocument source = new BuildableDocument()
    final RenderableDocument doc = new RenderableDocument()

    def "returns empty chunk for document with no contents"() {
        when:
        builder.buildDocument(source, doc)

        then:
        doc.contents.size() == 1
        doc.contents[0].contents.empty
    }

    def "adds a title page for the document"() {
        given:
        source.id = 'doc'
        source.title.append("a document")

        when:
        builder.buildDocument(source, doc)

        then:
        doc.contents.size() == 1
        doc.contents[0].id == 'doc'
        doc.contents[0].contents.size() == 1
        doc.contents[0].contents[0] instanceof TitleBlock
        doc.contents[0].contents[0].component == source
    }

    def "adds a new chunk for each top level component in the document"() {
        given:
        source.title.append("a book")
        def section = source.addSection()
        section.id = 'section'
        section.title.append("a section")
        def chapter = source.addChapter()
        chapter.id = 'chapter'
        chapter.title.append("a chapter")
        def appendix = source.addAppendix()
        appendix.id = 'appendix'
        appendix.title.append("appendix")

        when:
        builder.buildDocument(source, doc)

        then:
        doc.contents.size() == 4
        doc.contents[1].id == 'section'
        doc.contents[1].contents == [section]
        doc.contents[2].id == 'chapter'
        doc.contents[2].contents == [chapter]
        doc.contents[3].id == 'appendix'
        doc.contents[3].contents == [appendix]
    }

    def "adds a title page for each part"() {
        given:
        source.title.append("book")
        def part1 = source.addPart()
        part1.id = 'part1'
        part1.title.append("part 1")
        def part2 = source.addPart()
        part2.id = 'part2'
        part2.title.append("part 2")

        when:
        builder.buildDocument(source, doc)

        then:
        doc.contents.size() == 3
        doc.contents[1].id == 'part1'
        doc.contents[1].contents[0].component == part1
        doc.contents[1].contents[0].component == part1
        doc.contents[2].id == 'part2'
        doc.contents[2].contents[0] instanceof TitleBlock
        doc.contents[2].contents[0].component == part2
    }

    def "adds a new chunk for each top level component in a part"() {
        given:
        source.title.append("book")
        def part1 = source.addPart()
        part1.title.append("part 1")
        def chapter1 = part1.addChapter()
        def chapter2 = part1.addChapter()
        def part2 = source.addPart()
        part2.title.append("part 2")

        when:
        builder.buildDocument(source, doc)

        then:
        doc.contents.size() == 5
        doc.contents[0].contents[0].component == source
        doc.contents[1].contents[0].component == part1
        doc.contents[2].contents == [chapter1]
        doc.contents[3].contents == [chapter2]
        doc.contents[4].contents[0].component == part2
    }

    def "does not add a title page when the document does not have a title"() {
        given:
        def chapter = source.addChapter()
        chapter.addParagraph().append("some content")

        when:
        builder.buildDocument(source, doc)

        then:
        doc.contents.size() == 1
        doc.contents[0].contents == [chapter]
    }

    def "does not add a title page for a part that does not have a title"() {
        given:
        def part1 = source.addPart()
        def chapter1 = part1.addChapter()
        def chapter2 = part1.addChapter()

        when:
        builder.buildDocument(source, doc)

        then:
        doc.contents.size() == 2
        doc.contents[0].contents == [chapter1]
        doc.contents[1].contents == [chapter2]
    }

    def "groups non-component blocks on a separate chunk"() {
        given:
        def unknown1 = source.addError("unknown1")
        def para1 = source.addParagraph()
        def chapter = source.addChapter()
        chapter.id = 'chapter1'
        def para2 = source.addParagraph()
        def unknown2 = source.addError("unknown4")

        when:
        builder.buildDocument(source, doc)

        then:
        doc.contents.size() == 3
        doc.contents[0].id == 'page1'
        doc.contents[0].contents == [unknown1, para1]
        doc.contents[1].id == 'chapter1'
        doc.contents[1].contents == [chapter]
        doc.contents[2].id == 'page3'
        doc.contents[2].contents == [para2, unknown2]
    }
}
