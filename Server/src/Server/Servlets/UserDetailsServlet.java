package Server.Servlets;

import Models.External.GuessMarketException;
import Models.External.UserDetails;
import Server.Utils.ServletUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/user")
public class UserDetailsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String username = ServletUtils.requireStringParam(request, "username");
            UserDetails details;
            synchronized (ServletUtils.engineAccessLock) {
                details = ServletUtils.getEngine(getServletContext()).getUserDetails(username);
            }
            ServletUtils.writeJson(response, details);
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, exception);
        }
    }
}
