package fi.ninjaware.chaplinksvaadin;

import com.vaadin.terminal.Resource;
import com.vaadin.ui.Alignment;
import fi.ninjaware.chaplinksvaadin.Timeline.EventType;
import java.io.Serializable;
import java.util.Date;

/**
 * A single event on Timeline.
 * @author miku
 */
public class TimelineEvent implements Serializable {
    
    /**
     * Start date.
     */
    private Date start;
    
    /**
     * End date.
     */
    private Date end;
    
    /**
     * Text content.
     */
    private String content;
    
    /**
     * Event group.
     */
    private String group;
    
    /**
     * CSS class name.
     */
    private String className;
    
    /**
     * Whether the event is editable.
     */
    private boolean editable;
    
    /**
     * Event type.
     */
    private EventType type;
    
    /**
     * Event icon.
     */
    private Resource icon;
    
    /**
     * Event icon alignment.
     */
    private Alignment iconAlignment;

    public TimelineEvent() {
    }

    public TimelineEvent(Date start, String content) {
        this.start = start;
        this.content = content;
    }

    public TimelineEvent(Date start, Date end, String content) {
        this.start = start;
        this.end = end;
        this.content = content;
    }

    public TimelineEvent(Date start, Date end, String content, String group) {
        this.start = start;
        this.end = end;
        this.content = content;
        this.group = group;
    }
    
    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Resource getIcon() {
        return icon;
    }

    public void setIcon(Resource icon) {
        this.icon = icon;
    }

    public Alignment getIconAlignment() {
        return iconAlignment;
    }

    public void setIconAlignment(Alignment iconAlignment) {
        this.iconAlignment = iconAlignment;
    }
    
}
