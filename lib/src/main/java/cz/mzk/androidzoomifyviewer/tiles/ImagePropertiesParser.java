package cz.mzk.androidzoomifyviewer.tiles;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

import cz.mzk.androidzoomifyviewer.tiles.exceptions.InvalidDataException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.OtherIOException;

/**
 * Created by Martin Řehánek on 3.12.15.
 */
public class ImagePropertiesParser {

    public static ImageProperties parse(String propertiesXml, String imagePropertiesUrl) throws InvalidDataException, OtherIOException {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(propertiesXml));
            boolean elementImagePropertiesStarted = false;
            boolean elementImagePropertiesEnded = false;
            int width = -1;
            int height = -1;
            int numtiles = -1; // pro kontrolu
            int numimages = -1;
            double version = 0.0;
            int tileSize = -1;
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (!xpp.getName().equals("IMAGE_PROPERTIES")) {
                        throw new InvalidDataException(imagePropertiesUrl, "Unexpected element " + xpp.getName());
                    } else {
                        elementImagePropertiesStarted = true;
                        width = getAttributeIntegerValue(xpp, "WIDTH", imagePropertiesUrl);
                        height = getAttributeIntegerValue(xpp, "HEIGHT", imagePropertiesUrl);
                        numtiles = getAttributeIntegerValue(xpp, "NUMTILES", imagePropertiesUrl);
                        numimages = getAttributeIntegerValue(xpp, "NUMIMAGES", imagePropertiesUrl);
                        tileSize = getAttributeIntegerValue(xpp, "TILESIZE", imagePropertiesUrl);
                        version = getAttributeDoubleValue(xpp, "VERSION", imagePropertiesUrl);
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (!xpp.getName().equals("IMAGE_PROPERTIES")) {
                        throw new InvalidDataException(imagePropertiesUrl, "Unexpected element " + xpp.getName());
                    } else {
                        elementImagePropertiesEnded = true;
                    }
                }
                eventType = xpp.next();
            }
            if (!elementImagePropertiesStarted) {
                throw new InvalidDataException(imagePropertiesUrl, "Element IMAGE_PROPERTIES not found");
            }
            if (!elementImagePropertiesEnded) {
                throw new InvalidDataException(imagePropertiesUrl, "Element IMAGE_PROPERTIES not closed");
            }
            if (version != 1.8) {
                throw new InvalidDataException(imagePropertiesUrl, "Unsupported tiles version: " + version);
            }
            if (numimages != 1) {
                throw new InvalidDataException(imagePropertiesUrl, "Unsupported number of images: " + numimages);
            }
            // TODO: check numTiles
            return new ImageProperties(width, height, numtiles, tileSize);
        } catch (XmlPullParserException e1) {
            throw new InvalidDataException(imagePropertiesUrl, e1.getMessage());
        } catch (IOException e) {
            throw new OtherIOException(e.getMessage(), imagePropertiesUrl);
        }
    }

    private static Double getAttributeDoubleValue(XmlPullParser xpp, String attrName, String imagePropertiesUrl) throws InvalidDataException {
        String attrValue = getAttributeStringValue(xpp, attrName, imagePropertiesUrl);
        try {
            return Double.valueOf(attrValue);
        } catch (NumberFormatException e) {
            throw new InvalidDataException(imagePropertiesUrl, "invalid content of attribute " + attrName + ": '"
                    + attrValue + "'");
        }
    }

    private static int getAttributeIntegerValue(XmlPullParser xpp, String attrName, String imagePropertiesUrl) throws InvalidDataException {
        String attrValue = getAttributeStringValue(xpp, attrName, imagePropertiesUrl);
        try {
            return Integer.valueOf(attrValue);
        } catch (NumberFormatException e) {
            throw new InvalidDataException(imagePropertiesUrl, "invalid content of attribute " + attrName + ": '"
                    + attrValue + "'");
        }
    }

    private static String getAttributeStringValue(XmlPullParser xpp, String attrName, String imagePropertiesUrl) throws InvalidDataException {
        String attrValue = xpp.getAttributeValue(null, attrName);
        if (attrValue == null) {
            throw new InvalidDataException(imagePropertiesUrl, "missing attribute " + attrName);
        }
        if (attrValue.isEmpty()) {
            throw new InvalidDataException(imagePropertiesUrl, "empty attribute " + attrName);
        }
        return attrValue;
    }
}
