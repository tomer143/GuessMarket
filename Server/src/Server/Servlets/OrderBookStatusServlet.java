package Server.Servlets;

import Models.External.GuessMarketException;
import Models.External.OrderBookStatus;
import Server.Utils.ServletUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/event/orderbook-status")
public class OrderBookStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int eventId = ServletUtils.requireIntParam(request, "eventId");
            OrderBookStatus status;
            synchronized (ServletUtils.engineAccessLock) {
                status = ServletUtils.getEngine(getServletContext()).getOrderBookStatus(eventId);
            }
            ServletUtils.writeJson(response, status);
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, exception);
        }
    }
}
