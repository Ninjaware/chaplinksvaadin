package fi.ninjaware.chaplinksvaadin.example;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.visualization.client.DataTable;
import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import fi.ninjaware.chaplinksvaadin.Timeline;
import fi.ninjaware.chaplinksvaadin.Timeline.PropertyId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class ExampleApplication extends Application {

    private Window window;

    @Override
    public void init() {
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
                c.addContainerProperty(PropertyId.START, Date.class, null);
                c.addContainerProperty(PropertyId.END, Date.class, null);
                c.addContainerProperty(PropertyId.CAPTION, String.class, null);

                Calendar cal = GregorianCalendar.getInstance();
                cal.set(2014, 1, 5);
                
                {
                    Item item = c.getItem(c.addItem());
                    item.getItemProperty(PropertyId.START)
                            .setValue(cal.getTime());
                    cal.add(Calendar.DAY_OF_MONTH, 4);
                    item.getItemProperty(PropertyId.END)
                            .setValue(cal.getTime());
                    item.getItemProperty(PropertyId.CAPTION)
                            .setValue("Holiday");
                }
                
                {
                    Item item = c.getItem(c.addItem());
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    item.getItemProperty(PropertyId.START)
                            .setValue(cal.getTime());
                    cal.add(Calendar.DAY_OF_MONTH, 6);
                    item.getItemProperty(PropertyId.END)
                            .setValue(cal.getTime());
                    item.getItemProperty(PropertyId.CAPTION)
                            .setValue("Something fun");
                }
                
                {
                    Item item = c.getItem(c.addItem());
                    cal.add(Calendar.DAY_OF_MONTH, 8);
                    item.getItemProperty(PropertyId.START)
                            .setValue(cal.getTime());
                    item.getItemProperty(PropertyId.CAPTION)
                            .setValue("Single day event");
                }
                
                tl.setEventDataSource(c);
            }
        });

        window.addComponent(b);
        window.addComponent(tl);
    }

}
