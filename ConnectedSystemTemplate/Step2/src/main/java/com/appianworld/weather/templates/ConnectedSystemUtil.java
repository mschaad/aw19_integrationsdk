package com.appianworld.weather.templates;

import com.appian.connectedsystems.templateframework.sdk.IntegrationError;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import tk.plogitech.darksky.forecast.ForecastException;

import java.util.HashMap;
import java.util.Map;

public class ConnectedSystemUtil {
    private Long startTime;
    private Long endTime;
    private HashMap<String, Object> requestDiagnostics;
    private HashMap<String, Object> responseDiagnostics;
    private HashMap<String, Object> response;

    ConnectedSystemUtil(String method) {
        requestDiagnostics = new HashMap<>();
        responseDiagnostics = new HashMap<>();
        responseDiagnostics.put("DarkSky Java API", "com.google.cloud:darksky-forecast-api:1.31.0");
        responseDiagnostics.put("Method Used", method);
        response = new HashMap<>();
    }


    public void startTiming() {
        startTime = System.currentTimeMillis();
    }

    public void stopTiming() {
        endTime = System.currentTimeMillis();
    }

    public long getTiming() {
        if (startTime == null || endTime == null) {
            System.out.println("Start or End was never called, timing is invalid");
            return -1;
        }
        return endTime - startTime;
    }

    public void addRequestDiagnostic(String key, Object value) {
        requestDiagnostics.put(key, value);
    }

    public void addAllRequestDiagnostic(Map<String, Object> map) {
        requestDiagnostics.putAll(map);
    }

    public HashMap<String, Object> getRequestDiagnostics() {
        return requestDiagnostics;
    }


    public void addResponseDiagnostic(String key, Object value) {
        responseDiagnostics.put(key, value);
    }

    public void addAllResponseDiagnostic(Map<String, Object> map) {
        responseDiagnostics.putAll(map);
    }

    public HashMap<String, Object> getResponseDiagnostics() {
        return responseDiagnostics;
    }

    public void addResponse(String key, Object value) {
        response.put(key, value);
    }

    public void addAllResponse(Map<String, Object> map) {
        response.putAll(map);
    }

    public IntegrationResponse buildSuccess(){
        IntegrationResponse.Builder integrationResponseBuilder = IntegrationResponse.forSuccess(getResponse());
        IntegrationDesignerDiagnostic integrationDesignerDiagnostic = IntegrationDesignerDiagnostic.builder()
                .addRequestDiagnostic(getRequestDiagnostics())
                .addResponseDiagnostic(getResponseDiagnostics())
                .addExecutionTimeDiagnostic(getTiming())
                .build();
        return integrationResponseBuilder.withDiagnostic(integrationDesignerDiagnostic).build();
    }

    public HashMap<String, Object> getResponse() {
        return response;
    }

    public IntegrationResponse buildApiExceptionError(Exception e){
        IntegrationDesignerDiagnostic diagnostics = IntegrationDesignerDiagnostic.builder()
                .addRequestDiagnostic(requestDiagnostics)
                .addResponseDiagnostic(responseDiagnostics)
                .build();
        IntegrationError integrationError = IntegrationError.builder()
                .title("Could not perform action")
                .message("Could not perform requested action, verify that the inputs are valid.")
                .detail(e.getMessage())
                .build();
        IntegrationResponse.Builder integrationResponseBuilder = IntegrationResponse.forError(integrationError).withDiagnostic(diagnostics);
        return integrationResponseBuilder.build();
    }
}
