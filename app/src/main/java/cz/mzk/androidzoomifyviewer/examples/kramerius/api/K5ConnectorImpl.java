package cz.mzk.androidzoomifyviewer.examples.kramerius.api;

import java.util.Set;

/**
 * Created by Martin Řehánek on 18.11.15.
 */
public class K5ConnectorImpl implements K5Connector {

    @Override
    public Set<AltoParser.TextBox> getBoxes(String protocol, String domain, String pagePid, String searchQuery) {
        String url = K5Api.getAltoStreamPath(protocol, domain, pagePid);
        String[] searchTokens = searchQuery.split(" ");
        return AltoParser.getTextBlocks(url, searchTokens);
    }
}
