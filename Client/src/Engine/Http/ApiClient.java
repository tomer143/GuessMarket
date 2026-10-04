package Engine.Http;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;

public class ApiClient {
    public static final String BASE_URL = "http://localhost:8080/GuessMarket";
    public static final SessionCookieJar COOKIE_JAR = new SessionCookieJar();
    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder().cookieJar(COOKIE_JAR).build();
    public static final Gson GSON = new Gson();
}
