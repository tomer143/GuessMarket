package Server.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtils {

    private static final String USERNAME_ATTRIBUTE = "username";

    public static String getUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object attribute = session != null ? session.getAttribute(USERNAME_ATTRIBUTE) : null;
        return attribute != null ? attribute.toString() : null;
    }

    public static void setUsername(HttpServletRequest request, String username) {
        request.getSession(true).setAttribute(USERNAME_ATTRIBUTE, username);
    }

    public static void clearSession(HttpServletRequest request) {
        request.getSession().invalidate();
    }
}
