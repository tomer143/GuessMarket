package Server.Servlets;

import Models.External.GuessMarketException;
import Server.Utils.ServletUtils;
import Server.Utils.SessionUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        try {
            ServletUtils.getEngine(getServletContext()).registerUser(username);
            SessionUtils.setUsername(request, username.trim());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
        }
    }
}
