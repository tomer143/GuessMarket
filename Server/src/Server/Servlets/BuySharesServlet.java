package Server.Servlets;

import Models.External.GuessMarketException;
import Models.External.PurchaseResult;
import Server.Utils.ServletUtils;
import Server.Utils.SessionUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/event/buy")
public class BuySharesServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = SessionUtils.getUsername(request);
        if (username == null) {
            ServletUtils.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to buy shares.");
            return;
        }

        try {
            int eventId = ServletUtils.requireIntParam(request, "eventId");
            int optionIndex = ServletUtils.requireIntParam(request, "optionIndex");
            int amount = ServletUtils.requireIntParam(request, "amount");

            PurchaseResult result;
            synchronized (ServletUtils.engineAccessLock) {
                result = ServletUtils.getEngine(getServletContext()).buyShares(eventId, optionIndex, amount, username);
            }
            ServletUtils.writeJson(response, result);
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, exception);
        }
    }
}
