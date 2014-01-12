package fi.ninjaware.chaplinksvaadin.gwt.client.timeline;

import com.chap.links.client.Timeline;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.vaadin.terminal.gwt.client.VConsole;
import static fi.ninjaware.chaplinksvaadin.gwt.shared.Shared.*;
import java.util.Date;

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
     * The client side widget identifier
     */
    protected String paintableId;

    /**
     * Reference to the server connection object.
     */
    ApplicationConnection client;

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

        // Events
        String[] events = uidl.getStringArrayVariable(EVENTS);
        if (events.length > 0) {
            String[] fields = uidl.getStringArrayAttribute(FIELDS);
            DataTable dt = DataTable.create();
            for (String field : fields) {
                EventFields eventField = EventFields.valueOf(field);
                dt.addColumn(eventField.type, eventField.jsId);
            }

            dt.addRows(events.length);

            // Iterate the events
            for (int i = 0; i < events.length; i++) {
                String[] event = events[i].split("\\|");

                int dtCol = 0;
                // Iterate the event fields. Ignore the first field (=id).
                for (int j = 1; j < event.length; j++) {
                    ColumnType type = dt.getColumnType(dtCol);
                    if (!event[j].isEmpty()) {
                        if (type.equals(ColumnType.DATE)) {
                            dt.setValue(i, dtCol, new Date(Long.parseLong(event[j])));
                        } else if (type.equals(ColumnType.BOOLEAN)) {
                            dt.setValue(i, dtCol, Boolean.parseBoolean(event[j]));
                        } else {
                            dt.setValue(i, dtCol, event[j]);
                        }
                    }

                    dtCol++;
                }
            }

            setData(dt);
        }

        redraw();

    }

}
