
What is it?
===========
A JVM based toolkit for generating documentation.

Why?
----
Existing tool-chains for the JVM are unwieldy, slow and hard to customise.

Supported Inputs
================
Can parse a subset of markdown and DocBook. You can also use the API to build a document programmatically.

Markdown
--------
Supported syntax:

* Paragraphs - text only
* Setext style headers - text only
* Unordered lists - text only

DocBook
-------
Supported elements:

* book > chapter
* chapter > title, para
* section > title, para, itemizedlist
* title > text only
* para > text only
* itemizedlist > listitem
* listitem > para, itemizedlist

Supported Outputs
=================
Can generate HTML 4 and PDF output. There are some built-in themes that offer some degree of customisation.
You can also customise via the API, by implementing your own output format or theme.

Usage
=====

docmatic [--pdf] [--html] [--minimal] [--default] --out <output-dir> <input-files>*

Output formats:

* --pdf
* --html

Themes:

* --minimal
* --default
