package cz.mzk.androidzoomifyviewer.examples.kramerius;

/**
 * @author Martin Řehánek
 * 
 */
public class PageDataSource {

	private final String protocol;
	private final String domain;
	private final String pagePid;
	private String tilesBaseUrl = null;

	public PageDataSource(String protocol, String domain, String pagePid) {
		super();
		this.protocol = protocol;
		this.domain = domain;
		this.pagePid = pagePid;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getDomain() {
		return domain;
	}

	public String getPagePid() {
		return pagePid;
	}

	/**
	 * This String is being lazy-created and stored within this object so don't
	 * worry to call this multiple times.
	 */
	public String toZoomifyBaseUrl() {
		if (tilesBaseUrl == null) {
			tilesBaseUrl = protocol + "://" + domain + "/search/zoomify/" + pagePid + "/";
		}
		return tilesBaseUrl;
	}

}
