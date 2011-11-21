
What is it?
===========
A JVM based toolkit for generating documentation.

Why?
----
Existing tool-chains for the JVM are unwieldy, slow and hard to customise.

Supported Inputs
================
Can parse a subset of markdown and DocBook.

Markdown
--------
Supported syntax:

* Paragraphs - text only
* Setext style headers - text only

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
Can generate HTML and PDF output. No customisation is available yet.
