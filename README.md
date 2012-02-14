
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

* Paragraphs
* Setext style headers
* Unordered and ordered lists - paragraphs only (no nested lists, headers, etc).
* `` `code` ``
* `_emphasis_`

DocBook 4.5 and 5
-----------------
Supported elements:

* `<book>` - `<title>`, `<part>`, `<chapter>`, `<appendix>` only
* `<part>` - `<title>`, `<chapter>`, `<appendix>` only.
* `<chapter>`, `<appendix>`, `<section>` - `<title>`, `<section>`, `<para>`, `<itemizedlist>`, `<orderedlist>` only
* `<title>`, `<para>` - text, `<code>`, `<literal>`, `<emphasis>` only
* `<itemizedlist>`, `<orderedlist>` - `<listitem>` only
* `<listitem>` - `<para>`, `<itemizedlist>`, `<orderedlist>` only

Supported 4.5 elements:

* `<book>` - `<bookinfo>`
* `<bookinfo>` - `<title>` only

Supported Outputs
=================
Can generate HTML 4 and PDF output. There are some built-in themes that offer some degree of customisation.
You can also customise via the API, by implementing your own output format or theme.

Usage
=====
`docmatic <output-formats> <themes> --out <output-dir> <input-files>*`

Output formats
--------------
* `--pdf`

    Generates PDF.

* `--html`

    Generates HTML 4.

Themes
------

* `--minimal`

    Generates minimal styling and markup.

* `--default`

    Some lightweight styling.

* `--fixed-width`

    Fixed page width (HTML only).

API Usage
=========
Use a `Parser` implementation to build a `Document`. Use one or more `Renderer` implementations to generate output
from that `Document`.

Known Issues
============
* Exception rendering a markdown document that does not start with a header.
* Fonts are not correct for inline elements in headers in PDFs.
