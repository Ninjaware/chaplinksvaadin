package fi.ninjaware.chaplinksvaadin.gwt.client.timeline;

import com.chap.links.client.Timeline;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.visualization.client.AbstractDataTable;
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
        DataTable data = DataTable.create();
        data.addColumn(DataTable.ColumnType.DATETIME, "start");
        data.addColumn(DataTable.ColumnType.DATETIME, "end");
        data.addColumn(DataTable.ColumnType.STRING, "content");

        // fill the table with some data
        data.addRows(1);
        data.setValue(0, 0, dtf.parse("2014-01-02"));
        data.setValue(0, 1, dtf.parse("2014-01-04"));
        data.setValue(0, 2, "Conversation");
        data.setValue(1, 0, dtf.parse("2012-08-28"));
        data.setValue(1, 2, "Memo");
        data.setValue(2, 0, dtf.parse("2012-09-02"));
        data.setValue(2, 2, "Phone Call");

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

        draw(data, options);
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
            DataTable dt = DataTable.create();
            dt.addColumn(ColumnType.DATE, "start");
            dt.addColumn(ColumnType.DATE, "end");
            dt.addColumn(ColumnType.STRING, "content");
            dt.addColumn(ColumnType.STRING, "group");
            dt.addColumn(ColumnType.STRING, "className");
            dt.addColumn(ColumnType.BOOLEAN, "editable");
            dt.addColumn(ColumnType.STRING, "type");

            dt.addRows(events.length);

            // Iterate the events
            for (int i = 0; i < events.length; i++) {
                String[] event = events[i].split("\\|");

                // Iterate the event fields
                for (int j = 0; j < event.length; j++) {
                    ColumnType type = dt.getColumnType(j);
                    if (type.equals(ColumnType.DATE)) {
                        dt.setValue(i, j, new Date(Long.parseLong(event[j])));
                    } else if (type.equals(ColumnType.BOOLEAN)) {
                        dt.setValue(i, j, Boolean.parseBoolean(event[j]));
                    } else {
                        dt.setValue(j, j, event[j]);
                    }

                }
            }

            setData(dt);
        }

        redraw();

    }

}
