package Server.Servlets;

import Models.External.EventDetails;
import Models.External.GuessMarketException;
import Models.External.TradingMethod;
import Engine.GuessMarketEngine;
import Server.Utils.ServletUtils;
import Server.Utils.SessionUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/event/close")
public class CloseEventServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = SessionUtils.getUsername(request);
        if (username == null) {
            ServletUtils.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to close an event.");
            return;
        }

        try {
            int eventId = ServletUtils.requireIntParam(request, "eventId");
            int winningOptionIndex = ServletUtils.requireIntParam(request, "winningOptionIndex");

            GuessMarketEngine engine = ServletUtils.getEngine(getServletContext());
            Object status;
            synchronized (ServletUtils.engineAccessLock) {
                TradingMethod method = engine.getAllEvents().stream()
                        .filter(event -> event.id() == eventId)
                        .findFirst()
                        .map(EventDetails::method)
                        .orElseThrow(() -> new GuessMarketException("No event with id " + eventId + " is currently loaded."));

                if (method == TradingMethod.LMSR)
                    status = engine.closeEvent(eventId, winningOptionIndex, username);
                else
                    status = engine.closeOrderBookEvent(eventId, winningOptionIndex, username);
            }
            ServletUtils.writeJson(response, status);
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, exception);
        }
    }
}
