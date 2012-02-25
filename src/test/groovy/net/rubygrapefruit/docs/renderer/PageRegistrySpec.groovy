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

}
