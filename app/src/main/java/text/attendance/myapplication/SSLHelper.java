package text.attendance.myapplication;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLHelper {

    // ✅ Secure RequestQueue (Ignores SSL, Use in Dev Only)
    public static RequestQueue getSecureRequestQueue(Context context) {
        return Volley.newRequestQueue(context, new HurlStack(null, getUnsafeSSLSocketFactory()));
    }

    // ✅ Custom SSLSocketFactory (Trusts All Certificates)
    private static SSLSocketFactory getUnsafeSSLSocketFactory() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};  // Trust all certificates
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to create an SSL socket factory", e);
        }
    }

    // ✅ Allow All Hostnames (Use with Caution)
    public static void allowAllHostnames() {
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
}

