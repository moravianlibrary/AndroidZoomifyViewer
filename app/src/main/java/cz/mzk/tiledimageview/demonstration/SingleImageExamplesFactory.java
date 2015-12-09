package cz.mzk.tiledimageview.demonstration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Řehánek
 */
public class SingleImageExamplesFactory {

    private static final String BACKEND_URL_PREFIX = "http://mzk-tiledimageview-demo.appspot.com/zoomify/";

    public static List<ImageExampleWithHttpResponseCode> getErrorsExamples() {
        List<ImageExampleWithHttpResponseCode> result = new ArrayList<>();
        result.add(new ImageExampleWithHttpResponseCode(300, "Multiple chocies (loop)", BACKEND_URL_PREFIX + "redirection_loop_300/"));
        result.add(new ImageExampleWithHttpResponseCode(301, "Moved permanently (loop)", BACKEND_URL_PREFIX + "redirection_loop_301/"));
        result.add(new ImageExampleWithHttpResponseCode(302, "Found (loop)", BACKEND_URL_PREFIX + "redirection_loop_302/"));
        result.add(new ImageExampleWithHttpResponseCode(303, "See other (loop)", BACKEND_URL_PREFIX + "redirection_loop_303/"));
        result.add(new ImageExampleWithHttpResponseCode(307, "Not modified (loop)", BACKEND_URL_PREFIX + "redirection_loop_307/"));
        return result;
    }

    public static List<ImageExampleWithHttpResponseCode> getImagePropertiesResponseExamples() {
        List<ImageExampleWithHttpResponseCode> result = new ArrayList<>();
        // 20x
        result.add(new ImageExampleWithHttpResponseCode(200, "Ok", "http://mapy.mzk.cz/AA22/0103/"));
        // 30x
        result.add(new ImageExampleWithHttpResponseCode(300, "Multiple chocies", BACKEND_URL_PREFIX + "multiple_choices/"));
        result.add(new ImageExampleWithHttpResponseCode(301, "Moved permanently", BACKEND_URL_PREFIX + "moved_permanently/"));
        result.add(new ImageExampleWithHttpResponseCode(302, "Found", BACKEND_URL_PREFIX + "found/"));
        result.add(new ImageExampleWithHttpResponseCode(303, "See other", BACKEND_URL_PREFIX + "see_other/"));
        result.add(new ImageExampleWithHttpResponseCode(304, "Not modified", BACKEND_URL_PREFIX + "not_modified/"));
        result.add(new ImageExampleWithHttpResponseCode(305, "Use proxy", BACKEND_URL_PREFIX + "use_proxy/"));
        result.add(new ImageExampleWithHttpResponseCode(307, "Temporary redirect", BACKEND_URL_PREFIX + "temporary_redirect/"));
        // 4xx
        result.add(new ImageExampleWithHttpResponseCode(400, "Bad request", BACKEND_URL_PREFIX + "bad_request/"));
        result.add(new ImageExampleWithHttpResponseCode(401, "Unauthorized", BACKEND_URL_PREFIX + "unauthorized/"));
        result.add(new ImageExampleWithHttpResponseCode(403, "Forbidden", BACKEND_URL_PREFIX + "forbidden/"));
        result.add(new ImageExampleWithHttpResponseCode(404, "Not found", BACKEND_URL_PREFIX + "not_found/"));
        result.add(new ImageExampleWithHttpResponseCode(407, "Proxy authentication required", BACKEND_URL_PREFIX + "proxy_authentication_required/"));
        result.add(new ImageExampleWithHttpResponseCode(408, "Request timeout", BACKEND_URL_PREFIX + "request_timeout/"));
        result.add(new ImageExampleWithHttpResponseCode(410, "Gone", BACKEND_URL_PREFIX + "gone/"));
        // 50x
        result.add(new ImageExampleWithHttpResponseCode(500, "Internal server error", BACKEND_URL_PREFIX + "internal_server_error/"));
        result.add(new ImageExampleWithHttpResponseCode(501, "Not implemented", BACKEND_URL_PREFIX + "not_implemented/"));
        result.add(new ImageExampleWithHttpResponseCode(502, "Bad gateway", BACKEND_URL_PREFIX + "bad_gateway/"));
        result.add(new ImageExampleWithHttpResponseCode(503, "Service unavailable", BACKEND_URL_PREFIX + "service_unavailable/"));
        result.add(new ImageExampleWithHttpResponseCode(504, "Gateway timeout", BACKEND_URL_PREFIX + "gateway_timeout/"));
        result.add(new ImageExampleWithHttpResponseCode(505, "Http version not supported", BACKEND_URL_PREFIX + "http_version_not_supported/"));
        return result;
    }

    public static List<ImageExample> getImagePropertiesInvalidContentExamples() {
        List<ImageExample> result = new ArrayList<>();
        result.add(new ImageExample("Empty file", BACKEND_URL_PREFIX + "incorrect_image_properties/empty_file/"));
        result.add(new ImageExample("Empty document", BACKEND_URL_PREFIX + "incorrect_image_properties/empty_document/"));
        result.add(new ImageExample("Not well formed 1", BACKEND_URL_PREFIX + "incorrect_image_properties/not_well_formed1/"));
        result.add(new ImageExample("Not well formed 2", BACKEND_URL_PREFIX + "incorrect_image_properties/not_well_formed2/"));
        result.add(new ImageExample("Invalid root element", BACKEND_URL_PREFIX + "incorrect_image_properties/invalid_root_element/"));
        result.add(new ImageExample("Missing width", BACKEND_URL_PREFIX + "incorrect_image_properties/missing_width/"));
        result.add(new ImageExample("Missing height", BACKEND_URL_PREFIX + "incorrect_image_properties/missing_height/"));
        result.add(new ImageExample("Missing numtiles", BACKEND_URL_PREFIX + "incorrect_image_properties/missing_numtiles/"));
        result.add(new ImageExample("Missing numimages", BACKEND_URL_PREFIX + "incorrect_image_properties/missing_numimages/"));
        result.add(new ImageExample("Missing version", BACKEND_URL_PREFIX + "incorrect_image_properties/missing_version/"));
        result.add(new ImageExample("Missing tilesize", BACKEND_URL_PREFIX + "incorrect_image_properties/missing_tilesize/"));
        result.add(new ImageExample("Empty width", BACKEND_URL_PREFIX + "incorrect_image_properties/empty_width/"));
        result.add(new ImageExample("Empty height", BACKEND_URL_PREFIX + "incorrect_image_properties/empty_height/"));
        result.add(new ImageExample("Empty numtiles", BACKEND_URL_PREFIX + "incorrect_image_properties/empty_numtiles/"));
        result.add(new ImageExample("Empty numimages", BACKEND_URL_PREFIX + "incorrect_image_properties/empty_numimages/"));
        result.add(new ImageExample("Empty version", BACKEND_URL_PREFIX + "incorrect_image_properties/empty_version/"));
        result.add(new ImageExample("Empty tilesize", BACKEND_URL_PREFIX + "incorrect_image_properties/empty_tilesize/"));
        result.add(new ImageExample("NaN width", BACKEND_URL_PREFIX + "incorrect_image_properties/nan_width/"));
        result.add(new ImageExample("NaN height", BACKEND_URL_PREFIX + "incorrect_image_properties/nan_height/"));
        result.add(new ImageExample("NaN numtiles", BACKEND_URL_PREFIX + "incorrect_image_properties/nan_numtiles/"));
        result.add(new ImageExample("NaN numimages", BACKEND_URL_PREFIX + "incorrect_image_properties/nan_numimages/"));
        result.add(new ImageExample("NaN version", BACKEND_URL_PREFIX + "incorrect_image_properties/nan_version/"));
        result.add(new ImageExample("NaN tilesize", BACKEND_URL_PREFIX + "incorrect_image_properties/nan_tilesize/"));
        result.add(new ImageExample("Unsupported version", BACKEND_URL_PREFIX + "incorrect_image_properties/unsupported_version/"));
        result.add(new ImageExample("Unsupported numimages", BACKEND_URL_PREFIX + "incorrect_image_properties/unsupported_numimages/"));
        return result;
    }

    public static List<ImageExample> getImagePropertiesOtherErrorsExamples() {
        List<ImageExample> result = new ArrayList<>();
        result.add(new ImageExample("Server not responding (5 s)", BACKEND_URL_PREFIX + "image_properties_other_errors/server_not_responding_5_s/"));
        result.add(new ImageExample("Server not responding (10 s)", BACKEND_URL_PREFIX + "image_properties_other_errors/server_not_responding_10_s/"));
        result.add(new ImageExample("Server not responding (15 s)", BACKEND_URL_PREFIX + "image_properties_other_errors/server_not_responding_15_s/"));
        return result;
    }

    public static List<ImageExample> getWorkingExamples() {
        List<ImageExample> result = new ArrayList<>();
        result.add(new ImageExample("https", "https://docker.mzk.cz/search/zoomify/uuid:c254e63a-82da-11e0-bc9f-0050569d679d/"));
        result.add(new ImageExample("tiles problem 1", "http://iris.mzk.cz/tiles/example-data-incorrect-tiles/"));
        result.add(new ImageExample("tiles problem 2", "http://iris.mzk.cz/tiles/example-data-incorrect-tiles2/"));
        result.add(new ImageExample("tiles problem 3", "http://kramerius.mzk.cz/search/zoomify/uuid:a7ed9c10-4726-4b5b-a5c6-9449bb45c4b7/"));
        result.add(new ImageExample("tiles problem 4", "http://kramerius.mzk.cz/search/zoomify/uuid:5de8741e-3f83-49f8-b7a6-274e1f49603b/"));
        result.add(new ImageExample("tiles problem 5", "http://kramerius.mzk.cz/search/zoomify/uuid:66061a92-8299-4f36-bd23-e119d73a8c7e/"));
        result.add(new ImageExample("mapy.mzk.cz 1", "http://mapy.mzk.cz/AA22/0103/"));
        result.add(new ImageExample("mapy.mzk.cz 2", "http://mapy.mzk.cz/AA22/0104/"));
        result.add(new ImageExample("mapy.mzk.cz 3", "http://mapy.mzk.cz/AA22/0105/"));
        result.add(new ImageExample("britishpanoramics.com", "http://www.britishpanoramics.com/ZoomifyImage02/"));
        result.add(new ImageExample("tricedesigns.com 1", "http://www.tricedesigns.com/panoramas/Pemberton-Park-3/"));
        // erroneous hopefully in data
        result.add(new ImageExample("tricedesigns.com 2", "http://www.tricedesigns.com/panoramas/Pemberton-Park-4/Pemberton-Park-4/"));
        result.add(new ImageExample("tricedesigns.com 3", "http://www.tricedesigns.com/panoramas/office-outside/office-outside/"));
        result.add(new ImageExample("kramerius.mzk.cz",
                // "http://kramerius.mzk.cz/search/zoomify/uuid:afdd8ea1-ad6f-474c-9611-152cfd3a14b3/"
                "http://kramerius.mzk.cz/search/zoomify/uuid:5de8741e-3f83-49f8-b7a6-274e1f49603b/"));
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
}
