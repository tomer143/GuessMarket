package Server.Servlets;

import Models.External.GuessMarketException;
import Models.External.OrderAction;
import Models.External.OrderResult;
import Server.Utils.ServletUtils;
import Server.Utils.SessionUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/event/orders")
public class SubmitOrderServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = SessionUtils.getUsername(request);
        if (username == null) {
            ServletUtils.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to submit an order.");
            return;
        }

        try {
            int eventId = ServletUtils.requireIntParam(request, "eventId");
            int optionIndex = ServletUtils.requireIntParam(request, "optionIndex");
            OrderAction side = ServletUtils.requireEnumParam(request, "side", OrderAction.class);
            int quantity = ServletUtils.requireIntParam(request, "quantity");
            double price = ServletUtils.requireDoubleParam(request, "price");

            OrderResult result;
            synchronized (ServletUtils.engineAccessLock) {
                result = ServletUtils.getEngine(getServletContext()).submitOrder(eventId, optionIndex, side, quantity, price, username);
            }
            ServletUtils.writeJson(response, result);
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, exception);
        }
    }
}
