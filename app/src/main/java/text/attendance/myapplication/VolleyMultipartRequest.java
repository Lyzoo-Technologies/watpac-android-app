package text.attendance.myapplication;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;
    private final Map<String, String> mHeaders;
    private final Map<String, String> mParams;
    private final Map<String, DataPart> mByteData;

    private static final String LINE_END = "\r\n";
    private static final String TWO_HYPHENS = "--";
    private final String boundary;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mHeaders = new HashMap<>();
        this.mParams = new HashMap<>();
        this.mByteData = new HashMap<>();

        this.boundary = "VolleyBoundary" + new Random().nextInt(999999);
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // Append Text Parameters
            if (!mParams.isEmpty()) {
                for (Map.Entry<String, String> entry : mParams.entrySet()) {
                    bos.write((TWO_HYPHENS + boundary + LINE_END).getBytes(StandardCharsets.UTF_8));
                    bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_END).getBytes(StandardCharsets.UTF_8));
                    bos.write(("Content-Type: text/plain; charset=UTF-8" + LINE_END).getBytes(StandardCharsets.UTF_8));
                    bos.write(LINE_END.getBytes(StandardCharsets.UTF_8));
                    bos.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                    bos.write(LINE_END.getBytes(StandardCharsets.UTF_8));
                }
            }

            // Append File Data
            if (!mByteData.isEmpty()) {
                for (Map.Entry<String, DataPart> entry : mByteData.entrySet()) {
                    DataPart dataPart = entry.getValue();
                    bos.write((TWO_HYPHENS + boundary + LINE_END).getBytes(StandardCharsets.UTF_8));
                    bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + dataPart.getFileName() + "\"" + LINE_END).getBytes(StandardCharsets.UTF_8));

                    // Get a valid MIME type
                    String mimeType = dataPart.getType();
                    bos.write(("Content-Type: " + mimeType + LINE_END).getBytes(StandardCharsets.UTF_8));

                    bos.write(("Content-Transfer-Encoding: binary" + LINE_END).getBytes(StandardCharsets.UTF_8));
                    bos.write(LINE_END.getBytes(StandardCharsets.UTF_8));
                    bos.write(dataPart.getContent());
                    bos.write(LINE_END.getBytes(StandardCharsets.UTF_8));
                }
            }

            // End of multipart request
            bos.write((TWO_HYPHENS + boundary + TWO_HYPHENS + LINE_END).getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream: %s", e.getMessage());
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>(mHeaders);
        headers.put("Content-Type", getBodyContentType());
        return headers;
    }

    // 🆕 Method to add ad form parameters
    public void addAdFormParams(String companyName, String phoneNumber, byte[] imageBytes, String imageName) {
        mParams.put("company_name", companyName);
        mParams.put("phone_number", phoneNumber);
        mByteData.put("image", new DataPart(imageName, imageBytes, "image/jpeg"));
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = (type != null && !type.isEmpty()) ? type : getMimeType(fileName);
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return (type != null && !type.isEmpty()) ? type : "application/octet-stream";
        }

        private static String getMimeType(String fileName) {
            String mimeType = URLConnection.guessContentTypeFromName(fileName);
            return (mimeType != null) ? mimeType : "application/octet-stream";
        }
    }
}
