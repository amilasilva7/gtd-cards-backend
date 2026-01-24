package org.ostech.gtdcardsbackend.util;

public class APIConstants {

    // API Base Path
    public static final String API_BASE = "/api/v1";

    // AUTH API Endpoints
    public static final String AUTH_ENDPOINT = API_BASE + "/auth";
    public static final String AUTH_LOGIN_ENDPOINT = "/login";
    public static final String AUTH_LOGOUT_ENDPOINT = "/logout";
    public static final String AUTH_REGISTER_ENDPOINT = "/register";
    public static final String AUTH_VALIDATE_ENDPOINT = "/validate";

    // CARD API Endpoints
    public static final String CARD_ENDPOINT = API_BASE + "/cards";
    public static final String CARD_GET_BY_ID_ENDPOINT = "/{id}";
    public static final String CARD_UPDATE_ENDPOINT = "/{id}";

    // EXCHANGE RATES API Endpoints
    public static final String EXCHANGE_RATES_ENDPOINT = API_BASE + "/exchange-rates";

    private APIConstants() {
    }
}
