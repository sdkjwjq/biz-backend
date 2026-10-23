package org.example.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Only the server-validated filter may establish this request-local capability. */
public final class AdminTestContext {
    public static final String ACTOR = AdminTestContext.class.getName() + ".actor";
    public static final String TARGET = AdminTestContext.class.getName() + ".target";
    public record Target(String type, String id, String name, String action) { }
    private AdminTestContext() { }

    public static boolean enabled(Long actorId) {
        return actorId != null && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes
                && actorId.equals(attributes.getRequest().getAttribute(ACTOR));
    }

    public static void record(Long actorId, String type, Object id, String name, String action) {
        if (enabled(actorId) && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            attributes.getRequest().setAttribute(TARGET, new Target(type, String.valueOf(id), name, action));
        }
    }
}
