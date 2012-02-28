package net.rubygrapefruit.docs.model.buildable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.rubygrapefruit.docs.model.Action;
import net.rubygrapefruit.docs.model.Document;

import java.util.HashMap;
import java.util.Map;

public class BuildableDocument extends BuildableComponent implements Document {
    public String getTypeName() {
        return "Book";
    }

    /**
     * Finishes building this document. Does the following:
     *
     * <ul>
     * <li>Assigns ids to those components with no id assigned already.
     * <li>Resolves links and cross references.
     * </ul>
     */
    public void finish() {
        assignIds();
        super.finish();
    }

    private void assignIds() {
        final Multimap<String, BuildableComponent> unassigned = HashMultimap.create();
        final Map<String, BuildableComponent> assigned = new HashMap<String, BuildableComponent>();
        final Map<String, Integer> typeCounts = new HashMap<String, Integer>();

        visitAllComponents(new Action<BuildableComponent>() {
            public void execute(BuildableComponent component) {
                String type = component.getTypeName();
                Integer value = typeCounts.get(type);
                if (value == null) {
                    value = 1;
                }
                typeCounts.put(type, value + 1);
                if (component.getId() != null && !component.getId().isEmpty()) {
                    assigned.put(component.getId(), component);
                    return;
                }
                String candidateId = component.getTitle().getText();
                if (candidateId.isEmpty()) {
                    candidateId = type + value;
                }
                String id = candidateId.toLowerCase().replaceAll("\\s+", "_");
                unassigned.put(id, component);
            }
        });

        for (Map.Entry<String, BuildableComponent> entry : unassigned.entries()) {
            BuildableComponent component = entry.getValue();
            String id = entry.getKey();
            if (!assigned.containsKey(id)) {
                component.setId(id);
                assigned.put(id, component);
                continue;
            }
            for (int i = 1; ; i++) {
                String candidate = id + '_' + i;
                if (!assigned.containsKey(candidate)) {
                    assigned.put(candidate, component);
                    component.setId(candidate);
                    break;
                }
            }
        }
    }
}
