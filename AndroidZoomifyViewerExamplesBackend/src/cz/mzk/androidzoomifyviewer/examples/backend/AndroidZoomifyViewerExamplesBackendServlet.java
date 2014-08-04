package cz.mzk.androidzoomifyviewer.examples.backend;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.http.*;

@SuppressWarnings("serial")
public class AndroidZoomifyViewerExamplesBackendServlet extends HttpServlet {

	// private static final String OK1 = "http://mapy.mzk.cz/AA22/0103";
	private static final String OK1 = "http://kramerius.mzk.cz/search/zoomify/uuid:afdd8ea1-ad6f-474c-9611-152cfd3a14b3/";
	private static final String OK2 = "http://www.britishpanoramics.com/ZoomifyImage02/";
	private static final String OK3 = "http://mapy.mzk.cz/AA22/0105/";
	private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (req.getPathInfo() == null || req.getPathInfo().length() <= 1) {
			// just "/" or ""
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		String[] servletUrlTokens = extractUrlTokens(req);
		String imageId = servletUrlTokens[0];
		// 2xx
		if ("ok".equals(imageId) || "ok2".equals(imageId) || "ok3".equals(imageId)) {
			// TODO
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		// 30x
		// TODO: kazdy typ presmerovani na jiny objekt
		else if ("multiple_choices".equals(imageId)) { // 300
			// primarni
			// servletUrlTokens[0] = "ok";
			// String first = buildUrl(req, servletUrlTokens);
			// servletUrlTokens[0] = "ok2";
			// String second = buildUrl(req, servletUrlTokens);
			// servletUrlTokens[0] = "ok3";
			// String third = buildUrl(req, servletUrlTokens);
			String first = buildUrl(OK1, servletUrlTokens);
			String second = buildUrl(OK2, servletUrlTokens);
			String third = buildUrl(OK3, servletUrlTokens);

			PrintWriter writer = resp.getWriter();
			writer.println(first);
			writer.println(second);
			writer.println(third);

			resp.setHeader("Location", first);
			resp.setStatus(HttpServletResponse.SC_MULTIPLE_CHOICES);
			return;
		} else if ("moved_permanently".equals(imageId)) { // 301
			// servletUrlTokens[0] = "ok";
			// resp.setHeader("Location", buildUrl(req, servletUrlTokens));
			resp.setHeader("Location", buildUrl(OK1, servletUrlTokens));
			resp.sendError(HttpServletResponse.SC_MOVED_PERMANENTLY);
			return;
		} else if ("found".equals(imageId)) {// 302
			// servletUrlTokens[0] = "ok";
			// resp.setHeader("Location", buildUrl(req, servletUrlTokens));
			resp.setHeader("Location", buildUrl(OK1, servletUrlTokens));
			resp.sendError(HttpServletResponse.SC_FOUND);
			return;
		} else if ("see_other".equals(imageId)) {// 303
			// servletUrlTokens[0] = "ok";
			// resp.setHeader("Location", buildUrl(req, servletUrlTokens));
			resp.setHeader("Location", buildUrl(OK1, servletUrlTokens));
			resp.sendError(HttpServletResponse.SC_SEE_OTHER);
			return;
		} else if ("not_modified".equals(imageId)) { // 304
			// TODO
			// resp.sendError(HttpServletResponse.SC_NOT_MODIF7IED);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} else if ("use_proxy".equals(imageId)) { // 305
			// servletUrlTokens[0] = "ok";
			// resp.setHeader("Location", buildUrl(req, servletUrlTokens));
			resp.setHeader("Location", buildUrl(OK1, servletUrlTokens));
			resp.sendError(HttpServletResponse.SC_USE_PROXY);
			return;
		} else if ("temporary_redirect".equals(imageId)) { // 307
			// servletUrlTokens[0] = "ok";
			// resp.setHeader("Location", buildUrl(req, servletUrlTokens));
			resp.setHeader("Location", buildUrl(OK1, servletUrlTokens));
			resp.sendError(HttpServletResponse.SC_TEMPORARY_REDIRECT);
			return;
		}

		// 4xx
		else if ("bad_request".equals(imageId)) { // 400
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		} else if ("unauthorized".equals(imageId)) { // 401
			// TODO
			// resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} else if ("forbidden".equals(imageId)) { // 403
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		} else if ("not_found".equals(imageId)) {// 404
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} else if ("proxy_authentication_required".equals(imageId)) {// 407
			// TODO
			// resp.sendError(HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} else if ("request_timeout".equals(imageId)) {// 408
			resp.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT);
			return;
		} else if ("gone".equals(imageId)) {// 410
			resp.sendError(HttpServletResponse.SC_GONE);
			return;
		}
		// 5xx
		else if ("internal_server_error".equals(imageId)) {// 500
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		} else if ("not_implemented".equals(imageId)) {// 501
			resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
			return;
		} else if ("bad_gateway".equals(imageId)) {// 502
			resp.sendError(HttpServletResponse.SC_BAD_GATEWAY);
			return;
		} else if ("service_unavailable".equals(imageId)) {// 503
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			return;
		} else if ("gateway_timeout".equals(imageId)) {// 504
			resp.sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT);
			return;
		} else if ("http_version_not_supported".equals(imageId)) {// 505
			resp.sendError(HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED);
			return;
		}

		// redirection loop

		if ("redirection_loop_300".equals(imageId)) {
			String first = buildUrl(req, servletUrlTokens);
			PrintWriter writer = resp.getWriter();
			writer.println(first);
			writer.println(first);
			writer.println(first);
			resp.setHeader("Location", first);
			resp.sendError(300);
			return;
		} else if ("redirection_loop_301".equals(imageId)) {
			resp.setHeader("Location", buildUrl(req, servletUrlTokens));
			resp.sendError(301);
			return;
		} else if ("redirection_loop_302".equals(imageId)) {
			resp.setHeader("Location", buildUrl(req, servletUrlTokens));
			resp.sendError(302);
			return;
		} else if ("redirection_loop_303".equals(imageId)) {
			resp.setHeader("Location", buildUrl(req, servletUrlTokens));
			resp.sendError(303);
			return;
		} else if ("redirection_loop_307".equals(imageId)) {
			resp.setHeader("Location", buildUrl(req, servletUrlTokens));
			resp.sendError(307);
			return;
		}
		// invalid imageProperties
		else if ("incorrect_image_properties".equals(imageId)) {
			imagePropertiesInvalidContentError(resp, servletUrlTokens);
			return;
		}
		// other image properties errors
		else if ("image_properties_other_errors".equals(imageId)) {
			imagePropertiesOtherError(resp, servletUrlTokens);
		}

		// otherwise
		else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
	}

	private void imagePropertiesOtherError(HttpServletResponse resp, String[] servletUrlTokens) throws IOException {
		if (servletUrlTokens.length < 3) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		} else {
			String imageId = servletUrlTokens[1];
			String imageProperties = servletUrlTokens[2];
			if ("ImageProperties.xml".equals(imageProperties)) {
				if (servletUrlTokens.length == 3) {
					imagePropertiesOtherError(resp, imageId);
					return;
				} else if (servletUrlTokens.length == 4) {
					String supposedlyEmptyString = servletUrlTokens[3];
					if (supposedlyEmptyString == null || supposedlyEmptyString.isEmpty()) {
						imagePropertiesOtherError(resp, imageId);
						return;
					}
				}
			}
		}
		resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		return;
	}

	private void imagePropertiesInvalidContentError(HttpServletResponse resp, String[] servletUrlTokens)
			throws IOException {
		if (servletUrlTokens.length < 3) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		} else {
			String imageId = servletUrlTokens[1];
			String imageProperties = servletUrlTokens[2];
			if ("ImageProperties.xml".equals(imageProperties)) {
				if (servletUrlTokens.length == 3) {
					imagePropertiesInvalidContentError(resp, imageId);
					return;
				} else if (servletUrlTokens.length == 4) {
					String supposedlyEmptyString = servletUrlTokens[3];
					if (supposedlyEmptyString == null || supposedlyEmptyString.isEmpty()) {
						imagePropertiesInvalidContentError(resp, imageId);
						return;
					}
				}
			}
		}
		resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		return;
	}

	private void imagePropertiesOtherError(HttpServletResponse resp, String imageId) throws IOException {
		PrintWriter writer = resp.getWriter();
		if ("server_not_responding_5_s".equals(imageId)) {
			waitSeconds(5);
			resp.setContentType("application/xml");
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			resp.setStatus(HttpServletResponse.SC_OK);
			return;
		} else if ("server_not_responding_10_s".equals(imageId)) {
			waitSeconds(10);
			resp.setContentType("application/xml");
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			resp.setStatus(HttpServletResponse.SC_OK);
			return;
		} else if ("server_not_responding_15_s".equals(imageId)) {
			waitSeconds(15);
			resp.setContentType("application/xml");
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			resp.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
	}

	private void waitSeconds(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e1) {
			//
		}
	}

	private void imagePropertiesInvalidContentError(HttpServletResponse resp, String imageId) throws IOException {
		PrintWriter writer = resp.getWriter();
		if ("empty_file".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			return;
		} else if ("empty_document".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			return;
		} else if ("not_well_formed1".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" >");
			return;
		} else if ("not_well_formed2".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=7800 HEIGHT=6109 NUMTILES=744 NUMIMAGES=1 VERSION=1.8 TILESIZE=256 />");
			return;
		} else if ("invalid_root_element".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<image_properties WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			return;
		} else if ("missing_width".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			return;
		} else if ("missing_height".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\"  NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			return;
		} else if ("missing_numtiles".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\"  NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			return;
		} else if ("missing_numimages".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\"  VERSION=\"1.8\" TILESIZE=\"256\" />");
			return;
		} else if ("missing_version".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\"  TILESIZE=\"256\" />");
			return;
		} else if ("missing_tilesize".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.8\"  />");
			return;
		} else if ("empty_width".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			return;
		} else if ("empty_height".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			return;
		} else if ("empty_numtiles".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			return;
		} else if ("empty_numimages".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			return;
		} else if ("empty_version".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"\" TILESIZE=\"256\" />");
			return;
		} else if ("empty_tilesize".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"\" />");
			return;
		} else if ("nan_width".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"seven\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			return;
		} else if ("nan_height".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"five\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			return;
		} else if ("nan_numtiles".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"twentyseven\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			return;
		} else if ("nan_numimages".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"one\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			return;
		} else if ("nan_version".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"last\" TILESIZE=\"256\" />");
			return;
		} else if ("nan_tilesize".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"sixteen\" />");
			return;
		} else if ("unsupported_version".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"1\" VERSION=\"1.9\" TILESIZE=\"256\" />");
			return;
		} else if ("unsupported_numimages".equals(imageId)) {
			resp.setContentType("application/xml");
			resp.setStatus(HttpServletResponse.SC_OK);
			writer.println(XML_DECLARATION);
			writer.println("<IMAGE_PROPERTIES WIDTH=\"7800\" HEIGHT=\"6109\" NUMTILES=\"744\" NUMIMAGES=\"2\" VERSION=\"1.8\" TILESIZE=\"256\" />");
			return;
		}

		else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
	}

	private String[] extractUrlTokens(HttpServletRequest req) {
		String[] tokens = req.getPathInfo().split("/");
		return Arrays.copyOfRange(tokens, 1, tokens.length);
	}

	private String buildUrl(HttpServletRequest req, String... urlTokens) {
		StringBuilder builder = new StringBuilder();
		builder.append(req.getScheme()).append("://");
		builder.append(req.getServerName());
		if (req.getServerPort() != 80) {
			builder.append(":").append(req.getServerPort());
		}
		builder.append(req.getServletPath());
		for (String token : urlTokens) {
			builder.append("/").append(token);
		}
		return builder.toString();
	}

	private String buildUrl(String baseUrl, String... urlTokens) {
		StringBuilder builder = new StringBuilder();
		builder.append(baseUrl);
		for (int i = 1; i < urlTokens.length; i++) {
			String token = urlTokens[i];
			if (i == 1 && baseUrl.charAt(baseUrl.length() - 1) != '/') {
				builder.append("/");
			}
			builder.append(token).append('/');
		}
		return builder.toString();
	}

}
