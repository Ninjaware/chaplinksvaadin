package fi.ninjaware.chaplinksvaadin.gwt.client.timeline;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsDate;
import java.util.Date;

/**
 *
 * @author miku
 */
public class VTimelineEvent extends JavaScriptObject {

    protected VTimelineEvent() {
    }

    public final native JsDate getStartNative()/*-{ 
     return this.start; 
     }-*/;

    public final native JsDate getEndNative()/*-{ 
     return this.end; 
     }-*/;

    public final native String getContent()/*-{ 
     return this.content; 
     }-*/;

    public final native String getGroup()/*-{ 
     return this.group; 
     }-*/;

    public final Date getStartDate() {
        return new Date((long) getStartNative().getTime());
    }
    
    public final Date getEndDate() {
        return new Date((long) getEndNative().getTime());
    }

    public final String[] getSerialized() {
        String group = getGroup();
        String[] srlzd = new String[group != null ? 4 : 3];
        
        srlzd[0] = String.valueOf((long) getStartDate().getTime());
        srlzd[1] = String.valueOf((long) getEndDate().getTime());
        String content = getContent();
        srlzd[2] = (content != null ? content : "");
        
        if(group != null) {
            srlzd[3] = group;
        }
        
        return srlzd;
    }
}
