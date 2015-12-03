package cz.mzk.androidzoomifyviewer.examples.kramerius.api;

import android.graphics.Rect;
import android.util.Log;
import android.util.LruCache;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.XPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Martin Řehánek on 13.11.15.
 */
public class AltoParser extends XmlParser {

    private static final String TAG = XmlParser.class.getSimpleName();

    private static final Namespace ALTO_V2 = new Namespace("alto_v2", "http://www.loc.gov/standards/alto/ns-v2#");
    //characters filtered from alto_example Strings (or searched queries): \-?!;)(.|,
    private static final char[] FILTERED_CHARS = {'\\', '-', '?', '!', ';', ')', '(', '.', '|', ','};
    private static LruCache<String, AltoParser> processorCache = new LruCache<>(10);
    // TODO: 18.11.15 If eventually only exact matches will be needed:
    //private final Map<String, Rect> mRectMap;
    private final List<TextBox> mTextBoxes;

    public AltoParser(String url) {
        Log.v(TAG, "initalizing alto parser from: " + url);
        Document document = getDocument(url);
        if (document != null) {
            mTextBoxes = extractTextBlocks(document);
        } else {
            mTextBoxes = Collections.emptyList();
        }
    }

    public static Set<TextBox> getTextBlocks(String altoUrl, String[] tokens) {
        if (tokens.length == 0 || tokens[0].isEmpty()) {
            return Collections.emptySet();
        } else {
            AltoParser instance = processorCache.get(altoUrl);
            if (instance == null) {
                instance = new AltoParser(altoUrl);
                processorCache.put(altoUrl, instance);
            }
            return instance.getTextBlocks(tokens);
        }
    }

    private List<TextBox> extractTextBlocks(Document document) {
        List<TextBox> results = new ArrayList<>();
        Map namespaces = new HashMap();
        namespaces.put(ALTO_V2.getPrefix(), ALTO_V2.getURI());
        XPath xpath = document.createXPath("//alto_v2:String");
        xpath.setNamespaceURIs(namespaces);
        List<Node> stringEls = xpath.selectNodes(document);
        for (Node stringEl : stringEls) {
            TextBox box = xmlElementToTextBlock((Element) stringEl);
            if (box != null) {
                results.add(box);
            }
        }
        return results;

    }

    private TextBox xmlElementToTextBlock(Element stringEl) {
        String string = normalizeString(stringEl.attribute("CONTENT").getValue());
        if (string.isEmpty()) {
            return null;
        }
        int left = Integer.valueOf(stringEl.attribute("HPOS").getValue());
        int top = Integer.valueOf(stringEl.attribute("VPOS").getValue());
        int right = left + Integer.valueOf(stringEl.attribute("WIDTH").getValue());
        int bottom = top + Integer.valueOf(stringEl.attribute("HEIGHT").getValue());
        Rect rect = new Rect(left, top, right, bottom);
        Log.v(TAG, String.format("token: '%s'", string));
        return new TextBox(string, rect);
    }

    private String normalizeString(String token) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (isFilteredChar(c)) {
                continue;
            } else {
                builder.append(Character.toLowerCase(c));
            }
        }
        return builder.toString();
    }

    private boolean isFilteredChar(char c) {
        for (char filtered : FILTERED_CHARS) {
            if (c == filtered) {
                return true;
            }
        }
        return false;
    }

    public Set<TextBox> getTextBlocks(String[] tokens) {
        String[] tokensNormalized = new String[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            tokensNormalized[i] = normalizeString(tokens[i]);
        }
        Set<TextBox> results = new HashSet<>();
        for (TextBox textBox : mTextBoxes) {
            for (String token : tokensNormalized) {
                if (!token.isEmpty()) {
                    //if (textBox.getNormalizedString().contains(token)) {
                    if (textBox.getNormalizedString().equals(token)) {
                        results.add(textBox);
                        break;
                    }
                }
            }
        }
        return results;
    }

    public static class TextBox {

        private final String mNormalizedString;
        private final Rect mRectangle;

        public TextBox(String normalizedString, Rect rectangle) {
            mNormalizedString = normalizedString;
            mRectangle = rectangle;
        }

        public String getNormalizedString() {
            return mNormalizedString;
        }

        public Rect getRectangle() {
            return mRectangle;
        }
    }
}
