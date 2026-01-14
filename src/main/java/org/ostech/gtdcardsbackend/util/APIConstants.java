package org.ostech.gtdcardsbackend.util;

public class APIConstants {

    // API Base Path
    public static final String API_BASE = "/api/v1";

    // CARD API Endpoints
    public static final String CARD_ENDPOINT = API_BASE + "/cards";
    public static final String CARD_GET_BY_ID_ENDPOINT = CARD_ENDPOINT + "/{id}";
    public static final String CARD_UPDATE_ENDPOINT = CARD_ENDPOINT + "/{id}";
    public static final String CARD_EXCHANGE_RATES_ENDPOINT = CARD_ENDPOINT + "/{id}/exchange-rates";
    private APIConstants() {
    }
}
