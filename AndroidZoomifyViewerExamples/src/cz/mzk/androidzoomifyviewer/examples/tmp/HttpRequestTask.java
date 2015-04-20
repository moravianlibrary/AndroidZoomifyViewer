package cz.mzk.androidzoomifyviewer.examples.tmp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class HttpRequestTask extends AsyncTask<String, Void, Throwable> {
	public static interface ResultHandler {
		public void onSuccess();

		public void onError(String message);

	}

	private static final String TAG = HttpRequestTask.class.getSimpleName();
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

	private final Context context;
	private final ResultHandler handler;

	public HttpRequestTask(Context context, ResultHandler handler) {
		this.context = context;
		this.handler = handler;
	}

	@Override
	protected Throwable doInBackground(String... params) {
		String url = params[0];
		try {
			connect(new URL(url), MAX_REDIRECTS);
			return null;
		} catch (Throwable e) {
			return e;
		}
	}

	private void connect(URL url, int remainingRedirects) throws IOException, GeneralSecurityException {
		Log.d(TAG, "fetching: " + url + ", remaining redirects: " + remainingRedirects);
		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(5000);// 5s
			// urlConnection.setUseCaches(false);
			// urlConnection.setDefaultUseCaches(false);
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				// manual redirection
				if (REDIRECTION_HTTP_CODES.contains(responseCode)) {
					String newLocation = urlConnection.getHeaderField("Location");
					connect(new URL(newLocation), remainingRedirects - 1);
					return;
				} else {
					throw new RuntimeException("error downloading " + url + " (code=" + urlConnection.getResponseCode()
							+ ")");
				}
			}

			// if (urlConnection instanceof HttpsURLConnection) {
			// HttpsURLConnection httpsConn = (HttpsURLConnection) urlConnection;
			// httpsConn.setFollowRedirects(false);
			// httpsConn.setHostnameVerifier(mHostNameVerifier);
			// httpsConn.setSSLSocketFactory(SSLProvider.instanceOf(context).getSslSocketFactory());
			// logTrustManagersData();
			// }
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
	}

	private void logTrustManagersData() {
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init((KeyStore) null);
			TrustManager[] trustManagers = tmf.getTrustManagers();
			Log.d(TAG, "trust managers: " + trustManagers.length);
			for (int i = 0; i < trustManagers.length; i++) {
				Log.d(TAG, "trust manager " + i + ": ");
				X509TrustManager xtm = (X509TrustManager) trustManagers[i];
				for (X509Certificate cert : xtm.getAcceptedIssuers()) {
					String certStr = "Subj:" + cert.getSubjectDN().getName() + "\nIssuer:"
							+ cert.getIssuerDN().getName();
					Log.d(TAG, certStr);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onPostExecute(Throwable result) {
		if (result == null) {
			Log.d(TAG, "success");
			handler.onSuccess();
		} else {
			Log.e(TAG, "error: ", result);
			// result.printStackTrace();
			handler.onError(result.getMessage());
		}
	}

}
