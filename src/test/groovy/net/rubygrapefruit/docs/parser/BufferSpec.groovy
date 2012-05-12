package net.rubygrapefruit.docs.parser

import spock.lang.Specification

class BufferSpec extends Specification {
    def "production can match a sequence of characters"() {
        def buffer = buffer('abc')
        def production = { CharStream stream ->
            assert stream.consume('a' as char)
            assert stream.consume('b' as char)
        } as Production<CharStream>

        expect:
        buffer.consume(production)
        buffer.value == 'ab'
    }

    def "production cannot consume beyond the end of input"() {
        def buffer = buffer('a')
        def production = { CharStream stream ->
            assert stream.consume('a' as char)
            assert !stream.consume('b' as char)
        } as Production<CharStream>

        expect:
        buffer.consume(production)
        buffer.value == 'a'
    }

    def "production is not considered a match when it unwinds"() {
        def buffer = buffer('abab')
        def production = { CharStream stream ->
            stream.consume('a' as char)
            stream.consume('b' as char)
            stream.consume('c' as char)
            stream.rewind()
        } as Production<CharStream>

        expect:
        !buffer.consume(production)
        buffer.value == ''
    }

    def "production does not consume any characters when it unwinds"() {
        def buffer = buffer('abde')
        def production1 = { CharStream stream ->
            stream.consume('a' as char)
            stream.consume('b' as char)
            stream.consume('c' as char)
            stream.rewind()
        } as Production<CharStream>
        def production2 = matchAB()

        expect:
        !buffer.consume(production1)
        buffer.consume(production2)
        buffer.value == 'ab'
    }

    def "production can unwind on failed match"() {
        def buffer = buffer('abc')
        def production = { CharStream stream ->
            stream.consume('c' as char)
            stream.rewind()
            stream.consume('a' as char)
            stream.consume('b' as char)
        } as Production<CharStream>

        expect:
        buffer.consume(production)
        buffer.value == 'ab'
    }

    def "production can match a nested production"() {
        def buffer = buffer('abab')
        def nested = matchAB()
        def production = { CharStream stream ->
            assert stream.consume(nested)
            assert stream.consume(nested)
        } as Production<CharStream>

        expect:
        buffer.consume(production)
        buffer.value == 'abab'
    }

    def "value is made available for a nested production"() {
        def buffer = buffer('abcd')
        def nested1 = matchAB()
        def nested2 = matchCD()
        def production = { CharStream stream ->
            stream.consume(nested1)
            assert stream.value == 'ab'
            stream.consume(nested2)
            assert stream.value == 'cd'
        } as Production<CharStream>

        expect:
        buffer.consume(production)
        buffer.value == 'abcd'
    }

    def "location of start and end of production is made available"() {
        def buffer = buffer('ab\nabab\r\nab\rab')
        def nested1 = matchAB()
        def nested2 = matchEOL()
        def production = { CharStream stream ->
            stream.consume(nested1)
            assert stream.startColumn == 1
            assert stream.startLine == 1
            assert stream.endColumn == 2
            assert stream.endLine == 1
            stream.consume(nested2)
            stream.consume(nested1)
            assert stream.startColumn == 1
            assert stream.startLine == 2
            assert stream.endColumn == 2
            assert stream.endLine == 2
            stream.consume(nested1)
            assert stream.startColumn == 3
            assert stream.startLine == 2
            assert stream.endColumn == 4
            assert stream.endLine == 2
            stream.consume(nested2)
            stream.consume(nested1)
            assert stream.startColumn == 1
            assert stream.startLine == 3
            assert stream.endColumn == 2
            assert stream.endLine == 3
            stream.consume(nested2)
            stream.consume(nested1)
            assert stream.startColumn == 1
            assert stream.startLine == 4
            assert stream.endColumn == 2
            assert stream.endLine == 4
        } as Production<CharStream>

        expect:
        buffer.consume(production)
        buffer.startColumn == 1
        buffer.startLine == 1
        buffer.endColumn == 2
        buffer.endLine == 4
    }

    def "no characters are consumed when nested production unwinds"() {
        def buffer = buffer('abab')
        def nested1 = { CharStream stream ->
            stream.consume('a' as char)
            stream.consume('c' as char)
            stream.rewind()
        } as Production<CharStream>
        def nested2 = matchAB()
        def production = { CharStream stream ->
            assert !stream.consume(nested1)
            assert stream.consume(nested2)
        } as Production<CharStream>

        expect:
        buffer.consume(production)
        buffer.value == 'ab'
    }

    def "nested productions can consume beyond the end of the buffer"() {
        def buffer = buffer('abab', 3)
        def nested = matchAB()
        def production = { CharStream stream ->
            stream.consume(nested)
        } as Production<CharStream>

        expect:
        buffer.consume(production)
        buffer.consume(production)
        buffer.value == 'ab'
    }

    def "commit removes the most recent mark"() {
        def buffer = buffer('abcd')

        expect:
        buffer.start()
        buffer.consume('a' as char)
        buffer.start()
        buffer.consume('b' as char)
        buffer.commit()
        buffer.consume('c' as char)
        buffer.rewind()
        buffer.consume('a' as char)
    }

    def "rollback moves cursor back to most recent mark and removes the mark"() {
        def buffer = buffer('abcd')

        expect:
        buffer.start()
        buffer.consume('a' as char)
        buffer.start()
        buffer.consume('b' as char)
        buffer.rollback()
        buffer.consume('b' as char)
        buffer.rollback()
        buffer.consume('a' as char)
    }

    def "unwind moves cursor back to most recent mark and leaves the mark"() {
        def buffer = buffer('abcd')

        expect:
        buffer.start()
        buffer.consume('a' as char)
        buffer.start()
        buffer.consume('b' as char)
        buffer.rewind()
        buffer.consume('b' as char)
        buffer.rewind()
        buffer.consume('b' as char)
    }

    def matchAB() {
        return { CharStream stream ->
            if (!stream.consume('a' as char)) {
                return
            }
            if (!stream.consume('b' as char)) {
                stream.rewind()
            }
        } as Production<CharStream>
    }

    def matchCD() {
        return { CharStream stream ->
            if (!stream.consume('c' as char)) {
                return
            }
            if (!stream.consume('d' as char)) {
                stream.rewind()
            }
        } as Production<CharStream>
    }

    def matchEOL() {
        return { CharStream stream ->
            stream.consume('\r' as char)
            stream.consume('\n' as char)
        } as Production<CharStream>
    }

    def buffer(String value, int bufferLen = 256) {
        return new Buffer(new StringReader(value), bufferLen)
    }
}
