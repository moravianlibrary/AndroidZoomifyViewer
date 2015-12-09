package cz.mzk.tiledimageview.demonstration.ssl;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import cz.mzk.tiledimageview.demonstration.R;

public class SSLSocketFactoryProvider {

    private static final String TAG = SSLSocketFactoryProvider.class.getSimpleName();

    private static final int[] CERT_RES_IDS = new int[]{R.raw.washington_edu, R.raw.terena_ssl_ca_2_whole_chain};

    private static SSLSocketFactoryProvider instance;
    private final SSLContext sslContext;

    private SSLSocketFactoryProvider(Context context) throws KeyManagementException, CertificateException, KeyStoreException,
            NoSuchAlgorithmException, IOException {
        KeyStore localKeyStore = initLocalKeyStore(context);
        TrustManagerWithSystemAndLocalKeystores myTrustManager = new TrustManagerWithSystemAndLocalKeystores(
                localKeyStore);
        TrustManager[] tms = new TrustManager[]{myTrustManager};

        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tms, null);
        logTrustManager(myTrustManager);
    }

    public static SSLSocketFactoryProvider instanceOf(Context context) throws KeyManagementException, CertificateException,
            KeyStoreException, NoSuchAlgorithmException, IOException {
        if (instance == null) {
            // throw new RuntimeException("SSL Provider temporarily disabled");
            instance = new SSLSocketFactoryProvider(context);
        }
        return instance;
    }

    private KeyStore initLocalKeyStore(Context context) throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException {
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        for (int i = 0; i < CERT_RES_IDS.length; i++) {
            int resId = CERT_RES_IDS[i];
            String resName = context.getResources().getResourceName(resId);
            Log.d(TAG, "loading certificate from " + resName);
            X509Certificate cert = loadCertificate(context, cf, resId);
            keyStore.setCertificateEntry(resName, cert);
        }
        return keyStore;
    }

    private void logTrustManagersData(TrustManagerFactory tmf) {
        TrustManager[] trustManagers = tmf.getTrustManagers();
        Log.d(TAG, "trust managers: " + trustManagers.length);
        for (int i = 0; i < trustManagers.length; i++) {
            Log.d(TAG, "trust manager " + i + ": ");
            X509TrustManager xtm = (X509TrustManager) trustManagers[i];
            logTrustManager(xtm);
        }
    }

    private void logTrustManager(X509TrustManager xtm) {
        X509Certificate[] issuers = xtm.getAcceptedIssuers();
        Log.d(TAG, "issuers: " + issuers.length);
        for (X509Certificate cert : issuers) {
            String certStr = "Subj:" + cert.getSubjectDN().getName() + "\nIssuer:" + cert.getIssuerDN().getName();
            Log.d(TAG, certStr);
        }
    }

    private X509Certificate loadCertificate(Context context, CertificateFactory x509Cf, int resId)
            throws CertificateException, IOException {
        InputStream caInput = context.getResources().openRawResource(resId);
        Certificate ca;
        try {
            ca = x509Cf.generateCertificate(caInput);
            X509Certificate result = (X509Certificate) ca;
            Log.d(TAG, "ca= " + result.getSubjectDN());
            return result;
        } finally {
            caInput.close();
        }
    }

    public SSLSocketFactory getSslSocketFactory() {
        Log.d(TAG, "returning SSL socket factory");
        return sslContext.getSocketFactory();
    }

}
