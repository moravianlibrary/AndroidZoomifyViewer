package cz.mzk.androidzoomifyviewer.examples.kramerius;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;

/**
 * @author Martin Řehánek
 * 
 */
public class DownloadPageListTask extends ConcurrentAsyncTask<Void, Void, List<String>> {

	private static final String TAG = DownloadPageListTask.class.getSimpleName();

	private static final int CONNECTION_TIMEOUT = 5000;// 5s
	private static final int MAX_REDIRECTS = 5;
	private static final List<Integer> REDIRECTION_HTTP_CODES = new ArrayList<Integer>() {
		{
			add(300);
			add(301);
			add(302);
			add(303);
			add(307);
		}
	};

	private final Context mContext;
	private final String mProtocol;
	private final String mDomain;
	private final String mTopLevelPid;
	private final DownloadPidListResultHandler mHandler;

	private Exception mException = null;

	public DownloadPageListTask(Context context, String protocol, String domain, String topLevelPid,
			DownloadPidListResultHandler handler) {
		this.mContext = context;
		this.mProtocol = protocol;
		this.mDomain = domain;
		this.mTopLevelPid = topLevelPid;
		this.mHandler = handler;
	}

	@Override
	protected List<String> doInBackground(Void... params) {
		try {
			// GET http://localhost:8080/search/api/v5.0/item/<pid>/children
			Log.d(TAG, "fetching children");
			String jsonString = downloadJsonWithChildren();
			// Log.d(TAG, jsonString);
			List<String> pagePids = findPagePids(jsonString);
			return pagePids;
		} catch (Exception e) {
			mException = e;
			Log.w(TAG, e);
			e.printStackTrace();
			return Collections.<String> emptyList();
		}
	}

	private List<String> findPagePids(String jsonString) throws JSONException {
		List<String> result = new ArrayList<String>();
		JSONArray jsonArray = new JSONArray(jsonString);
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject object = (JSONObject) jsonArray.get(i);
			String pageType = object.getString("model");
			if ("page".equals(pageType)) {
				result.add(object.getString("pid"));
			}
		}
		Log.d(TAG, "pages: " + result.size());
		return result;
	}

	private String downloadJsonWithChildren() throws IOException, GeneralSecurityException {
		String resourceUrl = mProtocol + "://" + mDomain + "/search/api/v5.0/item/" + mTopLevelPid + "/children";
		return downloadJson(resourceUrl, MAX_REDIRECTS);
	}

	private String downloadJson(String resourceUrl, int remainingRedirects) throws IOException,
			GeneralSecurityException {
		Log.d(TAG, "downloading json from " + resourceUrl + ", remaining redirects: " + remainingRedirects);
		if (remainingRedirects == 0) {
			throw new IOException("max redirections reached (probably redirection loop)");
		}
		HttpURLConnection urlConnection = null;
		InputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			URL url = new URL(resourceUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
			// urlConnection.setFollowRedirects(true);
			// urlConnection.setInstanceFollowRedirects(true);
			// urlConnection.setUseCaches(false);
			// urlConnection.setDefaultUseCaches(false);
			if (urlConnection instanceof HttpsURLConnection) {
				// HttpsURLConnection httpsConn = (HttpsURLConnection) urlConnection;
				// httpsConn.setHostnameVerifier(mHostNameVerifier);
				// httpsConn.setSSLSocketFactory(SSLProvider.instanceOf(mContext).getSslSocketFactory());
			}
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				if (REDIRECTION_HTTP_CODES.contains(responseCode)) {
					String newLocation = urlConnection.getHeaderField("Location");
					close(urlConnection, in, out);
					return (downloadJson(newLocation, remainingRedirects - 1));
				} else {
					throw new IOException("error downloading " + resourceUrl + " (code="
							+ urlConnection.getResponseCode() + ")");
				}
			} else {
				in = new BufferedInputStream(urlConnection.getInputStream());
				byte[] buffer = new byte[8 * 1024];
				out = new ByteArrayOutputStream();
				int readBytes = 0;
				while ((readBytes = in.read(buffer)) != -1) {
					out.write(buffer, 0, readBytes);
				}
				out.flush();
				return out.toString();
			}
		} finally {
			close(urlConnection, in, out);
		}
	}

	private void close(HttpURLConnection urlConnection, InputStream in, ByteArrayOutputStream out) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				Log.d(TAG, "error closing input stream", e);
			}
		}
		if (urlConnection != null) {
			Log.d(TAG, "closing connection");
			urlConnection.disconnect();
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				Log.d(TAG, "error closing output stream", e);
			}
		}
	}

	@Override
	protected void onPostExecute(List<String> result) {
		if (mException != null) {
			mHandler.onError(mException.getMessage());
		} else {
			mHandler.onSuccess(result);
		}
	}

	public interface DownloadPidListResultHandler {
		public void onSuccess(List<String> pidList);

		public void onError(String errorMessage);
	}

}
