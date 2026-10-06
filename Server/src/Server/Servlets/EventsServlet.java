package Server.Servlets;

import Models.External.EventDetails;
import Models.External.FeeCollection;
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
import java.util.List;
import java.util.Map;

@WebServlet("/events")
public class EventsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<EventDetails> events;
        synchronized (ServletUtils.engineAccessLock) {
            events = ServletUtils.getEngine(getServletContext()).getAllEvents();
        }
        ServletUtils.writeJson(response, events);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String creatorUsername = SessionUtils.getUsername(request);
        if (creatorUsername == null) {
            ServletUtils.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to create an event.");
            return;
        }

        try {
            TradingMethod method = ServletUtils.requireEnumParam(request, "method", TradingMethod.class);
            String name = ServletUtils.requireStringParam(request, "name");
            String description = ServletUtils.requireStringParam(request, "description");
            int feePercent = ServletUtils.requireIntParam(request, "feePercent");
            FeeCollection feeCollection = ServletUtils.requireEnumParam(request, "feeCollection", FeeCollection.class);
            String optionAName = ServletUtils.requireStringParam(request, "optionAName");
            String optionBName = ServletUtils.requireStringParam(request, "optionBName");

            GuessMarketEngine engine = ServletUtils.getEngine(getServletContext());
            int eventId;

            if (method == TradingMethod.LMSR) {
                int liquidityB = ServletUtils.requireIntParam(request, "liquidityB");
                synchronized (ServletUtils.engineAccessLock) {
                    eventId = engine.createLmsrEvent(name, description, feePercent, feeCollection, optionAName, optionBName, liquidityB, creatorUsername);
                }
            } else {
                int baseValue = ServletUtils.requireIntParam(request, "baseValue");
                int initialAmount = ServletUtils.requireIntParam(request, "initialAmount");
                boolean allowMint = ServletUtils.requireBooleanParam(request, "allowMint");
                synchronized (ServletUtils.engineAccessLock) {
                    eventId = engine.createOrderBookEvent(name, description, feePercent, feeCollection, optionAName, optionBName, baseValue, initialAmount, allowMint, creatorUsername);
                }
            }

            ServletUtils.writeJson(response, Map.of("eventId", eventId));
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, exception);
        }
    }
}
