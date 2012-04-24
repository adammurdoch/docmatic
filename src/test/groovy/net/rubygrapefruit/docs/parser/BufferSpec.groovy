package net.rubygrapefruit.docs.parser

import spock.lang.Specification

class BufferSpec extends Specification {
    def "production can match a sequence of characters"() {
        def buffer = buffer('abc')
        def production = { CharStream stream ->
            assert stream.consume('a' as char)
            assert stream.consume('b' as char)
        } as CharProduction

        expect:
        buffer.scanFor(production)
        buffer.value == 'ab'
    }

    def "production cannot consume beyond the end of input"() {
        def buffer = buffer('a')
        def production = { CharStream stream ->
            assert stream.consume('a' as char)
            assert !stream.consume('b' as char)
        } as CharProduction

        expect:
        buffer.scanFor(production)
        buffer.value == 'a'
    }

    def "production is not considered a match when it unwinds"() {
        def buffer = buffer('abab')
        def production = { CharStream stream ->
            stream.consume('a' as char)
            stream.consume('b' as char)
            stream.consume('c' as char)
            stream.unwind()
        } as CharProduction

        expect:
        !buffer.scanFor(production)
        buffer.value == ''
    }

    def "production does not consume any characters when it unwinds"() {
        def buffer = buffer('abde')
        def production1 = { CharStream stream ->
            stream.consume('a' as char)
            stream.consume('b' as char)
            stream.consume('c' as char)
            stream.unwind()
        } as CharProduction
        def production2 = matchAB()

        expect:
        !buffer.scanFor(production1)
        buffer.scanFor(production2)
        buffer.value == 'ab'
    }

    def "production can unwind on failed match"() {
        def buffer = buffer('abc')
        def production = { CharStream stream ->
            stream.consume('c' as char)
            stream.unwind()
            stream.consume('a' as char)
            stream.consume('b' as char)
        } as CharProduction

        expect:
        buffer.scanFor(production)
        buffer.value == 'ab'
    }

    def "production can match a nested production"() {
        def buffer = buffer('abab')
        def nested = matchAB()
        def production = { CharStream stream ->
            assert stream.consume(nested)
            assert stream.consume(nested)
        } as CharProduction

        expect:
        buffer.scanFor(production)
        buffer.value == 'abab'
    }

    def "no characters are consumed when nested production unwinds"() {
        def buffer = buffer('abab')
        def nested1 = { CharStream stream ->
            stream.consume('a' as char)
            stream.consume('c' as char)
            stream.unwind()
        } as CharProduction
        def nested2 = matchAB()
        def production = { CharStream stream ->
            assert !stream.consume(nested1)
            assert stream.consume(nested2)
        } as CharProduction

        expect:
        buffer.scanFor(production)
        buffer.value == 'ab'
    }

    def "production can match one or more nested production"() {
        def buffer = buffer('ababc')
        def nested = matchAB()
        def production = { CharStream stream ->
            assert stream.consumeAtLeastOne(nested)
        } as CharProduction

        expect:
        buffer.scanFor(production)
        buffer.value == 'abab'
    }

    def "atLeastOne match does not consume any characters when only a partial match is found"() {
        def buffer = buffer('acabc')
        def nested = matchAB()
        def production = { CharStream stream ->
            assert !stream.consumeAtLeastOne(nested)
        } as CharProduction

        expect:
        !buffer.scanFor(production)
        buffer.value == ''
    }

    def "atLeastOne match does not consume any characters from subsequent partial match is found"() {
        def buffer = buffer('abac')
        def nested = matchAB()
        def production = { CharStream stream ->
            assert stream.consumeAtLeastOne(nested)
        } as CharProduction

        expect:
        buffer.scanFor(production)
        buffer.value == 'ab'
    }

    def "nested productions can consume beyond the end of the buffer"() {
        def buffer = buffer('abab', 3)
        def nested = matchAB()
        def production = { CharStream stream ->
            stream.consume(nested)
        } as CharProduction

        expect:
        buffer.scanFor(production)
        buffer.scanFor(production)
        buffer.value == 'ab'
    }

    def matchAB() {
        return { CharStream stream ->
            if (!stream.consume('a' as char)) {
                return
            }
            if (!stream.consume('b' as char)) {
                stream.unwind()
            }
        } as CharProduction
    }

    def buffer(String value, int bufferLen = 256) {
        return new Buffer(new StringReader(value), bufferLen)
    }
}
