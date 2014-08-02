package cz.mzk.androidzoomifyviewer.examples.kramerius;

import android.annotation.SuppressLint;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Martin Řehánek
 * 
 */
public class KrameriusObjectPersistentUrl {

	private static final String TAG = KrameriusObjectPersistentUrl.class.getSimpleName();
	// domeny max. 4. radu
	private static Pattern URL_PATTERN = Pattern
			.compile("http(s)?://[a-z0-9]+[a-z0-9-]*[a-z0-9]+(\\.[a-z0-9]+[a-z0-9-]*[a-z0-9]+(\\.[a-z0-9]+[a-z0-9-]*[a-z0-9]+)(\\.[a-z0-9]+[a-z0-9-]*[a-z0-9]+)?)?/search/handle/uuid:([a-z0-9-])+/?");
	private static final String PROTOCOL_SUFFIX = "://";
	private static final String DOMAIN_SUFFIX = "/search/handle/";

	private final String protocol;
	private final String domain;
	private final String pid;
	private String stringValue = null;

	public KrameriusObjectPersistentUrl(String protocol, String domain, String pid) {
		this.protocol = protocol;
		this.domain = domain;
		this.pid = pid;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getDomain() {
		return domain;
	}

	public String getPid() {
		return pid;
	}

	@Override
	public String toString() {
		if (stringValue == null) {
			StringBuilder builder = new StringBuilder();
			builder.append(protocol).append(PROTOCOL_SUFFIX);
			builder.append(domain).append(DOMAIN_SUFFIX);
			builder.append(pid);
			stringValue = builder.toString();
		}
		return stringValue;
	}

	@SuppressLint("DefaultLocale")
	public static KrameriusObjectPersistentUrl valueOf(String url) throws ParseException {
		Matcher matcher = URL_PATTERN.matcher(url.toLowerCase());
		if (!matcher.matches()) {
			throw new ParseException("invalid url '" + url + "'", 123456789);
		}
		String[] tokens = url.toLowerCase().split("/");
		String protocol = tokens[0].substring(0, tokens[0].length() - 1);
		String domain = tokens[2];
		String pid = tokens[5];
		return new KrameriusObjectPersistentUrl(protocol, domain, pid);
	}

}
