package cz.mzk.androidzoomifyviewer.examples.ssl;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import android.util.Log;

public class TrustManagerWithSystemAndLocalKeystores implements X509TrustManager {

	private static final String TAG = TrustManagerWithSystemAndLocalKeystores.class.getSimpleName();

	private final X509TrustManager defaultTrustManager;
	private final X509TrustManager localTrustManager;
	private X509Certificate[] acceptedIssuers;

	public TrustManagerWithSystemAndLocalKeystores(KeyStore localKeyStore) throws NoSuchAlgorithmException,
			KeyStoreException {
		Log.d(TAG, "initializing");
		defaultTrustManager = initTrustManager(null);
		localTrustManager = localKeyStore == null ? null : initTrustManager(localKeyStore);
	}

	private X509TrustManager initTrustManager(KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException {
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(keyStore);
		X509TrustManager xtm = (X509TrustManager) tmf.getTrustManagers()[0];
		return xtm;
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		Log.d(TAG, "checking server trusted: " + toString(chain) + ", auth type: " + authType);
		try {
			try {
				defaultTrustManager.checkServerTrusted(chain, authType);
				Log.d(TAG, "checkServerTrusted: ok (default)");
			} catch (CertificateException ce) {
				if (localTrustManager != null) {
					localTrustManager.checkServerTrusted(chain, authType);
					Log.d(TAG, "checkServerTrusted: ok (local)");
				} else {
					throw ce;
				}
			}
		} catch (CertificateException ce) {
			Log.d(TAG, "checkServerTrusted: failed");
			throw ce;
		}
	}

	private String toString(X509Certificate[] chain) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < chain.length; i++) {
			X509Certificate x509Certificate = chain[i];
			if (i != 0) {
				builder.append(" -> ");
			}
			builder.append('\"');
			builder.append(extractCommonName(x509Certificate.getSubjectX500Principal().getName()));
			builder.append('\"');
		}
		return builder.toString();
	}

	private String extractCommonName(String DN) {
		String[] tokenks = DN.split(",");
		for (String token : tokenks) {
			if (token.startsWith("CN=")) {
				return token.substring(3);
			}
		}
		return "";
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		try {
			try {
				defaultTrustManager.checkClientTrusted(chain, authType);
				Log.d(TAG, "checkClientTrusted: ok (default)");
			} catch (CertificateException ce) {
				if (localTrustManager != null) {
					localTrustManager.checkClientTrusted(chain, authType);
					Log.d(TAG, "checkClientTrusted: ok (local)");
				} else {
					throw ce;
				}
			}
		} catch (CertificateException ce) {
			Log.d(TAG, "checkClientTrusted: failed");
			throw ce;
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		if (acceptedIssuers == null) {
			acceptedIssuers = initAcceptedIssuers();
		}
		return acceptedIssuers;
	}

	private X509Certificate[] initAcceptedIssuers() {
		if (localTrustManager != null) {
			X509Certificate[] defaultCerts = defaultTrustManager.getAcceptedIssuers();
			X509Certificate[] localCerts = localTrustManager.getAcceptedIssuers();
			X509Certificate[] allCerts = new X509Certificate[defaultCerts.length + localCerts.length];
			Log.d(TAG, "certificates: default: " + defaultCerts.length + ", local: " + localCerts.length);
			for (int i = 0; i < defaultCerts.length; i++) {
				allCerts[i] = defaultCerts[i];
			}
			for (int i = 0; i < allCerts.length - defaultCerts.length; i++) {
				allCerts[defaultCerts.length + i] = localCerts[i];
			}
			return allCerts;
		} else {
			X509Certificate[] defaultCerts = defaultTrustManager.getAcceptedIssuers();
			Log.d(TAG, "certificates: default: " + defaultCerts.length + ", local: 0");
			return defaultCerts;
		}
	}
}
