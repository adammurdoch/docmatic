package net.rubygrapefruit.docs.renderer

import spock.lang.Specification
import net.rubygrapefruit.docs.model.buildable.BuildableDocument

class PageRegistrySpec extends Specification {
    def outputFile = new File("output.html")
    def doc = new RenderableDocument()

    def "calculates output file for front page"() {
        def frontPage = doc.addChunk()
        def registry = new PageRegistry(doc, outputFile)

        expect:
        registry.getPageFor(frontPage).file == outputFile
    }

    def "calculates output file for subsequent pages relative to the front page"() {
        doc.addChunk()
        def firstPage = doc.addChunk()
        firstPage.id = 'first_page'
        def registry = new PageRegistry(doc, outputFile)

        expect:
        registry.getPageFor(firstPage).file == new File("output.html.content/first_page.html")
    }

    def "calculates urls for single page document"() {
        def frontPage = doc.addChunk()
        def registry = new PageRegistry(doc, outputFile)

        expect:
        def page = registry.getPageFor(frontPage)
        page.homeUrl == null
        page.previousUrl == null
        page.nextUrl == null
    }

    def "calculates urls for front page"() {
        def frontPage = doc.addChunk()
        doc.addChunk().id = 'second_page'
        def registry = new PageRegistry(doc, outputFile)

        expect:
        def page = registry.getPageFor(frontPage)
        page.homeUrl == null
        page.previousUrl == null
        page.nextUrl == "output.html.content/second_page.html"
    }

    def "calculates urls for first page"() {
        doc.addChunk()
        def firstPage = doc.addChunk()
        doc.addChunk().id = 'next_page'
        def registry = new PageRegistry(doc, outputFile)

        expect:
        def page = registry.getPageFor(firstPage)
        page.homeUrl == "../output.html"
        page.previousUrl == "../output.html"
        page.nextUrl == "next_page.html"
    }

    def "calculates urls for subsequent page"() {
        doc.addChunk()
        doc.addChunk().id = "previous_page"
        def secondPage = doc.addChunk()
        doc.addChunk().id = "next_page"
        def registry = new PageRegistry(doc, outputFile)

        expect:
        def page = registry.getPageFor(secondPage)
        page.homeUrl == "../output.html"
        page.previousUrl == "previous_page.html"
        page.nextUrl == "next_page.html"
    }

    def "calculates urls for last page"() {
        doc.addChunk()
        doc.addChunk().id = "previous_page"
        def lastPage = doc.addChunk()
        def registry = new PageRegistry(doc, outputFile)

        expect:
        def page = registry.getPageFor(lastPage)
        page.homeUrl == "../output.html"
        page.previousUrl == "previous_page.html"
        page.nextUrl == null
    }

    def "locates page for target element"() {
        BuildableDocument sourceDoc = new BuildableDocument()
        def chapter = sourceDoc.addChapter()
        def section = chapter.addSection()
        doc.addChunk()
        def chunk = doc.addChunk()
        chunk.add(chapter)
        def registry = new PageRegistry(doc, outputFile)

        expect:
        def page = registry.getPageFor(section)
        page == registry.getPageFor(chunk)
    }

    def "calculates relative url from one page to another"() {
        def chunk1 = doc.addChunk()
        chunk1.id = 'page1'
        def chunk2 = doc.addChunk()
        chunk2.id = 'page2'
        def chunk3 = doc.addChunk()
        chunk3.id = 'page3'
        def registry = new PageRegistry(doc, outputFile)
        def page1 = registry.getPageFor(chunk1)
        def page2 = registry.getPageFor(chunk2)
        def page3 = registry.getPageFor(chunk3)

        expect:
        page2.getUrlTo(page1) == "../output.html"
        page1.getUrlTo(page2) == "output.html.content/page2.html"
        page2.getUrlTo(page3) == "page3.html"
        page3.getUrlTo(page2) == "page2.html"

        page1.getUrlTo(page1) == null
        page2.getUrlTo(page2) == null
    }
}
