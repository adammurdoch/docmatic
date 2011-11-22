package net.rubygrapefruit.docs.html

import net.rubygrapefruit.docs.markdown.MarkdownParser
import net.rubygrapefruit.docs.model.Document
import spock.lang.Specification
import net.rubygrapefruit.docs.html.HtmlRenderer
import net.rubygrapefruit.docs.renderer.DefaultTheme

class HtmlRendererSpec extends Specification {
    final HtmlRenderer renderer = new HtmlRenderer()

    def "renders an empty document"() {
        given:
        def doc = document '''
'''

        expect:
        render doc contains '''<body>
</body>
'''
    }
    
    def "renders the paragraphs of a document"() {
        given:
        def doc = document '''para 1.

para 2.
'''

        expect:
        render doc contains '''<body>
<p>para 1.</p>
<p>para 2.</p>
</body>
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
        render doc contains '''<body>
<h1>section 1</h1>
<h2>section 2</h2>
</body>
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
        render doc contains '''<body>
<h1>section 1</h1>
<p>para 1</p>
<p>para 2</p>
<h2>section 2</h2>
<p>para 3</p>
</body>
'''
    }

    def "renders the contents of an itemised list"() {
        given:
        def doc = document '''
* para 1
* para 2
* para 3
'''

        expect:
        render doc contains '''<body>
<ul>
<li>
<p>para 1</p>
</li>
<li>
<p>para 2</p>
</li>
<li>
<p>para 3</p>
</li>
</ul>
</body>
'''
    }

    def document(String text) {
        return new MarkdownParser().parse(text, "document.md")
    }
    
    def render(Document document) {
        def outstr = new ByteArrayOutputStream()
        renderer.render(document, new DefaultTheme(), outstr)
        return new String(outstr.toByteArray(), "utf-8")
    }
}
