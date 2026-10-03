package Server.Servlets;

import Models.External.BalanceHistoryPoint;
import Models.External.GuessMarketException;
import Server.Utils.ServletUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/user/balance-history")
public class UserBalanceHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String username = ServletUtils.requireStringParam(request, "username");
            List<BalanceHistoryPoint> history = ServletUtils.getEngine(getServletContext()).getUserBalanceHistory(username);
            ServletUtils.writeJson(response, history);
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, exception);
        }
    }
}
