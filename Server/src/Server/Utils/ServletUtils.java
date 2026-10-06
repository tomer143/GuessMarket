package Server.Utils;

import Models.External.GuessMarketException;
import Engine.GuessMarketEngine;
import com.google.gson.Gson;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ServletUtils {

    private static final String ENGINE_ATTRIBUTE_NAME = "guessMarketEngine";
    private static final Object engineLock = new Object();
    public static final Object engineAccessLock = new Object();
    private static final Gson GSON = new Gson();

    public static GuessMarketEngine getEngine(ServletContext servletContext) {
        synchronized (engineLock) {
            if (servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME) == null)
                servletContext.setAttribute(ENGINE_ATTRIBUTE_NAME, new GuessMarketEngine());
        }
        return (GuessMarketEngine) servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME);
    }

    public static void writeJson(HttpServletResponse response, Object body) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(GSON.toJson(body));
    }

    public static void writeError(HttpServletResponse response, GuessMarketException exception) throws IOException {
        writeError(response, classifyStatus(exception.getMessage()), exception.getMessage());
    }

    public static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().print(message);
    }

    private static int classifyStatus(String message) {
        if (message != null && (message.startsWith("No event with id") || message.startsWith("No user named")))
            return HttpServletResponse.SC_NOT_FOUND;
        return HttpServletResponse.SC_BAD_REQUEST;
    }

    public static String requireStringParam(HttpServletRequest request, String name) throws GuessMarketException {
        String value = request.getParameter(name);
        if (value == null || value.isBlank())
            throw new GuessMarketException("Missing required parameter \"" + name + "\".");
        return value.trim();
    }

    public static int requireIntParam(HttpServletRequest request, String name) throws GuessMarketException {
        String value = requireStringParam(request, name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new GuessMarketException("Parameter \"" + name + "\" must be a valid integer.");
        }
    }

    public static double requireDoubleParam(HttpServletRequest request, String name) throws GuessMarketException {
        String value = requireStringParam(request, name);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new GuessMarketException("Parameter \"" + name + "\" must be a valid number.");
        }
    }

    public static boolean requireBooleanParam(HttpServletRequest request, String name) throws GuessMarketException {
        String value = requireStringParam(request, name);
        if (value.equalsIgnoreCase("true"))
            return true;
        if (value.equalsIgnoreCase("false"))
            return false;
        throw new GuessMarketException("Parameter \"" + name + "\" must be \"true\" or \"false\".");
    }

    public static <T extends Enum<T>> T requireEnumParam(HttpServletRequest request, String name, Class<T> type) throws GuessMarketException {
        String value = requireStringParam(request, name);
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException exception) {
            throw new GuessMarketException("Parameter \"" + name + "\" has an invalid value \"" + value + "\".");
        }
    }
}
