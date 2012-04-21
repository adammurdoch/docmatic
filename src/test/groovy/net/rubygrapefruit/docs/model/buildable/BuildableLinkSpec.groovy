package net.rubygrapefruit.docs.model.buildable

import net.rubygrapefruit.docs.model.Text
import spock.lang.Specification

class BuildableLinkSpec extends Specification {
    final URI target = new URI("local.html")
    final BuildableLink link = new BuildableLink(target)

    def "uses URL text of target when empty"() {
        expect:
        link.text == 'local.html'
        link.contents.size() == 1
        link.contents[0] instanceof Text
        link.contents[0].text == 'local.html'
    }

    def "uses content when not empty"() {
        given:
        link.append('content')

        expect:
        link.text == 'content'
        link.contents.size() == 1
        link.contents[0] instanceof Text
        link.contents[0].text == 'content'
    }

}
