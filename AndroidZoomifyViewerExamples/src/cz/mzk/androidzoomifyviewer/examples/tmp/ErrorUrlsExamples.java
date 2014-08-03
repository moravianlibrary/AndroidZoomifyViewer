package cz.mzk.androidzoomifyviewer.examples.tmp;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Řehánek
 * 
 */
public class ErrorUrlsExamples {

	public static class Example {
		private final int errorCode;
		private final String errorName;
		private final String url;

		public Example(int errorCode, String errorName, String url) {
			super();
			this.errorCode = errorCode;
			this.errorName = errorName;
			this.url = url;
		}

		public int getErrorCode() {
			return errorCode;
		}

		public String getErrorName() {
			return errorName;
		}

		public String getUrl() {
			return url;
		}

	}

	public static List<Example> getErrorsExamples() {
		List<Example> result = new ArrayList<Example>();
		result.add(new Example(300, "Multiple chocies (loop)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/redirection_loop_300/"));
		result.add(new Example(301, "Moved permanently (loop)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/redirection_loop_301/"));
		result.add(new Example(302, "Found (loop)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/redirection_loop_302/"));
		result.add(new Example(303, "See other (loop)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/redirection_loop_303/"));
		result.add(new Example(307, "Not modified (loop)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/redirection_loop_307/"));
		return result;
	}

	public static List<Example> getResponseExamples() {
		List<Example> result = new ArrayList<Example>();
		// 20x
		result.add(new Example(200, "Ok",
				"http://kramerius.mzk.cz/search/zoomify/uuid:afdd8ea1-ad6f-474c-9611-152cfd3a14b3/"));
		result.add(new Example(200, "Ok", "http://www.britishpanoramics.com/ZoomifyImage02/"));
		result.add(new Example(200, "Ok", "http://mapy.mzk.cz/AA22/0103/"));
		// 30x
		result.add(new Example(300, "Multiple chocies",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/multiple_choices/"));
		result.add(new Example(301, "Moved permanently",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/moved_permanently/"));
		result.add(new Example(302, "Found",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/found/"));
		result.add(new Example(303, "See other",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/see_other/"));
		result.add(new Example(304, "Not modified",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/not_modified/"));
		result.add(new Example(305, "Use proxy",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/use_proxy/"));
		result.add(new Example(307, "Temporary redirect",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/temporary_redirect/"));
		// 4xx
		result.add(new Example(400, "Bad request",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/bad_request/"));
		result.add(new Example(401, "Unauthorized",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/unauthorized/"));
		result.add(new Example(403, "Forbidden",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/forbidden/"));
		result.add(new Example(404, "Not found",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/not_found/"));
		result.add(new Example(407, "Proxy authentication required",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/proxy_authentication_required/"));
		result.add(new Example(408, "Request timeout",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/request_timeout/"));
		result.add(new Example(410, "Gone",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/gone/"));
		// 50x
		result.add(new Example(500, "Internal server error",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/internal_server_error/"));
		result.add(new Example(501, "Not implemented",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/not_implemented/"));
		result.add(new Example(502, "Bad gateway",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/bad_gateway/"));
		result.add(new Example(503, "Service unavailable",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/service_unavailable/"));
		result.add(new Example(504, "Gateway timeout",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/gateway_timeout/"));
		result.add(new Example(505, "Http version not supported",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/http_version_not_supported/"));
		return result;
	}
}
