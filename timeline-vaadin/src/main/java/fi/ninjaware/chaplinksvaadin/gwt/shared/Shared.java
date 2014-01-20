package fi.ninjaware.chaplinksvaadin.gwt.shared;

import java.io.Serializable;

/**
 * Shared indices for server and client.
 *
 * @author miku
 */
public enum Shared implements Serializable {

    WIDTH,
    WIDTH_UNITS,
    HEIGHT,
    HEIGHT_UNITS,
    IMMEDIATE,
    EDITABLE,
    ANIMATE,
    VIEWPORT_START,
    VIEWPORT_END,
    TIMELINE_START,
    TIMELINE_END,
    STYLE,
    AXISONTOP,
    NAVIGATION,
    JS_INITIALIZED,
    EVENTS,
    NEW_EVENT,
    FIELDS,
    HAS_ADDLISTENERS,
    ICON_PREFIX("icon-"),
    ICONALIGN_PREFIX("iconpos-"),
    EVENT_ADD_EVENT_ID;

    private Shared() {
        v = toString();
    }

    private Shared(String value) {
        v = value;
    }

    public final String v;

}
