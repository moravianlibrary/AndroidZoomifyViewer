package cz.mzk.tiledimageview.cache;

import android.util.LruCache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Martin Řehánek on 7.12.15.
 */
public class CacheKeyBuilder {

    // TODO: 8.12.15 Profiler a otestovat, jestli ma smysl cachovani
    private static final boolean CACHE_KEYS = true;
    private static final Object lock = CACHE_KEYS ? new Object() : null;
    private static final char ESCAPE_CHAR = '_';
    private static final Map<Character, Character> POSSIBLY_RESERVED_CHARS = initPossiblyReservedChars();
    private static final Map<String, Character> ESCAPED_SUBSTRINGS = initEscapedStrings();
    private static final LruCache<String, String> KEY_CACHE = CACHE_KEYS ? new LruCache<String, String>(50) : null;

    private static Map<String, Character> initEscapedStrings() {
        Map<String, Character> map = new HashMap<>();
        map.put("ImageProperties.xml", Character.valueOf('1'));
        map.put("http://", Character.valueOf('2'));
        map.put("https://", Character.valueOf('3'));
        map.put(".jpg", Character.valueOf('4'));
        map.put("TileGroup", Character.valueOf('5'));
        return map;
    }

    private static Map<Character, Character> initPossiblyReservedChars() {
        Map<Character, Character> map = new HashMap<>();
        //Problematic characters in file names according to https://en.wikipedia.org/wiki/Filename#Reserved_characters_and_words
        map.put(Character.valueOf('/'), Character.valueOf('a'));
        map.put(Character.valueOf('\\'), Character.valueOf('b'));
        map.put(Character.valueOf('?'), Character.valueOf('c'));
        map.put(Character.valueOf('%'), Character.valueOf('d'));
        map.put(Character.valueOf('*'), Character.valueOf('e'));
        map.put(Character.valueOf(':'), Character.valueOf('f'));
        map.put(Character.valueOf('|'), Character.valueOf('g'));
        map.put(Character.valueOf('"'), Character.valueOf('h'));
        map.put(Character.valueOf('<'), Character.valueOf('i'));
        map.put(Character.valueOf('>'), Character.valueOf('j'));
        map.put(Character.valueOf('.'), Character.valueOf('k'));
        map.put(Character.valueOf(':'), Character.valueOf('l'));
        map.put(Character.valueOf('('), Character.valueOf('m'));
        map.put(Character.valueOf(')'), Character.valueOf('n'));
        map.put(Character.valueOf('&'), Character.valueOf('o'));
        map.put(Character.valueOf(';'), Character.valueOf('p'));
        map.put(Character.valueOf('#'), Character.valueOf('q'));
        return map;
    }

    // TODO: 8.12.15 Tohle trva relativne dlouho. Pri typickem use casu asi 5 % casu, hlavne kvuli vecne se generujicim klicu pro tiles cache
    // pokud tohle cachovat, tak zase by se to muselo synchronizovat. Krome uzkeho hrdla by mohl hrozit deadlock
    // zatim to vypada, ze by deadlock nastat nemel.
    // ale kdyby se jeste escapovaly "http" "ImageProperties.xml", ".jpg" apod. kvuli zkraceni nazvu souboru, tak to mozna budu vhodne
    public static String buildKeyFromUrl(String url) {
        if (CACHE_KEYS) {
            synchronized (lock) {
                String fromCache = KEY_CACHE.get(url);
                if (fromCache != null) {
                    return fromCache;
                } else {
                    String escapedSubstrings = escapeSubstrings(url);
                    String result = escapeSpecialChars(escapedSubstrings);
                    KEY_CACHE.put(url, result);
                    return result;
                }
            }
        } else {
            String escapedSubstrings = escapeSubstrings(url);
            return escapeSpecialChars(escapedSubstrings);
        }
    }

    private static String escapeSubstrings(String url) {
        Character escapCharacter = Character.valueOf(ESCAPE_CHAR);
        for (String substring : ESCAPED_SUBSTRINGS.keySet()) {
            String replacement = new StringBuilder().append(escapCharacter).append(ESCAPED_SUBSTRINGS.get(substring)).toString();
            url = url.replace(substring, replacement);
        }
        return url;
    }

    private static String escapeSpecialChars(String url) {
        StringBuilder builder = new StringBuilder();
        Set<Character> keys = POSSIBLY_RESERVED_CHARS.keySet();
        for (int i = 0; i < url.length(); i++) {
            char original = url.charAt(i);
            Character key = Character.valueOf(original);
            if (keys.contains(key)) {
                builder.append(ESCAPE_CHAR).append(POSSIBLY_RESERVED_CHARS.get(key));
            } else {
                builder.append(original);
            }
        }
        return builder.toString();
    }

}
