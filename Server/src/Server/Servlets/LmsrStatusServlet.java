package Server.Servlets;

import Models.External.EventStatus;
import Models.External.GuessMarketException;
import Server.Utils.ServletUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/event/lmsr-status")
public class LmsrStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int eventId = ServletUtils.requireIntParam(request, "eventId");
            EventStatus status;
            synchronized (ServletUtils.engineAccessLock) {
                status = ServletUtils.getEngine(getServletContext()).getEventStatus(eventId);
            }
            ServletUtils.writeJson(response, status);
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, exception);
        }
    }
}
