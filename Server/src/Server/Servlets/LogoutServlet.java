package Server.Servlets;

import Server.Utils.ServletUtils;
import Server.Utils.SessionUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = SessionUtils.getUsername(request);
        if (username == null) {
            ServletUtils.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to log out.");
            return;
        }

        synchronized (ServletUtils.engineAccessLock) {
            ServletUtils.getEngine(getServletContext()).logout(username);
        }
        SessionUtils.clearSession(request);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
