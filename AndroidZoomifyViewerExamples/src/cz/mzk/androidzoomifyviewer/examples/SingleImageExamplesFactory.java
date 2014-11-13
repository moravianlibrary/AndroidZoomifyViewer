package cz.mzk.androidzoomifyviewer.examples;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Řehánek
 * 
 */
public class SingleImageExamplesFactory {

	public static class ImageExample {
		private final String description;
		private final String url;

		public ImageExample(String description, String url) {
			super();
			this.description = description;
			this.url = url;
		}

		public String getErrorName() {
			return description;
		}

		public String getUrl() {
			return url;
		}
	}

	public static class ImageExampleWithHttpResponseCode extends ImageExample {

		private final int errorCode;

		public ImageExampleWithHttpResponseCode(int errorCode, String errorName, String url) {
			super(errorName, url);
			this.errorCode = errorCode;
		}

		public int getErrorCode() {
			return errorCode;
		}
	}

	public static List<ImageExampleWithHttpResponseCode> getErrorsExamples() {
		List<ImageExampleWithHttpResponseCode> result = new ArrayList<ImageExampleWithHttpResponseCode>();
		result.add(new ImageExampleWithHttpResponseCode(300, "Multiple chocies (loop)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/redirection_loop_300/"));
		result.add(new ImageExampleWithHttpResponseCode(301, "Moved permanently (loop)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/redirection_loop_301/"));
		result.add(new ImageExampleWithHttpResponseCode(302, "Found (loop)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/redirection_loop_302/"));
		result.add(new ImageExampleWithHttpResponseCode(303, "See other (loop)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/redirection_loop_303/"));
		result.add(new ImageExampleWithHttpResponseCode(307, "Not modified (loop)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/redirection_loop_307/"));
		return result;
	}

	public static List<ImageExampleWithHttpResponseCode> getImagePropertiesResponseExamples() {
		List<ImageExampleWithHttpResponseCode> result = new ArrayList<ImageExampleWithHttpResponseCode>();
		// 20x
		result.add(new ImageExampleWithHttpResponseCode(200, "Ok", "http://mapy.mzk.cz/AA22/0103/"));
		// 30x
		result.add(new ImageExampleWithHttpResponseCode(300, "Multiple chocies",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/multiple_choices/"));
		result.add(new ImageExampleWithHttpResponseCode(301, "Moved permanently",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/moved_permanently/"));
		result.add(new ImageExampleWithHttpResponseCode(302, "Found",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/found/"));
		result.add(new ImageExampleWithHttpResponseCode(303, "See other",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/see_other/"));
		result.add(new ImageExampleWithHttpResponseCode(304, "Not modified",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/not_modified/"));
		result.add(new ImageExampleWithHttpResponseCode(305, "Use proxy",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/use_proxy/"));
		result.add(new ImageExampleWithHttpResponseCode(307, "Temporary redirect",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/temporary_redirect/"));
		// 4xx
		result.add(new ImageExampleWithHttpResponseCode(400, "Bad request",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/bad_request/"));
		result.add(new ImageExampleWithHttpResponseCode(401, "Unauthorized",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/unauthorized/"));
		result.add(new ImageExampleWithHttpResponseCode(403, "Forbidden",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/forbidden/"));
		result.add(new ImageExampleWithHttpResponseCode(404, "Not found",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/not_found/"));
		result.add(new ImageExampleWithHttpResponseCode(407, "Proxy authentication required",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/proxy_authentication_required/"));
		result.add(new ImageExampleWithHttpResponseCode(408, "Request timeout",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/request_timeout/"));
		result.add(new ImageExampleWithHttpResponseCode(410, "Gone",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/gone/"));
		// 50x
		result.add(new ImageExampleWithHttpResponseCode(500, "Internal server error",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/internal_server_error/"));
		result.add(new ImageExampleWithHttpResponseCode(501, "Not implemented",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/not_implemented/"));
		result.add(new ImageExampleWithHttpResponseCode(502, "Bad gateway",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/bad_gateway/"));
		result.add(new ImageExampleWithHttpResponseCode(503, "Service unavailable",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/service_unavailable/"));
		result.add(new ImageExampleWithHttpResponseCode(504, "Gateway timeout",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/gateway_timeout/"));
		result.add(new ImageExampleWithHttpResponseCode(505, "Http version not supported",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/http_version_not_supported/"));
		return result;
	}

	public static List<ImageExample> getImagePropertiesInvalidContentExamples() {
		List<ImageExample> result = new ArrayList<ImageExample>();
		result.add(new ImageExample(
				"Empty file",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_file/"));
		result.add(new ImageExample(
				"Empty document",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_document/"));
		result.add(new ImageExample(
				"Not well formed 1",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/not_well_formed1/"));
		result.add(new ImageExample(
				"Not well formed 2",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/not_well_formed2/"));
		result.add(new ImageExample(
				"Invalid root element",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/invalid_root_element/"));
		result.add(new ImageExample(
				"Missing width",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/missing_width/"));
		result.add(new ImageExample(
				"Missing height",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/missing_height/"));
		result.add(new ImageExample(
				"Missing numtiles",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/missing_numtiles/"));
		result.add(new ImageExample(
				"Missing numimages",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/missing_numimages/"));
		result.add(new ImageExample(
				"Missing version",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/missing_version/"));
		result.add(new ImageExample(
				"Missing tilesize",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/missing_tilesize/"));
		result.add(new ImageExample(
				"Empty width",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_width/"));
		result.add(new ImageExample(
				"Empty height",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_height/"));
		result.add(new ImageExample(
				"Empty numtiles",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_numtiles/"));
		result.add(new ImageExample(
				"Empty numimages",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_numimages/"));
		result.add(new ImageExample(
				"Empty version",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_version/"));
		result.add(new ImageExample(
				"Empty tilesize",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/empty_tilesize/"));
		result.add(new ImageExample(
				"NaN width",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/nan_width/"));
		result.add(new ImageExample(
				"NaN height",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/nan_height/"));
		result.add(new ImageExample(
				"NaN numtiles",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/nan_numtiles/"));
		result.add(new ImageExample(
				"NaN numimages",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/nan_numimages/"));
		result.add(new ImageExample(
				"NaN version",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/nan_version/"));
		result.add(new ImageExample(
				"NaN tilesize",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/nan_tilesize/"));
		result.add(new ImageExample(
				"Unsupported version",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/unsupported_version/"));
		result.add(new ImageExample(
				"Unsupported numimages",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/incorrect_image_properties/unsupported_numimages/"));
		return result;
	}

	public static List<ImageExample> getImagePropertiesOtherErrorsExamples() {
		List<ImageExample> result = new ArrayList<ImageExample>();
		result.add(new ImageExample(
				"Server not responding (5 s)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/image_properties_other_errors/server_not_responding_5_s/"));
		result.add(new ImageExample(
				"Server not responding (10 s)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/image_properties_other_errors/server_not_responding_10_s/"));
		result.add(new ImageExample(
				"Server not responding (15 s)",
				"http://rzeh4n-test-androidzoomify.appspot.com/androidzoomifyviewerexamplesbackend/image_properties_other_errors/server_not_responding_15_s/"));
		return result;
	}

	public static List<ImageExample> getWorkingExamples() {
		List<ImageExample> result = new ArrayList<ImageExample>();
		// result.add(new ImageExample("tiles problem 1", "http://iris.mzk.cz/tiles/example-data-incorrect-tiles/"));
		// result.add(new ImageExample("tiles problem 2", "http://iris.mzk.cz/tiles/example-data-incorrect-tiles2/"));
		result.add(new ImageExample("mapy.mzk.cz 1", "http://mapy.mzk.cz/AA22/0103/"));
		result.add(new ImageExample("mapy.mzk.cz 2", "http://mapy.mzk.cz/AA22/0104/"));
		result.add(new ImageExample("mapy.mzk.cz 3", "http://mapy.mzk.cz/AA22/0105/"));
		result.add(new ImageExample("britishpanoramics.com", "http://www.britishpanoramics.com/ZoomifyImage02/"));
		result.add(new ImageExample("tricedesigns.com 1", "http://www.tricedesigns.com/panoramas/Pemberton-Park-3/"));
		// erroneous hopefully in data
		result.add(new ImageExample("tricedesigns.com 2",
				"http://www.tricedesigns.com/panoramas/Pemberton-Park-4/Pemberton-Park-4/"));
		result.add(new ImageExample("tricedesigns.com 3",
				"http://www.tricedesigns.com/panoramas/office-outside/office-outside/"));
		result.add(new ImageExample("kramerius.mzk.cz",
				"http://kramerius.mzk.cz/search/zoomify/uuid:afdd8ea1-ad6f-474c-9611-152cfd3a14b3/"));
		// http://www.fookes.com/ezimager/zoomify/classic/
		result.add(new ImageExample("Gruyère Panorama", "http://www.fookes.com/ezimager/zoomify/104_0486/"));
		result.add(new ImageExample("La Chaudalla, Charmey", "http://www.fookes.com/ezimager/zoomify/109_0977/"));
		result.add(new ImageExample("Panorama from Vounetz", "http://www.fookes.com/ezimager/zoomify/105_0532/"));
		result.add(new ImageExample("Medieval Town of Gruyères", "http://www.fookes.com/ezimager/zoomify/122_2259/"));
		result.add(new ImageExample("Stained Glass Window", "http://www.fookes.com/ezimager/zoomify/122_2209/"));
		result.add(new ImageExample("Gruyères Castle and Church", "http://www.fookes.com/ezimager/zoomify/122_2264/"));
		// result.add(new ImageExample("", ""));
		// test
		// result.add(new ImageExample("localhost test",
		// "http://10.0.0.2:8888/preview/07AED/"));
		return result;
	}
}
