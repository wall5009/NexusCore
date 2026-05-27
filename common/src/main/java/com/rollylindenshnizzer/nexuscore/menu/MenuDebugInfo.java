package com.rollylindenshnizzer.nexuscore.menu;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

import java.util.List;
import java.util.Map;

@NexusStable(since = "1.2")
public record MenuDebugInfo(String menuId,
                            int containerId,
                            Map<String, String> bindings,
                            List<String> routes,
                            List<String> syncFields) {
    public MenuDebugInfo {
        bindings = bindings == null ? Map.of() : Map.copyOf(bindings);
        routes = routes == null ? List.of() : List.copyOf(routes);
        syncFields = syncFields == null ? List.of() : List.copyOf(syncFields);
    }

    public String summary() {
        return menuId + "#" + containerId + " bindings=" + bindings.size()
                + " routes=" + routes.size() + " syncFields=" + syncFields.size();
    }
}
