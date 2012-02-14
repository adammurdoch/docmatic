package net.rubygrapefruit.docs.theme

import net.rubygrapefruit.docs.model.BuildableDocument
import net.rubygrapefruit.docs.renderer.RenderableDocument
import net.rubygrapefruit.docs.renderer.TitleBlock
import spock.lang.Specification

class MultipleChunkBuilderTest extends Specification {
    final MultipleChunkBuilder builder = new MultipleChunkBuilder()
    final BuildableDocument source = new BuildableDocument()
    final RenderableDocument doc = new RenderableDocument()

    def "adds a title page for the document"() {
        given:
        source.getTitle().append("a document")

        when:
        builder.buildDocument(source, doc)

        then:
        doc.contents.size() == 1
        doc.contents[0].id == null
        doc.contents[0].contents.size() == 1
        doc.contents[0].contents[0] instanceof TitleBlock
        doc.contents[0].contents[0].component == source
    }

    def "adds a new chunk for each top level component in the document"() {
        given:
        source.getTitle().append("a book")
        def section = source.addSection()
        section.getTitle().append("a section")
        def chapter = source.addChapter()
        chapter.getTitle().append("a chapter")
        def appendix = source.addAppendix()
        appendix.getTitle().append("appendix")

        when:
        builder.buildDocument(source, doc)

        then:
        doc.contents.size() == 4
        doc.contents[1].contents == [section]
        doc.contents[2].contents == [chapter]
        doc.contents[3].contents == [appendix]
    }

    def "adds a title page for each part"() {
        given:
        source.getTitle().append("book")
        def part1 = source.addPart()
        part1.getTitle().append("part 1")
        def part2 = source.addPart()
        part2.getTitle().append("part 2")

        when:
        builder.buildDocument(source, doc)

        then:
        doc.contents.size() == 3
        doc.contents[1].contents[0] instanceof TitleBlock
        doc.contents[1].contents[0].component == part1
        doc.contents[2].contents[0] instanceof TitleBlock
        doc.contents[2].contents[0].component == part2
    }

    def "adds a new chunk for each top level component in a part"() {
        given:
        source.getTitle().append("book")
        def part1 = source.addPart()
        part1.getTitle().append("part 1")
        def chapter1 = part1.addChapter()
        def chapter2 = part1.addChapter()
        def part2 = source.addPart()
        part2.getTitle().append("part 2")

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

    def "includes unknown blocks in the current chunk"() {
        given:
        def unknown1 = source.addUnknown("unknown1")
        def unknown2 = source.addUnknown("unknown2")
        def chapter = source.addChapter()
        def unknown3 = source.addUnknown("unknown3")
        def unknown4 = source.addUnknown("unknown4")

        when:
        builder.buildDocument(source, doc)

        then:
        doc.contents.size() == 2
        doc.contents[0].contents == [unknown1, unknown2]
        doc.contents[1].contents == [chapter, unknown3, unknown4]
    }
}
