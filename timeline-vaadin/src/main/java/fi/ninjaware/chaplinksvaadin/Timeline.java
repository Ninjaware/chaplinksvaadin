package fi.ninjaware.chaplinksvaadin;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import fi.ninjaware.chaplinksvaadin.gwt.client.timeline.VTimeline;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ClientWidget;
import static fi.ninjaware.chaplinksvaadin.gwt.shared.Shared.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server side component for the VTimeline widget.
 */
@ClientWidget(VTimeline.class)
public class Timeline extends AbstractComponent {

    private static final Logger log = LoggerFactory.getLogger(Timeline.class);

    /**
     * Default values for event property Ids. I'm going to deliberately break
     * the DRY principle. Find the counterpart in the VTimeline class. Details:
     * http://almende.github.io/chap-links-library/js/timeline/doc/
     */
    public enum EventFields {

        START(Date.class),
        END(Date.class),
        CONTENT(String.class),
        GROUP(String.class),
        CLASSNAME(String.class),
        EDITABLE(Boolean.class),
        TYPE(EventType.class),
        ICON(Resource.class),
        ICON_ALIGNMENT(Alignment.class);

        private EventFields(Class type) {
            this.type = type;
        }

        private Class type;

    }

    /**
     * Event types. Default type is "range".
     */
    public enum EventType {

        BOX,
        RANGE,
        DOT;

        /**
         * The value used by the JavaScript library.
         *
         * @return The enumerable name in lower case.
         */
        public String value() {
            return toString().toLowerCase();
        }

        public static EventType getDefault() {
            return RANGE;
        }
    }

    /**
     * An exception used to indicate invalid properties in the events container.
     */
    public static class EventContainerInvalidException extends Exception {

        /**
         * A collection of exceptions.
         */
        private Collection<EventContainerInvalidException> causes = null;

        /**
         * The simple constructor.
         *
         * @param message Exception message.
         */
        public EventContainerInvalidException(String message) {
            this(message, new ArrayList<EventContainerInvalidException>());
        }

        /**
         * The collection constructor.
         *
         * @param message Exception message.
         * @param causes A collection of all exceptions that occurred.
         */
        EventContainerInvalidException(String message,
                Collection<EventContainerInvalidException> causes) {
            super(message);
            this.causes = causes;
        }

        public Collection<EventContainerInvalidException> getCauses() {
            return causes;
        }

    }

    // <editor-fold desc="Property ids">
    /**
     * Event start property id in the <code>events</code> container. Type: Date
     * Required: Yes
     */
    private Object eventStartPropertyId = EventFields.START;

    /**
     * Event end property id in the <code>events</code> container. Type: Date
     * Required: No
     */
    private Object eventEndPropertyId = EventFields.END;

    /**
     * Event content property id in the <code>events</code> container. Can be
     * plain text or HTML. Type: String Required: Yes
     */
    private Object eventContentPropertyId = EventFields.CONTENT;

    /**
     * Event group property id in the <code>events</code> container. Groups are
     * used to group events on one line. A vertical axis showing the groups will
     * be drawn. Type: String Required: No
     */
    private Object eventGroupPropertyId = EventFields.GROUP;

    /**
     * Event CSS class name property id in the <code>events</code> container.
     * Enables custom CSS styles for events. Type: String Required: No
     */
    private Object eventClassNamePropertyId = EventFields.CLASSNAME;

    /**
     * Event editable property id in the <code>events</code> container. True
     * means the event can be edited or deleted, false means read-only. Type:
     * Boolean Required: No
     */
    private Object eventEditablePropertyId = EventFields.EDITABLE;

    /**
     * Event type property id in the <code>events</code> container. The default
     * value can be overridden by the global option "style". Type:
     * Timeline.EventType Required: No
     */
    private Object eventTypePropertyId = EventFields.TYPE;

    /**
     * Event icon property id in the <code>events</code> container. Type:
     * com.vaadin.terminal.Resource Required: No
     */
    private Object eventIconPropertyId = EventFields.ICON;

    /**
     * Event icon alignment property id in the <code>events</code> container. If
     * the container has <code>eventIconPropertyId</code> property, but not the
     * <code>eventIconAlignmentPropertyId</code> property, the default value of
     * icon alignment will be Alignment.TOP_CENTER. Type:
     * com.vaadin.ui.Alignment Required: No
     */
    private Object eventIconAlignmentPropertyId = EventFields.ICON_ALIGNMENT;

    // </editor-fold>
    /**
     * Event data container.
     */
    private Container.Indexed events;

    /**
     * Serialized events to be sent to the client.
     */
    private final List<String> serializedEvents = new ArrayList<String>();

    /**
     * The field map of the serialized events. Key = <code>events</code>
     * container id, Value = enumerable name.
     */
    private final Map<Object, String> serializedFields
            = new LinkedHashMap<Object, String>();

    /**
     * A map for the event icons. Key = <code>events</code> container id, Value
     * = Icon resource.
     */
    private final Map<Object, Resource> eventIcons
            = new LinkedHashMap<Object, Resource>();

    /**
     * A map for the event icon alignments. Key = <code>events</code> container
     * id, Value = Icon alignment.
     */
    private final Map<Object, Alignment> eventIconAlignments
            = new LinkedHashMap<Object, Alignment>();

    /**
     * True, when the required JavaScript has been loaded.
     */
    private boolean js_initialized;

    /**
     * The one and only constructor.
     */
    public Timeline() {
        super();

        setHeight(250, UNITS_PIXELS);
        setWidth(500, UNITS_PIXELS);
    }

    private void generateSerializedEvents() {

        serializedFields.clear();

        boolean iconFieldExists = false, iconAlignmentFieldExists = false;

        // Required fields
        serializedFields.put(eventStartPropertyId,
                EventFields.START.toString());
        serializedFields.put(eventContentPropertyId,
                EventFields.CONTENT.toString());

        // Optional fields
        Collection<?> propIds = events.getContainerPropertyIds();
        if (propIds.contains(eventEndPropertyId)) {
            serializedFields.put(eventEndPropertyId,
                    EventFields.END.toString());
        }
        if (propIds.contains(eventGroupPropertyId)) {
            serializedFields.put(eventGroupPropertyId,
                    EventFields.GROUP.toString());
        }
        if (propIds.contains(eventClassNamePropertyId)) {
            serializedFields.put(eventClassNamePropertyId,
                    EventFields.CLASSNAME.toString());
        }
        if (propIds.contains(eventEditablePropertyId)) {
            serializedFields.put(eventEditablePropertyId,
                    EventFields.EDITABLE.toString());
        }
        if (propIds.contains(eventTypePropertyId)) {
            serializedFields.put(eventTypePropertyId,
                    EventFields.TYPE.toString());
        }
        if (propIds.contains(eventIconPropertyId)) {
            iconFieldExists = true;
        }
        if (propIds.contains(eventIconAlignmentPropertyId)) {
            iconAlignmentFieldExists = true;
        }

        Set<Object> containerProps = serializedFields.keySet();

        // Iterate the events and add them to the serializedEvents list
        serializedEvents.clear();
        for (int i = 0; i < events.size(); i++) {
            Object id = events.getIdByIndex(i);
            Item item = events.getItem(id);

            StringBuilder srlzd = new StringBuilder();

            srlzd.append(id).append("|");

            // Event start
            Date startDate = (Date) item
                    .getItemProperty(eventStartPropertyId).getValue();
            if (startDate == null) {
                log.warn("Event start of item '{}' is null. "
                        + "Skipping item.", id);
                continue;
            }
            srlzd.append(String.valueOf(startDate.getTime()));

            Iterator<?> propIterator = containerProps.iterator();
            propIterator.next(); // Start date was already handled.
            while (propIterator.hasNext()) {
                Object propertyId = propIterator.next();
                Object property = item.getItemProperty(propertyId).getValue();
                Class<?> type = events.getType(propertyId);

                srlzd.append("|"); // add delimiter

                if (type.isAssignableFrom(Date.class)) {
                    Date date = (Date) property;
                    String strValue = "";
                    if (date != null) {
                        strValue = String.valueOf(date.getTime());
                    }
                    srlzd.append(strValue);
                } else if (type.isAssignableFrom(Boolean.class)) {
                    Boolean bool = (Boolean) property;
                    srlzd.append(bool == null ? Boolean.FALSE.toString()
                            : bool.toString());
                } else if (type.isAssignableFrom(EventType.class)) {
                    EventType eventType = (EventType) property;
                    srlzd.append(eventType == null
                            ? EventType.getDefault().value()
                            : eventType.value());
                } else {
                    // TODO: Escape the string. It could contain the delimiter character.
                    srlzd.append(property == null ? "" : property.toString());
                }
            }

            // Icons and icon alignments
            if (iconFieldExists) {
                Resource icon = (Resource) item
                        .getItemProperty(eventIconPropertyId).getValue();

                if (icon != null) {
                    eventIcons.put(id, icon);
                }
            } 
            if (iconAlignmentFieldExists) {
                Alignment alignment = (Alignment) item
                        .getItemProperty(eventIconAlignmentPropertyId)
                        .getValue();
                if (alignment != null) {
                    eventIconAlignments.put(id, alignment);
                }
            }

            // Add the serialized event to the serialized events' list.
            serializedEvents.add(srlzd.toString());
        }

    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        target.addAttribute(WIDTH, getWidth() + "");
        target.addAttribute(WIDTH_UNITS, UNIT_SYMBOLS[getWidthUnits()]);
        target.addAttribute(HEIGHT, getHeight() + "");
        target.addAttribute(HEIGHT_UNITS, UNIT_SYMBOLS[getHeightUnits()]);

        Collection<String> fields = serializedFields.values();
        target.addAttribute(FIELDS, fields.toArray(new String[fields.size()]));
        target.addVariable(this, EVENTS,
                serializedEvents.toArray(new String[serializedEvents.size()]));

        for (Object id : eventIcons.keySet()) {
            target.addAttribute(ICON_PREFIX + id, eventIcons.get(id));
        }

        for (Object id : eventIconAlignments.keySet()) {
            target.addAttribute(ICONALIGN_PREFIX + id,
                    eventIconAlignments.get(id).getBitMask());
        }

        serializedEvents.clear();
        eventIcons.clear();
        eventIconAlignments.clear();
    }

    /**
     * Validate the event container.
     *
     * @throws
     * fi.ninjaware.chaplinksvaadin.Timeline.EventContainerInvalidException when
     * there are one or more problems with the event container.
     */
    private void validateEventContainer() throws
            EventContainerInvalidException {

        List<EventContainerInvalidException> exceptions
                = new ArrayList<EventContainerInvalidException>();

        Collection<?> propIds = events.getContainerPropertyIds();

        // Check that required fields are in the container.
        if (!propIds.contains(eventStartPropertyId)) {
            String m = "Event start property '%s' not found in the container.";
            exceptions.add(new EventContainerInvalidException(
                    String.format(m, eventStartPropertyId)));

        }

        if (!propIds.contains(eventContentPropertyId)) {
            String m = "Event content property '%s' not "
                    + "found in the container.";
            exceptions.add(new EventContainerInvalidException(
                    String.format(m, eventContentPropertyId)));
        }

        // Check that all the data types match.
        Map<Object, EventFields> propertyFieldMap 
                = new LinkedHashMap<Object, EventFields>();
        propertyFieldMap.put(eventStartPropertyId, EventFields.START);
        propertyFieldMap.put(eventEndPropertyId, EventFields.END);
        propertyFieldMap.put(eventEditablePropertyId, EventFields.EDITABLE);
        propertyFieldMap.put(eventTypePropertyId, EventFields.TYPE);
        propertyFieldMap.put(eventIconPropertyId, EventFields.ICON);
        propertyFieldMap.put(eventIconAlignmentPropertyId, 
                EventFields.ICON_ALIGNMENT);

        for(Object propertyId : propertyFieldMap.keySet()) {
            try {
                validateProperty(propertyId, propertyFieldMap.get(propertyId));
            } catch(EventContainerInvalidException ex) {
                exceptions.add(ex);
            }
        }
        
        if (!exceptions.isEmpty()) {
            throw new EventContainerInvalidException(
                    "Found one or more problems in the event container. "
                    + "Use getCauses() method to see them.",
                    exceptions);
        }
    }

    /**
     * Validate a single property by checking its type.
     *
     * @param propertyId The property id in the <code>events</code> container.
     * @param field The corresponding event field enumerable.
     * @throws
     * fi.ninjaware.chaplinksvaadin.Timeline.EventContainerInvalidException when
     * the container contains <code>propertyId</code> but it's of wrong type.
     */
    private void validateProperty(Object propertyId, EventFields field) throws
            EventContainerInvalidException {
        Collection<?> propIds = events.getContainerPropertyIds();
        if (propIds.contains(propertyId)
                && !events.getType(propertyId).isAssignableFrom(field.type)) {
            String m = "Event property '%s' is not assignable from %s";
            throw new EventContainerInvalidException(
                    String.format(m, propertyId, field.type));
        }
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables
    ) {
        if (variables.containsKey(JS_INITIALIZED)) {
            log.debug("Google Visualization JavaScript loaded.");
            js_initialized = (Boolean) variables.get(JS_INITIALIZED);

            // TODO: paint (or not?).
        }

    }

    @Override
    public void requestRepaint() {
        // Don't allow repaint until the javascript has loaded.
        if (js_initialized) {
            super.requestRepaint();
        }
    }

    // <editor-fold desc="Getters and Setters">
    public Container.Indexed getEventDataSource() {
        return events;
    }

    /**
     * Set the event data source.
     *
     * @param events The event container.
     * @throws
     * fi.ninjaware.chaplinksvaadin.Timeline.EventContainerInvalidException when
     * the container properties are invalid.
     */
    public void setEventDataSource(Container.Indexed events)
            throws EventContainerInvalidException {
        this.events = events;
        if (this.events != null) {
            validateEventContainer();
            generateSerializedEvents();
        }

        requestRepaint();
    }

    public Object getEventStartPropertyId() {
        return eventStartPropertyId;
    }

    public void setEventStartPropertyId(Object eventStartPropertyId) {
        if (eventStartPropertyId == null) {
            throw new NullPointerException("Property can't be null");
        }

        this.eventStartPropertyId = eventStartPropertyId;
        requestRepaint();
    }

    public Object getEventEndPropertyId() {
        return eventEndPropertyId;
    }

    public void setEventEndPropertyId(Object eventEndPropertyId) {
        if (eventEndPropertyId == null) {
            throw new NullPointerException("Property can't be null");
        }

        this.eventEndPropertyId = eventEndPropertyId;
        requestRepaint();
    }

    public Object getEventContentPropertyId() {
        return eventContentPropertyId;
    }

    public void setEventContentPropertyId(Object eventContentPropertyId) {
        if (eventContentPropertyId == null) {
            throw new NullPointerException("Property can't be null");
        }

        this.eventContentPropertyId = eventContentPropertyId;
        requestRepaint();
    }

    public Object getEventGroupPropertyId() {
        return eventGroupPropertyId;
    }

    public void setEventGroupPropertyId(Object eventGroupPropertyId) {
        if (eventGroupPropertyId == null) {
            throw new NullPointerException("Property can't be null");
        }

        this.eventGroupPropertyId = eventGroupPropertyId;
        requestRepaint();
    }

    public Object getEventClassNamePropertyId() {
        return eventClassNamePropertyId;
    }

    public void setEventClassNamePropertyId(Object eventClassNamePropertyId) {
        if (eventClassNamePropertyId == null) {
            throw new NullPointerException("Property can't be null");
        }

        this.eventClassNamePropertyId = eventClassNamePropertyId;
        requestRepaint();
    }

    public Object getEventEditablePropertyId() {
        return eventEditablePropertyId;
    }

    public void setEventEditablePropertyId(Object eventEditablePropertyId) {
        if (eventEditablePropertyId == null) {
            throw new NullPointerException("Property can't be null");
        }

        this.eventEditablePropertyId = eventEditablePropertyId;
        requestRepaint();
    }

    public Object getEventTypePropertyId() {
        return eventTypePropertyId;
    }

    public void setEventTypePropertyId(Object eventTypePropertyId) {
        if (eventTypePropertyId == null) {
            throw new NullPointerException("Property can't be null");
        }

        this.eventTypePropertyId = eventTypePropertyId;
        requestRepaint();
    }

    public Object getEventIconPropertyId() {
        return eventIconPropertyId;
    }

    public void setEventIconPropertyId(Object eventIconPropertyId) {
        if (eventIconPropertyId == null) {
            throw new NullPointerException("Property can't be null");
        }
        this.eventIconPropertyId = eventIconPropertyId;
    }

    public Object getEventIconAlignmentPropertyId() {
        return eventIconAlignmentPropertyId;
    }

    public void setEventIconAlignmentPropertyId(Object eventIconAlignmentPropertyId) {
        if (eventIconAlignmentPropertyId == null) {
            throw new NullPointerException("Property can't be null");
        }
        this.eventIconAlignmentPropertyId = eventIconAlignmentPropertyId;
    }

    // </editor-fold>
}
