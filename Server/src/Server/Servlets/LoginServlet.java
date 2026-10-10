package Server.Servlets;

import Models.External.GuessMarketException;
import Server.Utils.ServletUtils;
import Server.Utils.SessionUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String previousUsername = SessionUtils.getUsername(request);
        try {
            String storedUsername;
            synchronized (ServletUtils.engineAccessLock) {
                storedUsername = ServletUtils.getEngine(getServletContext()).registerUser(username);
                if (previousUsername != null && !previousUsername.equals(storedUsername))
                    ServletUtils.getEngine(getServletContext()).logout(previousUsername);
            }
            SessionUtils.setUsername(request, storedUsername);
            ServletUtils.writeJson(response, Map.of("username", storedUsername));
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
        }
    }
}
