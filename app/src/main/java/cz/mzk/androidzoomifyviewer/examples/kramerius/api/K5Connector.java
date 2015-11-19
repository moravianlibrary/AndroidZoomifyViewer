package cz.mzk.androidzoomifyviewer.examples.kramerius.api;

import java.util.Set;

/**
 * Created by Martin Řehánek on 18.11.15.
 */
public interface K5Connector {

    public Set<AltoParser.TextBox> getBoxes(String protocol, String domain, String pagePid, String searchQuery);

}
