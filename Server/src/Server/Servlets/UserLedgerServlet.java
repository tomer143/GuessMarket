package Server.Servlets;

import Models.External.BalanceLedgerEntry;
import Models.External.GuessMarketException;
import Server.Utils.ServletUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/user/ledger")
public class UserLedgerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String username = ServletUtils.requireStringParam(request, "username");
            List<BalanceLedgerEntry> ledger;
            synchronized (ServletUtils.engineAccessLock) {
                ledger = ServletUtils.getEngine(getServletContext()).getUserBalanceLedger(username);
            }
            ServletUtils.writeJson(response, ledger);
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, exception);
        }
    }
}
