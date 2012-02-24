package net.rubygrapefruit.docs.html

import net.rubygrapefruit.docs.docbook.DocbookParser
import net.rubygrapefruit.docs.markdown.MarkdownParser
import net.rubygrapefruit.docs.model.Document
import net.rubygrapefruit.docs.theme.DefaultTheme
import net.rubygrapefruit.docs.theme.MinimalTheme
import net.rubygrapefruit.docs.theme.SingleChunkBuilder
import net.rubygrapefruit.docs.theme.Theme
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class HtmlRendererSpec extends Specification {
    @Rule final TemporaryFolder tmpDir = new TemporaryFolder()
    final HtmlRenderer renderer = new HtmlRenderer()

    def "renders an empty document"() {
        given:
        def doc = document '''
'''

        expect:
        rendered doc contains '''<body>
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
        rendered doc contains '''<body>
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
        rendered doc contains '''<body>
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
        rendered doc contains '''<body>
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
        rendered doc contains '''<body>
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
        rendered doc contains '''<body>
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
        rendered doc contains '''<body>
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
        rendered doc contains '''<body>
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
        def doc = docbook '''<book><chapter>
<para>
    <code>code</code>
    <literal>literal</literal>
</para>
</chapter></book>'''

        expect:
        rendered doc contains '''<body>
<p><code class="code">code</code> <code class="literal">literal</code></p>
</body>
'''
    }

    def "renders emphasis inlines"() {
        given:
        def doc = docbook '''<book><chapter>
<para>
    <emphasis>emphasis</emphasis>
</para>
</chapter></book>'''

        expect:
        rendered doc contains '''<body>
<p><em>emphasis</em></p>
</body>
'''
    }
    
    def "applies style rules from html theme"() {
        HtmlTheme theme = Mock()

        given:
        def doc = document 'content'

        and:
        _ * theme.documentBuilder >> new SingleChunkBuilder()
        _ * theme.writeStyleRules(!null) >> {Appendable target -> target.append('style-rule') }

        expect:
        rendered(doc, theme) contains 'style-rule'
    }

    def "renders each document chunk to a separate page"() {
        def outFile = new File(tmpDir.root, "out.html")
        
        given:
        def doc = docbook("<book><chapter><title>chapter 1</title></chapter><chapter><title>chapter 2</title></chapter></book>")
        
        when:
        rendered(doc, outFile, new DefaultTheme())
        
        then:
        outFile.file
        new File(tmpDir.root, "out.html.content/page1.html").file
    }

    def "renders navigation links for a page"() {
        def outFile = new File(tmpDir.root, "out.html")
        
        given:
        def doc = docbook("<book><chapter><title>chapter 1</title></chapter><chapter><title>chapter 2</title></chapter></book>")
        
        when:
        rendered(doc, outFile, new DefaultTheme())
        
        then:
        outFile.text.contains '''<div class="navbar header"><a href="out.html.content/page1.html" class="nextlink">Next</a></div>'''
        outFile.text.contains '''<div class="navbar footer"><a href="out.html.content/page1.html" class="nextlink">Next</a></div>'''
        def page1 = new File(tmpDir.root, "out.html.content/page1.html")
        page1.text.contains '''<div class="navbar header"><a href="../out.html" class="previouslink">Previous</a><a href="../out.html" class="homelink">Home</a></div>'''
        page1.text.contains '''<div class="navbar footer"><a href="../out.html" class="previouslink">Previous</a><a href="../out.html" class="homelink">Home</a></div>'''
    }

    def document(String text) {
        return new MarkdownParser().parse(text, "document.md")
    }

    def docbook(String text) {
        return new DocbookParser().parse(text, "document.xml")
    }

    def rendered(Document document, Theme theme = new MinimalTheme()) {
        def out = new File(tmpDir.root, "out.html")
        renderer.render(document, theme, out)
        assert out.file
        return out.text
    }

    def rendered(Document document, File outputDir, Theme theme = new MinimalTheme()) {
        renderer.render(document, theme, outputDir)
    }
}
