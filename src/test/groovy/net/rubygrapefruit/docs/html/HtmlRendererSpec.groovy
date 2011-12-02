package net.rubygrapefruit.docs.html

import net.rubygrapefruit.docs.docbook.DocbookParser
import net.rubygrapefruit.docs.markdown.MarkdownParser
import net.rubygrapefruit.docs.model.Document
import net.rubygrapefruit.docs.renderer.DefaultTheme
import spock.lang.Specification

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

    def "renders document title"() {
        given:
        def doc = docbook '''
<book>
    <title>title</title>
</book>
'''

        expect:
        render doc contains '''<body>
<h1>title</h1>
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

    def "renders the top-level sections of a document"() {
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

    def "renders the nested sections of a document"() {
        given:
        def doc = docbook '''
<book>
<chapter>
    <title>chapter 1</title>
    <section>
        <title>section 2</title>
        <section>
            <title>section 3</title>
            <section>
                <title>section 4</title>
            </section>
        </section>
    </section>
</chapter>
</book>
'''

        expect:
        render doc contains '''<body>
<h1>chapter 1</h1>
<h2>section 2</h2>
<h3>section 3</h3>
<h4>section 4</h4>
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

    def "renders the contents of an ordered list"() {
        given:
        def doc = document '''
1. para 1
1. para 2
1. para 3
'''

        expect:
        render doc contains '''<body>
<ol>
<li>
<p>para 1</p>
</li>
<li>
<p>para 2</p>
</li>
<li>
<p>para 3</p>
</li>
</ol>
</body>
'''
    }

    def "renders code inlines"() {
        given:
        def doc = document '''para 1 `code`

`code` para 2
'''

        expect:
        render doc contains '''<body>
<p>para 1 <code>code</code></p>
<p><code>code</code> para 2</p>
</body>
'''
    }

    def document(String text) {
        return new MarkdownParser().parse(text, "document.md")
    }

    def docbook(String text) {
        return new DocbookParser().parse(text, "document.xml")
    }

    def render(Document document) {
        def outstr = new ByteArrayOutputStream()
        renderer.render(document, new DefaultTheme(), outstr)
        return new String(outstr.toByteArray(), "utf-8")
    }
}
