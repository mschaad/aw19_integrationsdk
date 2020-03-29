package com.appianworld.weather.templates;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.simplified.sdk.connectiontesting.SimpleTestableConnectedSystemTemplate;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.connectiontesting.TestConnectionResult;
import tk.plogitech.darksky.forecast.*;
import tk.plogitech.darksky.forecast.model.Latitude;
import tk.plogitech.darksky.forecast.model.Longitude;

@TemplateId(name = "WeatherForecastConnectedSystemTemplate")
public class WeatherForecastConnectedSystemTemplate extends SimpleTestableConnectedSystemTemplate {
    final static String API_KEY = "API_KEY";

    @Override
    protected SimpleConfiguration getConfiguration(
            SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {
        return simpleConfiguration.setProperties(
                textProperty(API_KEY)
                        .label("DarkSky API Key")
                        .description("For more information: https://darksky.net/dev/account")
                        .isImportCustomizable(true)
                        .isRequired(true)
                        .build()
        );
    }

    @Override
    protected TestConnectionResult testConnection(SimpleConfiguration configuration, ExecutionContext executionContext) {
        try {
            ForecastRequest request = new ForecastRequestBuilder()
                    .key(new APIKey(configuration.getValue(API_KEY)))
                    .location(new GeoCoordinates(new Longitude(13.377704), new Latitude(52.516275))).build();

            DarkSkyClient client = new DarkSkyClient();
            client.forecastJsonString(request);
        } catch (ForecastException e) {
            return TestConnectionResult.error(e.getMessage());
        }
        return TestConnectionResult.success();
    }
}