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

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String[] servletUrlTokens = extractUrlTokens(req);
		String imageId = servletUrlTokens[0];
		// 2xx
		if ("ok".equals(imageId) || "ok2".equals(imageId) || "ok3".equals(imageId)) {
			// TODO
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
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
		} else if ("moved_permanently".equals(imageId)) { // 301
			// servletUrlTokens[0] = "ok";
			// resp.setHeader("Location", buildUrl(req, servletUrlTokens));
			resp.setHeader("Location", buildUrl(OK1, servletUrlTokens));
			resp.sendError(HttpServletResponse.SC_MOVED_PERMANENTLY);
		} else if ("found".equals(imageId)) {// 302
			// servletUrlTokens[0] = "ok";
			// resp.setHeader("Location", buildUrl(req, servletUrlTokens));
			resp.setHeader("Location", buildUrl(OK1, servletUrlTokens));
			resp.sendError(HttpServletResponse.SC_FOUND);
		} else if ("see_other".equals(imageId)) {// 303
			// servletUrlTokens[0] = "ok";
			// resp.setHeader("Location", buildUrl(req, servletUrlTokens));
			resp.setHeader("Location", buildUrl(OK1, servletUrlTokens));
			resp.sendError(HttpServletResponse.SC_SEE_OTHER);
		} else if ("not_modified".equals(imageId)) { // 304
			// TODO
			// resp.sendError(HttpServletResponse.SC_NOT_MODIF7IED);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else if ("use_proxy".equals(imageId)) { // 305
			// servletUrlTokens[0] = "ok";
			// resp.setHeader("Location", buildUrl(req, servletUrlTokens));
			resp.setHeader("Location", buildUrl(OK1, servletUrlTokens));
			resp.sendError(HttpServletResponse.SC_USE_PROXY);
		} else if ("temporary_redirect".equals(imageId)) { // 307
			// servletUrlTokens[0] = "ok";
			// resp.setHeader("Location", buildUrl(req, servletUrlTokens));
			resp.setHeader("Location", buildUrl(OK1, servletUrlTokens));
			resp.sendError(HttpServletResponse.SC_TEMPORARY_REDIRECT);
		}

		// 4xx
		else if ("bad_request".equals(imageId)) { // 400
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} else if ("unauthorized".equals(imageId)) { // 401
			// TODO
			// resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else if ("forbidden".equals(imageId)) { // 403
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		} else if ("not_found".equals(imageId)) {// 404
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else if ("proxy_authentication_required".equals(imageId)) {// 407
			// TODO
			// resp.sendError(HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else if ("request_timeout".equals(imageId)) {// 408
			resp.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT);
		} else if ("gone".equals(imageId)) {// 410
			resp.sendError(HttpServletResponse.SC_GONE);
		}
		// 5xx
		else if ("internal_server_error".equals(imageId)) {// 500
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} else if ("not_implemented".equals(imageId)) {// 501
			resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
		} else if ("bad_gateway".equals(imageId)) {// 502
			resp.sendError(HttpServletResponse.SC_BAD_GATEWAY);
		} else if ("service_unavailable".equals(imageId)) {// 503
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		} else if ("gateway_timeout".equals(imageId)) {// 504
			resp.sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT);
		} else if ("http_version_not_supported".equals(imageId)) {// 505
			resp.sendError(HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED);
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}

		// TODO: smycku presmerovani
		// TODO: nevalidni ImageProperties.xml
		// TODO: dlazdice neni jpeg, nebo je nejak rozbity

		// resp.setContentType("text/plain");
		// resp.getWriter().println("path info: <b>" + pathInfo + "</b>");
		//
		// // resp.getWriter().println("imageId: '" + imageId + "'");
		// for (String pathToken : pathTokens) {
		// resp.getWriter().println("|" + pathToken + "|");
		// }
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
