package net.rubygrapefruit.docs.markdown

import spock.lang.Specification
import net.rubygrapefruit.docs.model.*
import spock.lang.Ignore

class MarkdownParserSpec extends Specification {
    final MarkdownParser parser = new MarkdownParser()

    def "an empty string converts to an empty document"() {
        when:
        def doc = parse ""

        then:
        def paras = doc.getContents(Paragraph)
        paras.empty
    }

    def "a whitespace only string converts to an empty document"() {
        when:
        def doc = parse "  \n  \r\n  "

        then:
        def paras = doc.getContents(Paragraph)
        paras.empty
    }

    def "a white-space only line separates paragraphs"() {
        when:
        def doc = parse '''
para 1. sentence 1.2

para 2. sentence 2.2
'''

        then:
        def paras = doc.getContents(Paragraph)
        paras.size() == 2
        paras[0].text == 'para 1. sentence 1.2'
        paras[1].text == 'para 2. sentence 2.2'
    }

    def "a paragraph can span multiple lines"() {

        when:
        def doc = parse '''
sentence 1.1
    sentence 1.2
sentence 1.3
 sentence 1.4
\tsentence 1.5
'''

        then:
        def paras = doc.getContents(Paragraph)
        paras.size() == 1
        paras[0].text == '''sentence 1.1 sentence 1.2 sentence 1.3 sentence 1.4 sentence 1.5'''
    }

    def "normalises whitespace in paragraph"() {

        when:
        def doc = parse '''
sentence 1.1\t    \tsentence 1.2
sentence       1.3\t\t\t
'''

        then:
        def paras = doc.getContents(Paragraph)
        paras.size() == 1
        paras[0].text == '''sentence 1.1 sentence 1.2 sentence 1.3'''
    }

    def "last paragraph does not need trailing end-of-line"() {
        when:
        def doc = parse '''para 1. sentence 1.2'''

        then:
        def paras = doc.getContents(Paragraph)
        paras.size() == 1
        paras[0].text == 'para 1. sentence 1.2'
    }

    def "underline with equals creates a level 1 section"() {
        when:
        def doc = parse '''
title
=====
para 1. sentence 1.2

para 2

title 2
=======
'''

        then:
        doc.contents.size() == 2

        doc.contents[0].title.text == 'title'
        doc.contents[0].contents.size() == 2
        doc.contents[0].contents[0].text == 'para 1. sentence 1.2'
        doc.contents[0].contents[1].text == 'para 2'

        doc.contents[1].title.text == 'title 2'
        doc.contents[1].contents.empty
    }

    def "underline with dash creates a level 2 section"() {
        when:
        def doc = parse '''
title
=====

section 2
-------
para 1. sentence 1.2

para 2

title 2
=======
'''

        then:
        doc.contents.size() == 2

        doc.contents[0].title.text == 'title'
        doc.contents[0].contents.size() == 1
        doc.contents[0].contents[0].title.text == 'section 2'
        doc.contents[0].contents[0].contents.size() == 2
        doc.contents[0].contents[0].contents[0].text == 'para 1. sentence 1.2'
        doc.contents[0].contents[0].contents[1].text == 'para 2'

        doc.contents[1].title.text == 'title 2'
        doc.contents[1].contents.empty
    }

    def "can have equals and dash characters inside heading"() {
        when:
        def doc = parse '''
===
===

---
===

a-b
===
'''

        then:
        doc.contents.size() == 3
        doc.contents[0].title.text == '==='
        doc.contents[1].title.text == '---'
        doc.contents[2].title.text == 'a-b'
    }

    def "can have equals and dash characters inside paragraph"() {
        when:
        def doc = parse '''
para == -- para

para
--- ===
para

===
para

---
para

a ====

para
== --
'''

        then:
        doc.contents.size() == 6
        doc.contents[0].text == 'para == -- para'
        doc.contents[1].text == 'para --- === para'
        doc.contents[2].text == '=== para'
        doc.contents[3].text == '--- para'
        doc.contents[4].text == 'a ===='
        doc.contents[5].text == 'para == --'
    }

    def "can have equals and dash characters at end of paragraph"() {
        when:
        def doc = parse '''
para ==

para
para
====

para
para
----

---

====
'''

        then:
        doc.contents.size() == 5
        doc.contents[0].text == 'para =='
        doc.contents[1].text == 'para para ===='
        doc.contents[2].text == 'para para ----'
        doc.contents[3].text == '---'
        doc.contents[4].text == '===='
    }

    def "a leading * defines an itemised list item"() {
        when:
        def doc = parse '''
* item 1
* item 2
* item 3
'''

        then:
        doc.contents.size() == 1
        doc.contents[0] instanceof ItemisedList
        doc.contents[0].items.size() == 3
        doc.contents[0].items[0].contents[0].text == 'item 1'
        doc.contents[0].items[1].contents[0].text == 'item 2'
        doc.contents[0].items[2].contents[0].text == 'item 3'
    }

    def "a list item can span multiple lines"() {
        when:
        def doc = parse '''
* item 1
line 1.2
* item 2
    line 2.2
'''

        then:
        doc.contents.size() == 1
        doc.contents[0].items.size() == 2
        doc.contents[0].items[0].contents[0].text == 'item 1 line 1.2'
        doc.contents[0].items[1].contents[0].text == 'item 2 line 2.2'
    }

    def "can separate list items with blank lines"() {
        when:
        def doc = parse '''
* item 1
line 1.2

* item 2

\t\t\t

* item 3
'''

        then:
        doc.contents.size() == 1
        doc.contents[0].items.size() == 3
        doc.contents[0].items[0].contents[0].text == 'item 1 line 1.2'
        doc.contents[0].items[1].contents[0].text == 'item 2'
        doc.contents[0].items[2].contents[0].text == 'item 3'
    }

    def "a list item can have multiple paragraphs"() {
        when:
        def doc = parse '''
* item 1
line 1.2

    para 2
line 2.2

\tpara 3
    line 3.3

\t \t    para 4
* item 2

   not a para
'''

        then:
        doc.contents.size() == 2
        doc.contents[0].items.size() == 2
        doc.contents[0].items[0].contents.size() == 4
        doc.contents[0].items[0].contents[0].text == 'item 1 line 1.2'
        doc.contents[0].items[0].contents[1].text == 'para 2 line 2.2'
        doc.contents[0].items[0].contents[2].text == 'para 3 line 3.3'
        doc.contents[0].items[0].contents[3].text == 'para 4'
        doc.contents[0].items[1].contents[0].text == 'item 2'
        doc.contents[1].text == 'not a para'
    }

    def "a paragraph between list items starts a new list"() {
        when:
        def doc = parse '''
* item 1

   not a para

* item 2

'''

        then:
        doc.contents.size() == 3
        doc.contents[0].items.size() == 1
        doc.contents[0].items[0].contents[0].text == 'item 1'
        doc.contents[1].text == 'not a para'
        doc.contents[2].items[0].contents[0].text == 'item 2'
    }

    def "a paragraph can contain list item marker characters"() {
        when:
        def doc = parse '''
*

*not an item

not * an item

not
* an item
'''

        then:
        doc.contents.size() == 4
        doc.contents[0].text == '*'
        doc.contents[1].text == '*not an item'
        doc.contents[2].text == 'not * an item'
        doc.contents[3].text == 'not * an item'
    }

    def "list item can contain list item marker characters"() {
        when:
        def doc = parse '''
* - item
+ item + item
- -+*
* *
*no whitespace
'''

        then:
        doc.contents.size() == 1
        doc.contents[0].items.size() == 4
        doc.contents[0].items[0].contents[0].text == '- item'
        doc.contents[0].items[1].contents[0].text == 'item + item'
        doc.contents[0].items[2].contents[0].text == '-+*'
        doc.contents[0].items[3].contents[0].text == '* *no whitespace'
    }

    def "header takes precedence over list item"() {
        when:
        def doc = parse '''
* item
-

- item
-----
para
'''

        then:
        doc.contents.size() == 1
        doc.contents[0].contents.size() == 2
        doc.contents[0].contents[0].title.text == '* item'
        doc.contents[0].contents[1].title.text == '- item'
        doc.contents[0].contents[1].contents[0].text == 'para'
    }

    def "a leading digits and . defines an ordered list item"() {
        when:
        def doc = parse '''
1. item 1
1. item 2
78. item 3
'''

        then:
        doc.contents.size() == 1
        doc.contents[0] instanceof OrderedList
        doc.contents[0].items.size() == 3
        doc.contents[0].items[0].contents[0].text == 'item 1'
        doc.contents[0].items[1].contents[0].text == 'item 2'
        doc.contents[0].items[2].contents[0].text == 'item 3'
    }

    def "can interleave list types"() {
        when:
        def doc = parse '''
- item 1
+ item 2
* item 3
1. item 1
2. item 2
- item 1
'''

        then:
        doc.contents.size() == 3
        doc.contents[0].items.size() == 3
        doc.contents[0].items[0].contents[0].text == 'item 1'
        doc.contents[0].items[1].contents[0].text == 'item 2'
        doc.contents[0].items[2].contents[0].text == 'item 3'
        doc.contents[1].items.size() == 2
        doc.contents[1].items[0].contents[0].text == 'item 1'
        doc.contents[1].items[1].contents[0].text == 'item 2'
        doc.contents[2].items.size() == 1
        doc.contents[2].items[0].contents[0].text == 'item 1'
    }

    @Ignore
    def "paragraphs can contain ordered list item marker"() {
        expect: false
    }

    def "a series of ` delimits code inline"() {
        when:
        def doc = parse '''
`some code`

this is `some code` and some text

a`b

a``b

`  `
'''

        then:
        doc.contents[0].contents[0] instanceof Code
        doc.contents[0].contents[0].text == 'some code'

        doc.contents[1].contents[0] instanceof Text
        doc.contents[1].contents[0].text == 'this is '
        doc.contents[1].contents[1] instanceof Code
        doc.contents[1].contents[1].text == 'some code'
        doc.contents[1].contents[2] instanceof Text
        doc.contents[1].contents[2].text == ' and some text'

        doc.contents[2].contents[0] instanceof Text
        doc.contents[2].contents[0].text == 'a`b'

        doc.contents[3].contents[0] instanceof Text
        doc.contents[3].contents[0].text == 'a``b'

        doc.contents[4].contents[0] instanceof Code
        doc.contents[4].contents[0].text == ''
    }

    def "code is terminated by equal number of ` characters"() {
        when:
        def doc = parse '''
`some code`` some code`

``some code`some code``

```some code`some code

`some code```some code

```some code```some code```
        '''

        then:
        doc.contents[0].contents[0] instanceof Code
        doc.contents[0].contents[0].text == 'some code``some code'

        doc.contents[1].contents[0] instanceof Code
        doc.contents[1].contents[0].text == 'some code`some code'

        doc.contents[2].contents[0] instanceof Code
        doc.contents[2].contents[0].text == '``some code'
        doc.contents[2].contents[1] instanceof Text
        doc.contents[2].contents[1].text == 'some code'

        doc.contents[3].contents[0] instanceof Text
        doc.contents[3].contents[0].text == '`some code```some code'

        doc.contents[4].contents[0] instanceof Code
        doc.contents[4].contents[0].text == 'some code'
        doc.contents[4].contents[1] instanceof Text
        doc.contents[4].contents[1].text == 'some code```'
    }

    def "code inline can span multiple lines"() {
        when:
        def doc = parse '''
` some code
line 2
line 3 `
        '''

        then:
        doc.contents[0].contents[0] instanceof Code
        doc.contents[0].contents[0].text == 'some code line 2 line 3'
    }

    def "normalises whitespace in code inline"() {
        when:
        def doc = parse '''
`  some code
   line   2 \t \t
line   3  `

` a b `` c d `
        '''

        then:
        doc.contents[0].contents[0] instanceof Code
        doc.contents[0].contents[0].text == 'some code line 2 line 3'

        doc.contents[1].contents[0] instanceof Code
        doc.contents[1].contents[0].text == 'a b `` c d'
    }

    @Ignore
    def "code inline does not contain other inlines"() {
        expect: false
    }

    def "_ or * character delimits emphasis inline"() {
        when:
        def doc = parse '''
_some text_

*some text*

_ text _

_text*text

_text__

_ __

__ **
        '''

        then:
        doc.contents[0].contents[0] instanceof Emphasis
        doc.contents[0].contents[0].text == 'some text'

        doc.contents[1].contents[0] instanceof Emphasis
        doc.contents[1].contents[0].text == 'some text'

        doc.contents[2].contents[0] instanceof Text
        doc.contents[2].contents[0].text == '_ text _'

        doc.contents[3].contents[0] instanceof Text
        doc.contents[3].contents[0].text == '_text*text'

        doc.contents[4].contents[0] instanceof Emphasis
        doc.contents[4].contents[0].text == 'text'
        doc.contents[4].contents[1] instanceof Text
        doc.contents[4].contents[1].text == '_'

        doc.contents[5].contents[0] instanceof Text
        doc.contents[5].contents[0].text == '_ __'

        doc.contents[6].contents[0] instanceof Text
        doc.contents[6].contents[0].text == '__ **'
    }

    @Ignore
    def "two _ or * characters delimits strong inline"() {
        expect: false
    }

    @Ignore
    def "emphasis inline can contain other inlines"() {
        expect: false
    }

    @Ignore
    def "strong inline can contain other inlines"() {
        expect: false
    }

    def "headers can contain inlines"() {
        when:
        def doc = parse '''
text `code`
===========

`code`
------
        '''

        then:
        doc.contents[0].title.contents.size() == 2
        doc.contents[0].title.contents[0] instanceof Text
        doc.contents[0].title.contents[0].text == 'text '
        doc.contents[0].title.contents[1] instanceof Code
        doc.contents[0].title.contents[1].text == 'code'
        doc.contents[0].contents[0].title.contents.size() == 1
        doc.contents[0].contents[0].title.contents[0] instanceof Code
        doc.contents[0].contents[0].title.contents[0].text == 'code'
    }

    def "list items can contain inlines"() {
        when:
        def doc = parse '''
* `code`
* `code`
        '''

        then:
        doc.contents[0].items.size() == 2
        doc.contents[0].items[0].contents[0].contents[0] instanceof Code
        doc.contents[0].items[0].contents[0].contents[0].text == 'code'
        doc.contents[0].items[1].contents[0].contents[0] instanceof Code
        doc.contents[0].items[1].contents[0].contents[0].text == 'code'
    }

    def "can parse from Reader"() {
        when:
        def doc = parse new StringReader('''para 1. sentence 1.2''')

        then:
        def paras = doc.getContents(Paragraph)
        paras.size() == 1
        paras[0].text == 'para 1. sentence 1.2'
    }

    def parse(def text) {
        return parser.parse(text, "document.md")
    }
}
