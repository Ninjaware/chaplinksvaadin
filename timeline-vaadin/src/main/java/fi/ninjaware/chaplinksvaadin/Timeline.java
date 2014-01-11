package fi.ninjaware.chaplinksvaadin;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import fi.ninjaware.chaplinksvaadin.gwt.client.timeline.VTimeline;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import static fi.ninjaware.chaplinksvaadin.gwt.shared.Shared.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server side component for the VTimeline widget.
 */
@ClientWidget(VTimeline.class)
public class Timeline extends AbstractComponent {

    private static final Logger log = LoggerFactory.getLogger(Timeline.class);

    /**
     * Default values for event property Ids.
     * Details: http://almende.github.io/chap-links-library/js/timeline/doc/
     */
    public enum PropertyId {
        START,
        END,
        CAPTION,
        GROUP,
        CLASSNAME,
        EDITABLE,
        TYPE
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
         * @return The enumerable name in lower case.
         */
        public String value() {
            return toString().toLowerCase();
        }
    }

    // <editor-fold desc="Property Id's">
    
    /**
     * Event start property Id in the <code>events</code> container.
     * Type: Date
     * Required: Yes
     */
    private Object eventStartPropertyId = PropertyId.START;

    /**
     * Event end property Id in the <code>events</code> container. 
     * Type: Date
     * Required: No
     */
    private Object eventEndPropertyId = PropertyId.END;

    /**
     * Event caption property Id in the <code>events</code> container. Can be
     * plain text or HTML.
     * Type: String
     * Required: Yes
     */
    private Object eventCaptionPropertyId = PropertyId.CAPTION;

    /**
     * Event group property Id in the <code>events</code> container. Groups
     * are used to group events on one line. A vertical axis showing the groups
     * will be drawn.
     * Type: String
     * Required: No
     */
    private Object eventGroupPropertyId = PropertyId.GROUP;
    
    /**
     * Event CSS class name property Id in the <code>events</code> container. 
     * Enables custom CSS styles for events.
     * Type: String
     * Required: No
     */
    private Object eventClassNamePropertyId = PropertyId.CLASSNAME;
    
    /**
     * Event editable property Id in the <code>events</code> container. True
     * means the event can be edited or deleted, false means read-only.
     * Type: Boolean
     * Required: No
     */
    private Object eventEditablePropertyId = PropertyId.EDITABLE;
    
    /**
     * Event type property Id in the <code>events</code> container. The default
     * value can be overridden by the global option "style".
     * Type: Timeline.EventType
     * Required: No
     */
    private Object eventTypePropertyId = PropertyId.TYPE;
    
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
        if (events != null) {

            boolean eventEndPropertyFound = true;
            if (!events.getContainerPropertyIds().contains(eventEndPropertyId)) {
                // Event end date is optional.
                log.debug("Event end property '{}' not found in the container.",
                        eventEndPropertyId);
                eventEndPropertyFound = false;
            }

            if (validateEventContainer()) {
                for (int i = 0; i < events.size(); i++) {
                    Object id = events.getIdByIndex(i);
                    Item item = events.getItem(id);

                    // Event start.
                    Date startDate = (Date) item
                            .getItemProperty(eventStartPropertyId).getValue();
                    if (startDate == null) {
                        log.warn("Event start of item '{}' is null. "
                                + "Skipping item.", id);
                        continue;
                    }
                    String startStr = String.valueOf(startDate.getTime());

                    // Event end.
                    String endStr = "";
                    if (eventEndPropertyFound) {
                        Date endDate = (Date) item
                                .getItemProperty(eventEndPropertyId).getValue();
                        if (endDate != null) {
                            endStr = String.valueOf(endDate.getTime());
                        }
                    }

                    // Caption.
                    Object capObj = item.getItemProperty(eventCaptionPropertyId)
                            .getValue();
                    String caption = (capObj == null) ? "" : capObj.toString();

                    // Group.
                    // Add to the serialized container.
                    String serialized = id + "|" + startStr + "|" + endStr + "|"
                            + caption;
                    serializedEvents.add(serialized);
                }
            }
        }
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        target.addAttribute(WIDTH, getWidth() + "");
        target.addAttribute(WIDTH_UNITS, UNIT_SYMBOLS[getWidthUnits()]);
        target.addAttribute(HEIGHT, getHeight() + "");
        target.addAttribute(HEIGHT_UNITS, UNIT_SYMBOLS[getHeightUnits()]);

        target.addVariable(this, EVENTS,
                serializedEvents.toArray(new String[serializedEvents.size()]));

        serializedEvents.clear();

    }

    /**
     * Validate the event container.
     *
     * @return True, if the container is valid.
     */
    private boolean validateEventContainer() {

        boolean eventContainerValid = true;

        Collection<?> propIds = events.getContainerPropertyIds();
        if (!propIds.contains(eventStartPropertyId)) {
            log.error("Event start property '{}' not found in the container.",
                    eventStartPropertyId);
            eventContainerValid = false;

        } else if (!events.getType(eventStartPropertyId)
                .isAssignableFrom(Date.class)) {
            log.error("Event start property '{}' is not assignable from "
                    + "java.util.Date.", eventStartPropertyId);
            eventContainerValid = false;

        }

        if (propIds.contains(eventEndPropertyId)
                && !events.getType(eventEndPropertyId)
                .isAssignableFrom(Date.class)) {
            log.error("Event end property '{}' is not assignable from "
                    + "java.util.Date.", eventEndPropertyId);
            eventContainerValid = false;

        }

        if (!propIds.contains(eventCaptionPropertyId)) {
            log.error("Event caption property '{}' not found in the container.",
                    eventCaptionPropertyId);
            eventContainerValid = false;
        }
        
        // TODO: Validate the rest of the properties as well.

        return eventContainerValid;
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
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
    
    public void setEventDataSource(Container.Indexed events) {
        this.events = events;
        generateSerializedEvents();
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

    public Object getEventCaptionPropertyId() {
        return eventCaptionPropertyId;
    }

    public void setEventCaptionPropertyId(Object eventCaptionPropertyId) {
        if (eventCaptionPropertyId == null) {
            throw new NullPointerException("Property can't be null");
        }

        this.eventCaptionPropertyId = eventCaptionPropertyId;
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
        if(eventClassNamePropertyId == null) {
            throw new NullPointerException("Property can't be null");
        }
        
        this.eventClassNamePropertyId = eventClassNamePropertyId;
        requestRepaint();
    }

    public Object getEventEditablePropertyId() {
        return eventEditablePropertyId;
    }

    public void setEventEditablePropertyId(Object eventEditablePropertyId) {
        if(eventEditablePropertyId == null) {
            throw new NullPointerException("Property can't be null");
        }
        
        this.eventEditablePropertyId = eventEditablePropertyId;
        requestRepaint();
    }

    public Object getEventTypePropertyId() {
        return eventTypePropertyId;
    }

    public void setEventTypePropertyId(Object eventTypePropertyId) {
        if(eventTypePropertyId == null) {
            throw new NullPointerException("Property can't be null");
        }
        
        this.eventTypePropertyId = eventTypePropertyId;
        requestRepaint();
    }

    // </editor-fold>

    
}
