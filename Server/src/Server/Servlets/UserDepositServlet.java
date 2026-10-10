package Server.Servlets;

import Models.External.GuessMarketException;
import Server.Utils.ServletUtils;
import Server.Utils.SessionUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/user/deposit")
public class UserDepositServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = SessionUtils.getUsername(request);
        if (username == null) {
            ServletUtils.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to load funds.");
            return;
        }

        try {
            double amount = ServletUtils.requireDoubleParam(request, "amount");

            synchronized (ServletUtils.engineAccessLock) {
                ServletUtils.getEngine(getServletContext()).depositFunds(username, amount);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, exception);
        }
    }
}
