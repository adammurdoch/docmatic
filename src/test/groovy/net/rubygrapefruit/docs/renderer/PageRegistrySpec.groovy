package net.rubygrapefruit.docs.renderer

import spock.lang.Specification

class PageRegistrySpec extends Specification {
    def outputFile = new File("output.html")
    def doc = new RenderableDocument()

    def "calculates output file for front page"() {
        def frontPage = doc.addChunk()
        def registry = new PageRegistry(doc, outputFile)

        expect:
        registry.getPageFor(frontPage).file == outputFile
    }

    def "calculates output file for first page relative to the front page"() {
        doc.addChunk()
        def firstPage = doc.addChunk()
        def registry = new PageRegistry(doc, outputFile)

        expect:
        registry.getPageFor(firstPage).file == new File("output.html.content/page1.html")
    }

    def "calculates output file for subsequent page relative to the front page"() {
        doc.addChunk()
        doc.addChunk()
        def secondPage = doc.addChunk()
        def registry = new PageRegistry(doc, outputFile)

        expect:
        registry.getPageFor(secondPage).file == new File("output.html.content/page2.html")
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
        doc.addChunk()
        def registry = new PageRegistry(doc, outputFile)

        expect:
        def page = registry.getPageFor(frontPage)
        page.homeUrl == null
        page.previousUrl == null
        page.nextUrl == "output.html.content/page1.html"
    }

    def "calculates urls for first page"() {
        doc.addChunk()
        def firstPage = doc.addChunk()
        doc.addChunk()
        def registry = new PageRegistry(doc, outputFile)

        expect:
        def page = registry.getPageFor(firstPage)
        page.homeUrl == "../output.html"
        page.previousUrl == "../output.html"
        page.nextUrl == "page2.html"
    }

    def "calculates urls for subsequent page"() {
        doc.addChunk()
        doc.addChunk()
        def secondPage = doc.addChunk()
        doc.addChunk()
        def registry = new PageRegistry(doc, outputFile)

        expect:
        def page = registry.getPageFor(secondPage)
        page.homeUrl == "../output.html"
        page.previousUrl == "page1.html"
        page.nextUrl == "page3.html"
    }

    def "calculates urls for last page"() {
        doc.addChunk()
        doc.addChunk()
        def lastPage = doc.addChunk()
        def registry = new PageRegistry(doc, outputFile)

        expect:
        def page = registry.getPageFor(lastPage)
        page.homeUrl == "../output.html"
        page.previousUrl == "page1.html"
        page.nextUrl == null
    }

}
