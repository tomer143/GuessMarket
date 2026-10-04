package Engine.Http;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionCookieJar implements CookieJar {
    private final Map<String, Map<String, Cookie>> cookieStore = new HashMap<>();

    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        Map<String, Cookie> hostCookies = cookieStore.computeIfAbsent(url.host(), host -> new HashMap<>());
        for (Cookie cookie : cookies)
            hostCookies.put(cookie.name(), cookie);
    }

    @Override
    public synchronized List<Cookie> loadForRequest(HttpUrl url) {
        Map<String, Cookie> hostCookies = cookieStore.get(url.host());
        return hostCookies == null ? List.of() : new ArrayList<>(hostCookies.values());
    }

    public synchronized void clear() {
        cookieStore.clear();
    }
}
