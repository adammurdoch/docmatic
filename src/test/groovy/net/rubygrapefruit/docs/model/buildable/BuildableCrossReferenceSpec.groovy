package net.rubygrapefruit.docs.model.buildable

import spock.lang.Specification
import net.rubygrapefruit.docs.model.Referenceable
import net.rubygrapefruit.docs.model.Text

class BuildableCrossReferenceSpec extends Specification {
    final Referenceable target = Mock()
    final BuildableCrossReference reference = new BuildableCrossReference(target)

    def "uses reference text of target when empty"() {
        given:
        target.referenceText >> '<target>'

        expect:
        reference.text == '<target>'
        reference.contents.size() == 1
        reference.contents[0] instanceof Text
        reference.contents[0].text == '<target>'
    }

    def "uses content when not empty"() {
        given:
        reference.append('content')

        expect:
        reference.text == 'content'
        reference.contents.size() == 1
        reference.contents[0] instanceof Text
        reference.contents[0].text == 'content'
    }

}
