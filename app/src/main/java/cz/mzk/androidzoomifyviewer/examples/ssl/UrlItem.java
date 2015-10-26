package cz.mzk.androidzoomifyviewer.examples.ssl;

import java.util.ArrayList;
import java.util.List;

public class UrlItem {

	private final String url;
	private final String info;

	public UrlItem(String url, String info) {
		super();
		this.url = url;
		this.info = info;
	}

	public String getUrl() {
		return url;
	}

	public String getInfo() {
		return info;
	}

	public static List<UrlItem> getTestData() {
		List<UrlItem> result = new ArrayList<UrlItem>();
		result.add(new UrlItem("https://google.com", "Google"));
		result.add(new UrlItem("https://kramerius.lib.cas.cz/search/handle/uuid:4240f893-7853-4088-9a07-a6cdb81f3631",
				"TERENA - ok"));
		result.add(new UrlItem("https://docker.mzk.cz/search/handle/uuid:8ffd7a5b-82da-11e0-bc9f-0050569d679d",
				"TERENA - ok"));
		result.add(new UrlItem(
				"https://docker.mzk.cz/search/api/v5.0/item/uuid:8ffd7a5b-82da-11e0-bc9f-0050569d679d/children",
				"TERENA - ok"));
		result.add(new UrlItem("https://docker.mzk.cz/search/i.jsp?pid=uuid:3bc06ae3-5a91-4967-a51b-6dfd69b80a87",
				"TERENA - ok"));
		result.add(new UrlItem("https://kramerius.fsv.cuni.cz/search/handle/uuid:9711c868-2fd1-11e0-a23a-0050569d679d",
				"TERENA, invalid date"));
		result.add(new UrlItem(
				"https://krameriusndktest.mzk.cz/search/handle/uuid:70680130-01ff-11e4-9789-005056827e52",
				"TERENA, invalid common name"));
		result.add(new UrlItem("https://kramerius.zcm.cz/search/handle/uuid:981c2b8d-2042-11e3-88f5-001b63bd97ba",
				"StartCom, invalid common name"));

		result.add(new UrlItem("https://certs.cac.washington.edu/CAtest/", "custom certificate test"));

		// result.add(new UrlItem("", ""));

		return result;
	}

}
