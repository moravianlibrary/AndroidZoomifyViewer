package cz.mzk.androidzoomifyviewer.examples.kramerius;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;

/**
 * @author Martin Řehánek
 * 
 */
public class DownloadPageListTask extends ConcurrentAsyncTask<Void, Void, List<String>> {

	private static final String TAG = DownloadPageListTask.class.getSimpleName();
	private final String mProtocol;
	private final String mDomain;
	private final String mTopLevelPid;
	private final DownloadPidListResultHandler mUtilizer;
	private Exception mException = null;

	public DownloadPageListTask(String mProtocol, String mDomain, String mTopLevelPid,
			DownloadPidListResultHandler mUtilizer) {
		this.mProtocol = mProtocol;
		this.mDomain = mDomain;
		this.mTopLevelPid = mTopLevelPid;
		this.mUtilizer = mUtilizer;
	}

	@Override
	protected List<String> doInBackground(Void... params) {
		try {
			// GET http://localhost:8080/search/api/v5.0/item/<pid>/children
			String jsonString = downloadJson();
			// Log.d(TAG, jsonString);
			List<String> pagePids = findPagePids(jsonString);
			return pagePids;
		} catch (Exception e) {
			mException = e;
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

	private String downloadJson() throws IOException {
		String resourceUrl = mProtocol + "://" + mDomain + "/search/api/v5.0/item/" + mTopLevelPid + "/children";
		Log.d(TAG, "downloading json from " + resourceUrl);
		HttpURLConnection urlConnection = null;
		InputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			URL url = new URL(resourceUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(5000);// 5s
			// TODO: resit dalsi kody (redirect apod.)
			if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("error downloding " + resourceUrl + " (code=" + urlConnection.getResponseCode()
						+ ")");
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
			if (in != null) {
				in.close();
			}
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	@Override
	protected void onPostExecute(List<String> result) {
		if (mException != null) {
			// TODO: poresit chybu
			Log.e(TAG, "error downloading/parsing json", mException);
		} else {
			mUtilizer.onSuccess(result);
		}
	}

	// TODO: Handler with multiple errors
	public interface DownloadPidListResultHandler {
		public void onSuccess(List<String> pidList);
	}

}
