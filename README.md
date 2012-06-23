
What is it?
===========
Docmatic is a JVM based toolkit for generating documentation. It accepts as input any combination of Markdown,
DocBook or HTML, and generates HTML or PDF output.

Why?
----
* Existing tool-chains for the JVM are unwieldy, slow and hard to customise. Docmatic is small, fast and easy to install.
* Docbook has a comprehensive model for documentation, but is awkward to use for authoring documents. Markdown, on the
other hand, is nice for authoring, but the resulting documents lack a lot of useful structural information. Docmatic
is an experiment in allowing you to build documentation from these two formats, using the strengths of each as
appropriate.

Features
--------
* Generate single or multi-paged HTML.
* Themes.
* Java API, to allow you to programmatically build, transform, theme, or render a document.

Supported Inputs
================
Docmatic can parse a subset of Markdown, HTML 4.01 and 5, and DocBook 4.5 and 5. You can also use the API to build a
document programmatically.

Note that Docmatic is a very early work-in-progress, so not much of these input formats is supported yet.

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
Supported DocBook 5 elements:

* `<book>`, with `<title>`, `<part>`, `<chapter>`, `<appendix>` child elements only.
* `<part>`, with `<title>`, `<chapter>`, `<appendix>` child elements only.
* `<chapter>`, `<appendix>`, `<section>`, with `<title>`, `<section>`, `<para>`, `<itemizedlist>`, `<orderedlist>` child elements only.
* `<title>`, `<para>`, with text and `<classname>`, `<code>`, `<literal>`, `<emphasis>`, `<xref>`, `<link>` child elements only.
* `<itemizedlist>`, `<orderedlist>`, with `<listitem>` child elements only.
* `<listitem>`, with `<para>`, `<itemizedlist>`, `<orderedlist>` child elements only.
* `<classname>`, `<code>`, `<literal>`, `<emphasis>`, with text only.
* `<xref>`, `<link>`, with `<classname>`, `<code>`, `<literal>`, `<emphasis>` child elements and text only.

Supported DocBook 4.5 elements:

* `<book>`, with `<bookinfo>` child element.
* `<bookinfo>`, with `<title>` child element only.
* `<ulink>`, with `<classname>`, `<code>`, `<literal>`, `<emphasis>` child elements and text only.

You can mix DocBook 5 and DocBook 4.5 elements in the same document.

HTML 4.01 and 5
---------------
Supported HTML elements:

* `<html>`, with `<body>`, `<p>` child elements only. Also allows text.
* `<body>`, with `<p>` child elements only. Also allows text.
* `<p>` elements with, text only.

The HTML parser is fairly lenient. You can leave out the doctype declaration, `<html>` and `<body>` elements. Any text
outside a block element is wrapped in an implicit `<p>` element.

Supported Outputs
=================
Can generate HTML 4 and PDF output. There are some built-in themes that offer some degree of customisation.
You can also customise via the API, by implementing your own output renderer or theme.

Building from source
====================

Run `./gradlew installApp` to build an installation in `build/install/docmatic`.

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

Input files
-----------
Docmatic guesses the type of each input file based on its extension:

* `.xml` is treated as a DocBook file.
* `.html` is treated as an HTML file.
* Everything else is treated as a Markdown file.

API Usage
=========
Use a `Parser` implementation to build a `Document`. Use one or more `Renderer` implementations to generate output
from that `Document`. Use a `Theme` implementation to tweak the output.

License
=======
Docmatic is licensed under the ASL 2.0.

Known Issues
============

* Paragraph fonts are used for inline elements in PDF headers.
* Docbook `<xref/>` elements can refer to `<book>`, `<part>`, `<chapter>`, `<appendix>` and `<section>` elements only.
* Cannot parse an HTML 4 doctype declaration.
