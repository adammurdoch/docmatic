
What is it?
===========
Docmatic is a JVM based toolkit for generating documentation. It accepts as input any combination of Markdown or
DocBook, and generates HTML 4 or PDF output.

Why?
----
Existing tool-chains for the JVM are unwieldy, slow and hard to customise.

Supported Inputs
================
Can parse a subset of Markdown and DocBook 4.5 and 5. You can also use the API to build a document programmatically.

Note that Docmatic is a work-in-progress, so not everything is supported.

Markdown
--------
Supported syntax:

* Paragraphs - text only
* Setext style headers - text only
* Unordered and ordered lists - paragraphs only (no nested lists, headers, etc).

DocBook 4.5 and 5
-----------------
Supported elements:

* book > title, part, chapter, appendix
* part > title, chapter, appendix
* chapter, appendix, section > title, section, para, itemizedlist, orderedlist
* title > text only
* para > text only
* itemizedlist > listitem
* orderedlist > listitem
* listitem > para, itemizedlist, orderedlist

Supported 4.5 elements:

* book > bookinfo
* bookinfo > title

Supported Outputs
=================
Can generate HTML 4 and PDF output. There are some built-in themes that offer some degree of customisation.
You can also customise via the API, by implementing your own output format or theme.

Usage
=====
`docmatic [--pdf] [--html] [--minimal] [--default] --out <output-dir> <input-files>*`

Output formats:

* `--pdf`
* `--html`

Themes:

* `--minimal`
* `--default`
