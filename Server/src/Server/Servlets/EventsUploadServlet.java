package Server.Servlets;

import Models.External.GuessMarketException;
import Server.Utils.ServletUtils;
import Server.Utils.SessionUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;

@WebServlet("/events/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 25, maxFileSize = 1024 * 1024 * 20, maxRequestSize = 1024 * 1024 * 25)
public class EventsUploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = SessionUtils.getUsername(request);
        if (username == null) {
            ServletUtils.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to upload an events file.");
            return;
        }

        try {
            Part filePart = request.getPart("file");
            if (filePart == null)
                throw new GuessMarketException("Missing required file part \"file\".");

            String fileName = filePart.getSubmittedFileName();
            if (fileName == null || !fileName.toLowerCase().endsWith(".xml"))
                throw new GuessMarketException("The file must have an \".xml\" extension.");

            try (InputStream inputStream = filePart.getInputStream()) {
                synchronized (ServletUtils.engineAccessLock) {
                    ServletUtils.getEngine(getServletContext()).uploadEventsFile(inputStream, username);
                }
            }

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (GuessMarketException exception) {
            ServletUtils.writeError(response, exception);
        } catch (ServletException exception) {
            ServletUtils.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Could not read the uploaded file: " + exception.getMessage());
        }
    }
}
