package cz.mzk.androidzoomifyviewer.examples.test;

import java.text.ParseException;

import junit.framework.TestCase;
import android.annotation.SuppressLint;
import cz.mzk.androidzoomifyviewer.examples.kramerius.KrameriusObjectPersistentUrl;

/**
 * @author Martin Řehánek
 * 
 */
public class KrameriusObjectPersistentUrlTest extends TestCase {

	private KrameriusObjectPersistentUrl parseUrl(String url) throws ParseException {
		// System.out.println("parsing '" + url + "'");
		return KrameriusObjectPersistentUrl.valueOf(url);
	}

	// TODO: povolit i porty treba 8080?

	@SuppressLint("DefaultLocale")
	public void testProtocols() {
		try {
			KrameriusObjectPersistentUrl url1 = parseUrl("http://docker.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22");
			assertEquals("http", url1.getProtocol());
			assertEquals("docker.mzk.cz", url1.getDomain());
			assertEquals("uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22", url1.getPid());
			assertEquals("http://docker.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22/",
					url1.toString());

			KrameriusObjectPersistentUrl url2 = parseUrl("http://docker.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22"
					.toUpperCase());
			assertEquals("http", url2.getProtocol());
			assertEquals("docker.mzk.cz", url2.getDomain());
			assertEquals("uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22", url2.getPid());
			assertEquals("http://docker.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22/",
					url2.toString());

			KrameriusObjectPersistentUrl url3 = parseUrl("https://docker.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22");
			assertEquals("https", url3.getProtocol());
			assertEquals("docker.mzk.cz", url3.getDomain());
			assertEquals("uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22", url3.getPid());
			assertEquals("https://docker.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22/",
					url3.toString());

			KrameriusObjectPersistentUrl url4 = parseUrl("https://docker.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22"
					.toUpperCase());
			assertEquals("https", url4.getProtocol());
			assertEquals("docker.mzk.cz", url4.getDomain());
			assertEquals("uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22", url4.getPid());
			assertEquals("https://docker.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22/",
					url4.toString());

		} catch (ParseException e) {
			// e.printStackTrace();
			fail(e.getMessage());
		}

		String[] invalidProtocols = { "ssl", "ftp", "git" };
		for (String protocol : invalidProtocols) {
			try {
				parseUrl(protocol + "://docker.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22");
				fail();
			} catch (ParseException e) {
				// OK
			}
		}

		try {
			parseUrl("http//docker.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22");
			fail();
		} catch (ParseException e) {
			// OK
		}

		try {
			parseUrl("http:/docker.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22");
			fail();
		} catch (ParseException e) {
			// OK
		}
	}

	public void testCorrectDomains() {
		String[] domains = { "kramerius.mzk.cz", "kramerius-test.nkp.cz", "kramerius.fi.muni.cz", "a-b.12.3-4.cz" };
		for (String domain : domains) {
			try {
				KrameriusObjectPersistentUrl url = parseUrl("http://" + domain
						+ "/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22");
				assertEquals("http", url.getProtocol());
				assertEquals(domain, url.getDomain());
				assertEquals("uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22", url.getPid());
			} catch (ParseException e) {
				// e.printStackTrace();
				fail(e.getMessage());
			}
		}
	}

	public void testIncorrectDomains() {
		String[] domainsOk = { "kramerius.mzk.c", "kramerius.mzk.-c-", "kramerius.mzk.-c", "kramerius.mzk.c-",
				"kramerius.s2.d3.mzk.cz", };
		for (String domain : domainsOk) {
			try {
				parseUrl("http://" + domain + "/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22");
				fail();
			} catch (ParseException e) {
				// OK
			}
		}
	}

	public void testIncorrectSuffix() {
		try {
			parseUrl("http://docker.mzk.cz/search/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22");
			fail();
		} catch (ParseException e) {
			// OK
		}

		try {
			parseUrl("http://docker.mzk.cz/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22");
			fail();
		} catch (ParseException e) {
			// OK
		}

		try {
			parseUrl("http://docker.mzk.cz/searchhandle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22");
			fail();
		} catch (ParseException e) {
			// OK
		}
	}

	public void testIncorrectPid() {
		try {
			parseUrl("http://docker.mzk.cz/search/handle/");
			fail();
		} catch (ParseException e) {
			// OK
		}
		try {
			parseUrl("http://docker.mzk.cz/search/handle/uuid");
			fail();
		} catch (ParseException e) {
			// OK
		}
		try {
			parseUrl("http://docker.mzk.cz/search/handle/uuid:");
			fail();
		} catch (ParseException e) {
			// OK
		}

		try {
			KrameriusObjectPersistentUrl url = parseUrl("http://docker.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22/");
			assertEquals("http", url.getProtocol());
			assertEquals("docker.mzk.cz", url.getDomain());
			assertEquals("uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22", url.getPid());
			assertEquals("http://docker.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22/",
					url.toString());
		} catch (ParseException e) {
			// e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
