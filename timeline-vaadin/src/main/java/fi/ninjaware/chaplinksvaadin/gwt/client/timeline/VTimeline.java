package fi.ninjaware.chaplinksvaadin.gwt.client.timeline;

import com.chap.links.client.Timeline;
import com.chap.links.client.events.AddHandler;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.DOM;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.AlignmentInfo;
import static fi.ninjaware.chaplinksvaadin.gwt.shared.Shared.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VTimeline extends Timeline implements Paintable {

    /**
     * Set the CSS class name to allow styling.
     */
    public static final String CLASSNAME = "v-chaplinks-timeline";

    /**
     * Event property ids. I'm going to deliberately break the DRY principle.
     * Find the counterpart in the Timeline class. Details:
     * http://almende.github.io/chap-links-library/js/timeline/doc/
     */
    private enum EventFields {

        START("start", ColumnType.DATE),
        END("end", ColumnType.DATE),
        CONTENT("content", ColumnType.STRING),
        GROUP("group", ColumnType.STRING),
        CLASSNAME("className", ColumnType.STRING),
        EDITABLE("editable", ColumnType.BOOLEAN),
        TYPE("type", ColumnType.STRING);

        EventFields(String jsId, ColumnType type) {
            this.jsId = jsId;
            this.type = type;
        }

        final String jsId;

        final ColumnType type;

    }

    /**
     * The default icon position.
     */
    private static final AlignmentInfo defaultIconAlignment
            = new AlignmentInfo(AlignmentInfo.CENTER, AlignmentInfo.TOP);

    /**
     * The client side widget identifier
     */
    protected String paintableId;

    /**
     * Reference to the server connection object.
     */
    ApplicationConnection client;

    /**
     * Indicates whether events should be sent to the server immediately.
     */
    private boolean immediate;

    /**
     * A handler that forwards add events to the server side.
     */
    private TimelineAddHandler addHandler;

    /**
     * The constructor should first call super() to initialize the component and
     * then handle any initialization relevant to Vaadin.
     */
    public VTimeline() {
        super();
        // This method call of the Paintable interface sets the component
        // style name in DOM tree
        setStyleName(CLASSNAME);

        VisualizationUtils.loadVisualizationApi(new Runnable() {

            @Override
            public void run() {
                VConsole.log("Google Visualization JavaScript loaded");
                client.updateVariable(paintableId, JS_INITIALIZED, true, true);
                init();
            }

        }, Timeline.PACKAGE);

    }

    private void init() {
        DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM-dd");

        Options options = Timeline.Options.create();
        options.setStyle(Timeline.Options.STYLE.BOX);
        options.setStart(dtf.parse("2014-01-01"));
        options.setEnd(dtf.parse("2014-01-11"));
        options.setHeight("100%");
        options.setWidth("100%");
        options.setEditable(true);
        options.setShowCustomTime(true);
        options.setShowNavigation(true);
        options.setAxisOnTop(true);
        // options.setShowMajorLabels(false);
        // options.setShowMinorLabels(false);

        options.setMin(dtf.parse("2013-12-24"));         // lower limit of visible range
        options.setMax(dtf.parse("2014-01-31"));         // upper limit of visible range
        options.setZoomMin(1000L * 60L * 60L * 24L); // one day in milliseconds
        options.setZoomMax(1000L * 60L * 60L * 24L * 31L * 3L);  // about three months in milliseconds

        draw(null, options);
    }

    /**
     * Called whenever an update is received from the server
     *
     * @param uidl
     * @param client
     */
    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        // This call should be made first. 
        // It handles sizes, captions, tooltips, etc. automatically.
        if (client.updateComponent(this, uidl, true)) {
            // If client.updateComponent returns true there has been no changes and we
            // do not need to update anything.
            return;
        }

        // Save reference to server connection object to be able to send
        // user interaction later
        this.client = client;

        // Save the client side identifier (paintable id) for the widget
        paintableId = uidl.getId();

        String width = uidl.getStringAttribute(WIDTH);
        String width_units = uidl.getStringAttribute(WIDTH_UNITS);
        setWidth(width + width_units);

        String height = uidl.getStringAttribute(HEIGHT);
        String height_units = uidl.getStringAttribute(HEIGHT_UNITS);
        setHeight(height + height_units);

        immediate = uidl.getBooleanAttribute(IMMEDIATE);

        // Listener info
        if (addHandler == null) {
            addAddHandler(addHandler = new TimelineAddHandler());
        }
        // Handlers cannot be removed, so we have to enable/disable it.
        addHandler.setEnabled(uidl.getBooleanAttribute(HAS_ADDLISTENERS));

        // Icons and icon positions
        Set<String> attributeNames = uidl.getAttributeNames();
        Map<String, String> icons = new HashMap<String, String>();
        Map<String, AlignmentInfo> iconAlignments
                = new HashMap<String, AlignmentInfo>();
        for (String attributeName : attributeNames) {
            if (attributeName.startsWith(ICON_PREFIX)) {
                String id = attributeName.substring(ICON_PREFIX.length());
                String icon = uidl.getStringAttribute(ICON_PREFIX + id);

                icons.put(id, icon);
            } else if (attributeName.startsWith(ICONALIGN_PREFIX)) {
                String id = attributeName.substring(ICONALIGN_PREFIX.length());
                int alignBits = uidl.getIntAttribute(ICONALIGN_PREFIX + id);

                iconAlignments.put(id, new AlignmentInfo(alignBits));
            }
        }

        // Events
        String[] events = uidl.getStringArrayVariable(EVENTS);
        if (events.length > 0) {
            String[] fields = uidl.getStringArrayAttribute(FIELDS);
            DataTable dt = generateData(fields, events, icons, iconAlignments);
            setData(dt);
        }

        redraw();

    }

    /**
     * Generate the timeline data from the UIDL data.
     *
     * @param fields The fields used in <code>events</code>.
     * @param events The timeline events.
     * @param icons A map of icon id's and icon UIDL URIs.
     * @param iconAlignments A map of icon id's and icon alignments.
     * @return A DataTable of events.
     */
    private DataTable generateData(String[] fields, String[] events,
            Map<String, String> icons,
            Map<String, AlignmentInfo> iconAlignments) {
        DataTable dt = DataTable.create();

        int contentCol = -1;
        for (String field : fields) {
            EventFields eventField = EventFields.valueOf(field);
            int col = dt.addColumn(eventField.type, eventField.jsId);
            if (eventField.equals(EventFields.CONTENT)) {
                contentCol = col;
            }
        }

        dt.addRows(events.length);

        // Iterate the events
        for (int i = 0; i < events.length; i++) {
            JSONArray event = JSONParser.parseStrict(events[i]).isArray();

            int dtCol = 0;

            String id = event.get(0).isString().stringValue();

            // Iterate the event fields. Ignore the first field (=id).
            for (int j = 1; j < event.size(); j++) {
                ColumnType type = dt.getColumnType(dtCol);

                String value = event.get(j).isString().stringValue();
                if (!value.isEmpty()) {
                    if (type.equals(ColumnType.DATE)) {
                        dt.setValue(i, dtCol, new Date(Long.parseLong(value)));
                    } else if (type.equals(ColumnType.BOOLEAN)) {
                        dt.setValue(i, dtCol, Boolean.parseBoolean(value));
                    } else {
                        dt.setValue(i, dtCol, value);
                    }
                }

                dtCol++;
            }

            if (icons.containsKey(id)) {
                String iconUri = client.translateVaadinUri(icons.get(id));
                String content = dt.getValueString(i, contentCol);

                AlignmentInfo iconAlign = iconAlignments.containsKey(id)
                        ? iconAlignments.get(id)
                        : defaultIconAlignment;

                Element icon = DOM.createImg();
                icon.setPropertyString("src", iconUri);
                Element helperSpan = DOM.createSpan();
                helperSpan.appendChild(icon);

                StringBuilder style = new StringBuilder();

                if (iconAlign.isLeft() || iconAlign.isHorizontalCenter()) {
                    style.append("margin-right: auto;");
                }
                if (iconAlign.isRight() || iconAlign.isHorizontalCenter()) {
                    style.append("margin-left: auto;");
                }

                style.append("vertical-align: ")
                        .append(iconAlign.getVerticalAlignment())
                        .append(";");

                if (iconAlign.isBottom() || iconAlign.isTop()) {
                    style.append("display: block;");
                }

                icon.setPropertyString("style", style.toString());

                String value;

                /*
                 I = Icon first
                 C = Text first
                 III
                 IIC
                 CCC
                 */
                if (iconAlign.isBottom()
                        || (iconAlign.isRight() && !iconAlign.isTop())) {
                    value = content + helperSpan.getInnerHTML();
                } else {
                    value = helperSpan.getInnerHTML() + content;
                }

                dt.setValue(i, contentCol, value);
            }
        }

        return dt;
    }

    /**
     * Get the number of events on the timeline.
     *
     * @return The number of events.
     */
    public int getEventCount() {
        return getEventCountNative(getJso());
    }

    private native int getEventCountNative(JavaScriptObject jso) /*-{
     return jso.items.length;
     }-*/;

    class TimelineAddHandler extends AddHandler {

        private boolean enabled;

        @Override
        public void onAdd(AddEvent event) {
            if (!enabled) {
                return;
            }
            
            // Find the new event.
            int index = getEventCount() - 1;
            VTimelineEvent newEvent = (VTimelineEvent) getItem(index).cast();
            client.updateVariable(paintableId, NEW_EVENT + index,
                    newEvent.getSerialized(), immediate);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

    }
}
