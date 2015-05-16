import com.cardshifter.modapi.actions.ActionComponent
import com.cardshifter.modapi.attributes.Attributes
import com.cardshifter.modapi.attributes.ECSAttributeMap
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.cards.ZoneComponent
import com.cardshifter.modapi.resources.ECSResource
import com.cardshifter.modapi.resources.ECSResourceMap

class CardDelegate {
    Entity entity

    Entity entity() {
        entity
    }

    def propertyMissing(String name, value) {
        ECSResource res = entity.game.resource(name)
        if (res) {
            res.retriever.set(entity, (int) value)
        } else {
            println "Missing property: Cannot set $name to $value"
        }
    }

    def propertyMissing(String name) {
        println 'Missing property: ' + name
    }

    def methodMissing(String name, args) {
        ECSResource res = entity.game.resource(name)
        if (res) {
            res.retriever.set(entity, (int) args[0])
        } else {
            println 'Missing method: ' + name
        }
    }
}

class ZoneDelegate {
    Entity entity
    ZoneComponent zone

    def cards(Closure<?> closure) {
        closure.delegate = this
        closure.call()
    }

    def card(String name, Closure<?> closure) {
        def card = entity.game.newEntity()
        ECSAttributeMap.createFor(card).set(Attributes.NAME, name)
        ECSResourceMap.createFor(card)
        card.addComponent(new ActionComponent())
        closure.delegate = new CardDelegate(entity: card)
        closure.setResolveStrategy(Closure.DELEGATE_ONLY)
        closure.call()
        zone.addOnBottom(card)
    }

    def card(Closure<?> closure) {
        card('', closure)
    }
}
