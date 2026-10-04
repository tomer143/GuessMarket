package Engine;

import Engine.Http.ApiClient;
import Models.External.*;
import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ClientGuessMarketEngine {

    public void loadEventsFile(String path) throws GuessMarketException {
        File file = new File(path);
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/xml"));
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder().url(urlBuilder("events/upload").build()).post(body).build();
        executeAndReadBody(request);
    }

    public void registerUser(String username) throws GuessMarketException {
        HttpUrl url = urlBuilder("login").addQueryParameter("username", username).build();
        executeAndReadBody(new Request.Builder().url(url).post(RequestBody.create(new byte[0])).build());
    }

    public void logout() {
        try {
            executeAndReadBody(new Request.Builder().url(urlBuilder("logout").build()).post(RequestBody.create(new byte[0])).build());
        } catch (GuessMarketException ignored) {
        } finally {
            ApiClient.COOKIE_JAR.clear();
        }
    }

    public void depositFunds(String username, double amount) throws GuessMarketException {
        HttpUrl url = urlBuilder("user/deposit")
                .addQueryParameter("username", username)
                .addQueryParameter("amount", String.valueOf(amount))
                .build();
        executeAndReadBody(new Request.Builder().url(url).post(RequestBody.create(new byte[0])).build());
    }

    public List<BalanceLedgerEntry> getUserBalanceLedger(String username) throws GuessMarketException {
        HttpUrl url = urlBuilder("user/ledger").addQueryParameter("username", username).build();
        Type type = new TypeToken<List<BalanceLedgerEntry>>() {}.getType();
        return ApiClient.GSON.fromJson(executeAndReadBody(new Request.Builder().url(url).get().build()), type);
    }

    public List<EventDetails> getAllEvents() {
        try {
            HttpUrl url = urlBuilder("events").build();
            Type type = new TypeToken<List<EventDetails>>() {}.getType();
            return ApiClient.GSON.fromJson(executeAndReadBody(new Request.Builder().url(url).get().build()), type);
        } catch (GuessMarketException exception) {
            return List.of();
        }
    }

    public void openEvent(int eventId, String username) throws GuessMarketException {
        HttpUrl url = urlBuilder("event/open").addQueryParameter("eventId", String.valueOf(eventId)).build();
        executeAndReadBody(new Request.Builder().url(url).post(RequestBody.create(new byte[0])).build());
    }

    public int createLmsrEvent(String name, String description, int feePercent, FeeCollection feeCollection,
                                String optionAName, String optionBName, int liquidityB, String creatorUsername) throws GuessMarketException {
        HttpUrl url = eventCreationUrlBuilder(TradingMethod.LMSR, name, description, feePercent, feeCollection, optionAName, optionBName)
                .addQueryParameter("liquidityB", String.valueOf(liquidityB))
                .build();
        return readEventId(new Request.Builder().url(url).post(RequestBody.create(new byte[0])).build());
    }

    public int createOrderBookEvent(String name, String description, int feePercent, FeeCollection feeCollection,
                                     String optionAName, String optionBName, int baseValue, int initialAmount, boolean allowMint,
                                     String creatorUsername) throws GuessMarketException {
        HttpUrl url = eventCreationUrlBuilder(TradingMethod.ORDER_BOOK, name, description, feePercent, feeCollection, optionAName, optionBName)
                .addQueryParameter("baseValue", String.valueOf(baseValue))
                .addQueryParameter("initialAmount", String.valueOf(initialAmount))
                .addQueryParameter("allowMint", String.valueOf(allowMint))
                .build();
        return readEventId(new Request.Builder().url(url).post(RequestBody.create(new byte[0])).build());
    }

    private HttpUrl.Builder eventCreationUrlBuilder(TradingMethod method, String name, String description, int feePercent,
                                                      FeeCollection feeCollection, String optionAName, String optionBName) {
        return urlBuilder("events")
                .addQueryParameter("method", method.name())
                .addQueryParameter("name", name)
                .addQueryParameter("description", description)
                .addQueryParameter("feePercent", String.valueOf(feePercent))
                .addQueryParameter("feeCollection", feeCollection.name())
                .addQueryParameter("optionAName", optionAName)
                .addQueryParameter("optionBName", optionBName);
    }

    private int readEventId(Request request) throws GuessMarketException {
        String body = executeAndReadBody(request);
        Type type = new TypeToken<Map<String, Double>>() {}.getType();
        Map<String, Double> parsed = ApiClient.GSON.fromJson(body, type);
        return parsed.get("eventId").intValue();
    }

    public EventStatus getEventStatus(int eventId) throws GuessMarketException {
        HttpUrl url = urlBuilder("event/lmsr-status").addQueryParameter("eventId", String.valueOf(eventId)).build();
        return ApiClient.GSON.fromJson(executeAndReadBody(new Request.Builder().url(url).get().build()), EventStatus.class);
    }

    public PurchaseResult buyShares(int eventId, int optionIndex, int amount, String username) throws GuessMarketException {
        HttpUrl url = urlBuilder("event/buy")
                .addQueryParameter("eventId", String.valueOf(eventId))
                .addQueryParameter("optionIndex", String.valueOf(optionIndex))
                .addQueryParameter("amount", String.valueOf(amount))
                .build();
        return ApiClient.GSON.fromJson(executeAndReadBody(new Request.Builder().url(url).post(RequestBody.create(new byte[0])).build()), PurchaseResult.class);
    }

    public EventStatus closeEvent(int eventId, int winningOptionIndex, String username) throws GuessMarketException {
        HttpUrl url = closeEventUrl(eventId, winningOptionIndex);
        return ApiClient.GSON.fromJson(executeAndReadBody(new Request.Builder().url(url).post(RequestBody.create(new byte[0])).build()), EventStatus.class);
    }

    public OrderBookStatus closeOrderBookEvent(int eventId, int winningOptionIndex, String username) throws GuessMarketException {
        HttpUrl url = closeEventUrl(eventId, winningOptionIndex);
        return ApiClient.GSON.fromJson(executeAndReadBody(new Request.Builder().url(url).post(RequestBody.create(new byte[0])).build()), OrderBookStatus.class);
    }

    private HttpUrl closeEventUrl(int eventId, int winningOptionIndex) {
        return urlBuilder("event/close")
                .addQueryParameter("eventId", String.valueOf(eventId))
                .addQueryParameter("winningOptionIndex", String.valueOf(winningOptionIndex))
                .build();
    }

    public OrderResult submitOrder(int eventId, int optionIndex, OrderAction side, int quantity, double price, String username) throws GuessMarketException {
        HttpUrl url = urlBuilder("event/orders")
                .addQueryParameter("eventId", String.valueOf(eventId))
                .addQueryParameter("optionIndex", String.valueOf(optionIndex))
                .addQueryParameter("side", side.name())
                .addQueryParameter("quantity", String.valueOf(quantity))
                .addQueryParameter("price", String.valueOf(price))
                .build();
        return ApiClient.GSON.fromJson(executeAndReadBody(new Request.Builder().url(url).post(RequestBody.create(new byte[0])).build()), OrderResult.class);
    }

    public OrderBookStatus getOrderBookStatus(int eventId) throws GuessMarketException {
        HttpUrl url = urlBuilder("event/orderbook-status").addQueryParameter("eventId", String.valueOf(eventId)).build();
        return ApiClient.GSON.fromJson(executeAndReadBody(new Request.Builder().url(url).get().build()), OrderBookStatus.class);
    }

    public LmsrParticipation getUserLmsrParticipation(int eventId, String username) throws GuessMarketException {
        HttpUrl url = urlBuilder("event/participation/lmsr")
                .addQueryParameter("eventId", String.valueOf(eventId))
                .addQueryParameter("username", username)
                .build();
        return ApiClient.GSON.fromJson(executeAndReadBody(new Request.Builder().url(url).get().build()), LmsrParticipation.class);
    }

    public OrderBookParticipation getUserOrderBookParticipation(int eventId, String username) throws GuessMarketException {
        HttpUrl url = urlBuilder("event/participation/orderbook")
                .addQueryParameter("eventId", String.valueOf(eventId))
                .addQueryParameter("username", username)
                .build();
        return ApiClient.GSON.fromJson(executeAndReadBody(new Request.Builder().url(url).get().build()), OrderBookParticipation.class);
    }

    public List<UserSummary> getAllUsers() {
        try {
            HttpUrl url = urlBuilder("users").build();
            Type type = new TypeToken<List<UserSummary>>() {}.getType();
            return ApiClient.GSON.fromJson(executeAndReadBody(new Request.Builder().url(url).get().build()), type);
        } catch (GuessMarketException exception) {
            return List.of();
        }
    }

    public UserDetails getUserDetails(String username) throws GuessMarketException {
        HttpUrl url = urlBuilder("user").addQueryParameter("username", username).build();
        return ApiClient.GSON.fromJson(executeAndReadBody(new Request.Builder().url(url).get().build()), UserDetails.class);
    }

    public List<BalanceHistoryPoint> getUserBalanceHistory(String username) throws GuessMarketException {
        HttpUrl url = urlBuilder("user/balance-history").addQueryParameter("username", username).build();
        Type type = new TypeToken<List<BalanceHistoryPoint>>() {}.getType();
        return ApiClient.GSON.fromJson(executeAndReadBody(new Request.Builder().url(url).get().build()), type);
    }

    private HttpUrl.Builder urlBuilder(String path) {
        return HttpUrl.parse(ApiClient.BASE_URL + "/" + path).newBuilder();
    }

    private String executeAndReadBody(Request request) throws GuessMarketException {
        try (Response response = ApiClient.HTTP_CLIENT.newCall(request).execute()) {
            String body = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful())
                throw new GuessMarketException(body);
            return body;
        } catch (IOException exception) {
            throw new GuessMarketException("Could not reach the server: " + exception.getMessage());
        }
    }
}
