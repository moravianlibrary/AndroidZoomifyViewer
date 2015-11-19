package cz.mzk.androidzoomifyviewer.examples.kramerius.api;

import android.util.Log;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.net.MalformedURLException;
import java.net.URL;


public class XmlParser {

    private static final String TAG = XmlParser.class.getName();

    Document getDocument(String urlString) {
        Document document = null;
        try {
            URL url = new URL(urlString);
            SAXReader reader = new SAXReader();
            reader.setValidation(false);
            reader.setEncoding("UTF-8");
            reader.setStripWhitespaceText(true);
            document = reader.read(url);
        } catch (MalformedURLException ex) {
            Log.e(TAG, String.format("MalformedURLException (%s): %s", urlString, ex.getMessage()));
        } catch (DocumentException ex) {
            Log.e(TAG, String.format("DocumentException (%s): %s", urlString, ex.getMessage()));
            ex.printStackTrace();
        }
        return document;
    }
}
