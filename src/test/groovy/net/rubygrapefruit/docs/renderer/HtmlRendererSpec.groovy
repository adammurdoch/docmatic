package net.rubygrapefruit.docs.renderer

import net.rubygrapefruit.docs.markdown.MarkdownParser
import net.rubygrapefruit.docs.model.Document
import spock.lang.Specification

class HtmlRendererSpec extends Specification {
    final HtmlRenderer renderer = new HtmlRenderer()

    def "renders an empty document"() {
        given:
        def doc = document '''
'''

        expect:
        render(doc) == '''<html>
<body>
</body>
</html>
'''
    }
    
    def "renders the paragraphs of a document"() {
        given:
        def doc = document '''para 1.

para 2.
'''

        expect:
        render(doc) == '''<html>
<body>
<p>para 1.</p>
<p>para 2.</p>
</body>
</html>
'''
    }

    def "renders the sections of a document"() {
        given:
        def doc = document '''
section 1
=========

section 2
---------
'''

        expect:
        render(doc) == '''<html>
<body>
<h1>section 1</h1>
<h2>section 2</h2>
</body>
</html>
'''
    }

    def "renders the contents of the sections of a document"() {
        given:
        def doc = document '''
section 1
=========
para 1

para 2

section 2
---------
para 3
'''

        expect:
        render(doc) == '''<html>
<body>
<h1>section 1</h1>
<p>para 1</p>
<p>para 2</p>
<h2>section 2</h2>
<p>para 3</p>
</body>
</html>
'''
    }

    def document(String text) {
        return new MarkdownParser().parse(text)
    }
    
    def render(Document document) {
        def outstr = new ByteArrayOutputStream()
        renderer.render(document, outstr)
        return new String(outstr.toByteArray(), "utf-8")
    }
}
