package cz.mzk.androidzoomifyviewer.examples.kramerius.api;


public class K5Api {

    public static String getAltoStreamPath(String protocol, String domain, String pagePid) {
        return protocol + "://" + domain + "/search/api/v5.0/item/" + pagePid + "/streams/ALTO";
    }
}
