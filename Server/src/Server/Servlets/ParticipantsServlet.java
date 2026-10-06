package Server.Servlets;

import Models.External.GuessMarketException;
import Models.External.ParticipantSummary;
import Server.Utils.ServletUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/event/participants")
public class ParticipantsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int eventId = ServletUtils.requireIntParam(request, "eventId");
            List<ParticipantSummary> participants;
            synchronized (ServletUtils.engineAccessLock) {
                participants = ServletUtils.getEngine(getServletContext()).getOrderBookParticipants(eventId);
            }
            ServletUtils.writeJson(response, participants);
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, exception);
        }
    }
}
