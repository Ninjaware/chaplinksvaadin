package fi.ninjaware.chaplinksvaadin.example;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import fi.ninjaware.chaplinksvaadin.Timeline;
import fi.ninjaware.chaplinksvaadin.Timeline.EventType;
import fi.ninjaware.chaplinksvaadin.Timeline.EventFields;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class ExampleApplication extends Application {

    private static final Logger log;
    
    static {
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
        log = LoggerFactory.getLogger(ExampleApplication.class);
    }
    
    private Window window;

    @Override
    public void init() {
        setTheme("example");
        
        window = new Window("Widget Test");
        setMainWindow(window);

        final Timeline tl = new Timeline();

        Button b = new Button("Click");
        b.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                tl.setWidth(600, Sizeable.UNITS_PIXELS);
                tl.setHeight(300, Sizeable.UNITS_PIXELS);

                IndexedContainer c = new IndexedContainer();
                c.addContainerProperty(EventFields.START, Date.class, null);
                c.addContainerProperty(EventFields.END, Date.class, null);
                c.addContainerProperty(EventFields.CONTENT, String.class, null);
                c.addContainerProperty(EventFields.TYPE, EventType.class, null);
                c.addContainerProperty(EventFields.ICON, Resource.class, null);
                c.addContainerProperty(EventFields.ICON_ALIGNMENT, Alignment.class, null);

                Calendar cal = GregorianCalendar.getInstance();
                cal.set(2014, 0, 5);

                {
                    Item item = c.getItem(c.addItem());
                    item.getItemProperty(EventFields.START)
                            .setValue(cal.getTime());
                    cal.add(Calendar.DAY_OF_MONTH, 4);
                    item.getItemProperty(EventFields.END)
                            .setValue(cal.getTime());
                    item.getItemProperty(EventFields.CONTENT)
                            .setValue("Holiday");
                }

                {
                    Item item = c.getItem(c.addItem());
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    item.getItemProperty(EventFields.START)
                            .setValue(cal.getTime());
                    cal.add(Calendar.DAY_OF_MONTH, 6);
                    item.getItemProperty(EventFields.END)
                            .setValue(cal.getTime());
                    item.getItemProperty(EventFields.CONTENT)
                            .setValue("Something fun");
                    item.getItemProperty(EventFields.ICON)
                            .setValue(new ThemeResource("air_plane_airport_2.png"));
                    item.getItemProperty(EventFields.ICON_ALIGNMENT)
                            .setValue(Alignment.BOTTOM_LEFT);
                }

                {
                    Item item = c.getItem(c.addItem());
                    cal.add(Calendar.DAY_OF_MONTH, 8);
                    item.getItemProperty(EventFields.START)
                            .setValue(cal.getTime());
                    item.getItemProperty(EventFields.CONTENT)
                            .setValue("Single day event");
                    item.getItemProperty(EventFields.TYPE)
                            .setValue(EventType.BOX);
                    item.getItemProperty(EventFields.ICON)
                            .setValue(new ThemeResource("taxi.png"));
                    item.getItemProperty(EventFields.ICON_ALIGNMENT)
                            .setValue(Alignment.MIDDLE_LEFT);
                }
                
                try {
                    tl.setEventDataSource(c);
                } catch (Timeline.EventContainerInvalidException exs) {
                    log.error("Event container invalid.");
                    for(Timeline.EventContainerInvalidException ex : 
                            exs.getCauses()) {
                        log.error(ex.getMessage());
                    }
                }
            }
        });

        window.addComponent(b);
        window.addComponent(tl);
    }

}
