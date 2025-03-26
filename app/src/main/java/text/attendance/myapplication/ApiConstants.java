package text.attendance.myapplication;

public class ApiConstants {
    public static final String BASE_URL = "https://192.168.0.108:3443";

    // 🔹 Authentication APIs
    public static final String LOGIN_URL = BASE_URL + "/login";
    public static final String REGISTER_URL = BASE_URL + "/register";
    public static final String VERIFY_OTP_URL = BASE_URL + "/verify-otp";
    public static final String RESEND_OTP_URL = BASE_URL + "/resend-otp";
    public static final String CHECK_EMAIL_URL = BASE_URL + "/check-email";
    public static final String RESET_PASSWORD_URL = BASE_URL + "/reset-password";
    public static final String GET_USERNAME_URL = BASE_URL + "/getUsername";

    // 🔹 Company Upload API
    public static final String UPLOAD_COMPANY_DETAILS = BASE_URL + "/upload-company-details";
    public static final String UPLOAD_BASEMENT_DETAILS = BASE_URL + "/upload-basement-details";

}
