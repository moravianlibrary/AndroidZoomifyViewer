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

	public static List<Example> getImagePropertiesResponseExamples() {
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

	public static List<Example> getImagePropertiesInvalidContentExamples() {
		List<Example> result = new ArrayList<Example>();
		result.add(new Example(
				200,
				"Empty file",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_file/"));
		result.add(new Example(
				200,
				"Empty document",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_document/"));
		result.add(new Example(
				200,
				"Not well formed 1",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/not_well_formed1/"));
		result.add(new Example(
				200,
				"Not well formed 2",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/not_well_formed2/"));
		result.add(new Example(
				200,
				"Invalid root element",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/invalid_root_element/"));
		result.add(new Example(
				200,
				"Missing width",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/missing_width/"));
		result.add(new Example(
				200,
				"Missing height",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/missing_height/"));
		result.add(new Example(
				200,
				"Missing numtiles",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/missing_numtiles/"));
		result.add(new Example(
				200,
				"Missing numimages",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/missing_numimages/"));
		result.add(new Example(
				200,
				"Missing version",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/missing_version/"));
		result.add(new Example(
				200,
				"Missing tilesize",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/missing_tilesize/"));
		result.add(new Example(
				200,
				"Empty width",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_width/"));
		result.add(new Example(
				200,
				"Empty height",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_height/"));
		result.add(new Example(
				200,
				"Empty numtiles",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_numtiles/"));
		result.add(new Example(
				200,
				"Empty numimages",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_numimages/"));
		result.add(new Example(
				200,
				"Empty version",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_version/"));
		result.add(new Example(
				200,
				"Empty tilesize",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_tilesize/"));
		result.add(new Example(
				200,
				"NaN width",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/nan_width/"));
		result.add(new Example(
				200,
				"NaN height",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/nan_height/"));
		result.add(new Example(
				200,
				"NaN numtiles",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/nan_numtiles/"));
		result.add(new Example(
				200,
				"NaN numimages",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/nan_numimages/"));
		result.add(new Example(
				200,
				"NaN version",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/nan_version/"));
		result.add(new Example(
				200,
				"NaN tilesize",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/nan_tilesize/"));
		result.add(new Example(
				200,
				"Unsupported version",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/unsupported_version/"));
		result.add(new Example(
				200,
				"Unsupported numimages",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/unsupported_numimages/"));
		return result;
	}

	public static List<Example> getImagePropertiesOtherErrorsExamples() {
		List<Example> result = new ArrayList<Example>();
		result.add(new Example(
				200,
				"Server not responding (5 s)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/image_properties_other_errors/server_not_responding_5_s/"));
		result.add(new Example(
				200,
				"Server not responding (10 s)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/image_properties_other_errors/server_not_responding_10_s/"));
		result.add(new Example(
				200,
				"Server not responding (15 s)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/image_properties_other_errors/server_not_responding_15_s/"));
		return result;
	}
}
