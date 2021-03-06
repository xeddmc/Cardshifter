package com.cardshifter.modapi.resources;

import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.Entity;

import java.util.*;
import java.util.stream.Stream;

public class ResourceModifierComponent extends Component implements ECSResourceStrategy {

    private final Map<ECSResource, List<EntityModifier>> strategies = new HashMap<>();

    public void addModifier(ECSResource resource, EntityModifier modifier) {
        this.strategies.putIfAbsent(resource, new LinkedList<>());
        List<EntityModifier> list = this.strategies.get(resource);
        list.add(modifier);
        list.sort(Comparator.comparingInt(em -> em.getPriority()));
    }

    public Stream<ECSResource> getModifiedResources() {
        return strategies.keySet().stream();
    }

    @Override
    public int getResource(Entity entity, ECSResource resource, int actualValue) {
        Iterable<EntityModifier> modifiers = strategies.get(resource);
        if (modifiers == null) {
            return actualValue;
        }

        int value = actualValue;
        for (EntityModifier modifier : modifiers) {
            if (modifier.isActive() && modifier.appliesTo(entity)) {
                value = modifier.getResource(entity, resource, value);
            }
        }
        return value;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + strategies;
    }

    public int getModifiedResourcesCount() {
        return strategies.size();
    }
}
