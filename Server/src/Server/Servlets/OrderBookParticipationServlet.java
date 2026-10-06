package Server.Servlets;

import Models.External.GuessMarketException;
import Models.External.OrderBookParticipation;
import Server.Utils.ServletUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/event/participation/orderbook")
public class OrderBookParticipationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int eventId = ServletUtils.requireIntParam(request, "eventId");
            String username = ServletUtils.requireStringParam(request, "username");

            OrderBookParticipation participation;
            synchronized (ServletUtils.engineAccessLock) {
                participation = ServletUtils.getEngine(getServletContext()).getUserOrderBookParticipation(eventId, username);
            }
            ServletUtils.writeJson(response, participation);
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, exception);
        }
    }
}
