package com.appianworld.weather.templates;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.google.gson.Gson;
import tk.plogitech.darksky.forecast.*;
import tk.plogitech.darksky.forecast.model.Latitude;
import tk.plogitech.darksky.forecast.model.Longitude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appianworld.weather.templates.WeatherForecastConnectedSystemTemplate.API_KEY;

@TemplateId(name = "GetWeatherIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class GetWeatherIntegrationTemplate extends SimpleIntegrationTemplate {

    private final static String LATITUDE = "LATITUDE";
    private final static String LONGITUDE = "LONGTITUDE";
    private final static String EXCLUDE_ALERTS = "EXCLUDE_ALERTS";
    private final static String EXCLUDE_CURRENT = "EXCLUDE_CURRENT";
    private final static String EXCLUDE_DAILY = "EXCLUDE_DAILY";
    private final static String EXCLUDE_HOURLY = "EXCLUDE_HOURLY";
    private final static String EXCLUDE_MINUTELY = "EXCLUDE_MINUTELY";
    private final static String EXCLUDE_FLAGS = "EXCLUDE_FLAGS";

    @Override
    protected SimpleConfiguration getConfiguration(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            PropertyPath propertyPath,
            ExecutionContext executionContext) {
        return integrationConfiguration.setProperties(
                doubleProperty(LATITUDE)
                        .label("Latitude")
                        .description("Latitude must be between -90 and 90")
                        .isExpressionable(true)
                        .isRequired(true)
                        .build(),
                doubleProperty(LONGITUDE)
                        .label("Longitude")
                        .description("Longitude must be between -180 and 180")
                        .isExpressionable(true)
                        .isRequired(false)
                        .build(),
                booleanProperty(EXCLUDE_ALERTS)
                        .label("Disable Weather Alerts")
                        .description("Excludes weather alerts in the response")
                        .isExpressionable(true)
                        .build(),
                booleanProperty(EXCLUDE_CURRENT)
                        .label("Disable Current Weather")
                        .description("Excludes current weather in the response")
                        .isExpressionable(true)
                        .build(),
                booleanProperty(EXCLUDE_DAILY)
                        .label("Disable Daily Weather Forecast")
                        .description("Excludes daily weather forecast in the response")
                        .isExpressionable(true)
                        .build(),
                booleanProperty(EXCLUDE_HOURLY)
                        .label("Disable Hourly Weather Forecast")
                        .description("Excludes hourly weather forecast in the response")
                        .isExpressionable(true)
                        .build(),
                booleanProperty(EXCLUDE_MINUTELY)
                        .label("Disable Minutely Weather Forecast")
                        .description("Excludes minutely weather forecast in the response")
                        .isExpressionable(true)
                        .build(),
                booleanProperty(EXCLUDE_FLAGS)
                        .label("Disable Weather Metadata")
                        .description("Excludes metadata about weather stations and data sources used for the current weather and forecasts")
                        .isExpressionable(true)
                        .build()
        );
    }

    @Override
    protected IntegrationResponse execute(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            ExecutionContext executionContext) {
        Gson gson = new Gson();
        ConnectedSystemUtil util = new ConnectedSystemUtil("ForecastRequest.forecastJsonString");
        Double latitude = integrationConfiguration.getValue(LATITUDE);
        Double longitude = integrationConfiguration.getValue(LONGITUDE);
        boolean excludeAlerts = Boolean.TRUE.equals(integrationConfiguration.getValue(EXCLUDE_ALERTS));
        boolean excludeCurrent = Boolean.TRUE.equals(integrationConfiguration.getValue(EXCLUDE_CURRENT));
        boolean excludeDaily = Boolean.TRUE.equals(integrationConfiguration.getValue(EXCLUDE_DAILY));
        boolean excludeHourly = Boolean.TRUE.equals(integrationConfiguration.getValue(EXCLUDE_HOURLY));
        boolean excludeMinutely = Boolean.TRUE.equals(integrationConfiguration.getValue(EXCLUDE_MINUTELY));
        boolean excludeFlags = Boolean.TRUE.equals(integrationConfiguration.getValue(EXCLUDE_FLAGS));

        List<ForecastRequestBuilder.Block> blocks = new ArrayList<>();
        if (excludeAlerts) {
            blocks.add(ForecastRequestBuilder.Block.alerts);
        }
        if (excludeCurrent) {
            blocks.add(ForecastRequestBuilder.Block.currently);
        }
        if (excludeDaily) {
            blocks.add(ForecastRequestBuilder.Block.daily);
        }
        if (excludeHourly) {
            blocks.add(ForecastRequestBuilder.Block.hourly);
        }
        if (excludeMinutely) {
            blocks.add(ForecastRequestBuilder.Block.minutely);
        }
        if (excludeFlags) {
            blocks.add(ForecastRequestBuilder.Block.flags);
        }

        util.addAllRequestDiagnostic(getRequestDiagnostic(latitude, longitude, blocks));

        ForecastRequest request = new ForecastRequestBuilder()
                .key(new APIKey(connectedSystemConfiguration.getValue(API_KEY)))
                .location(new GeoCoordinates(new Longitude(longitude), new Latitude(latitude)))
                .exclude(blocks.toArray(new ForecastRequestBuilder.Block[0]))
                .language(ForecastRequestBuilder.Language.valueOf(executionContext.getExecutionLocale().getLanguage()))
                .build();

        DarkSkyClient client = new DarkSkyClient();
        util.startTiming();
        try {
            util.addAllResponse(gson.fromJson(client.forecastJsonString(request), new HashMap<String, Object>().getClass()));
        } catch (ForecastException e) {
            return util.buildApiExceptionError(e);
        }
        util.stopTiming();
        return util.buildSuccess();
    }

    private Map<String, Object> getRequestDiagnostic(Double latitude, Double longitude, List<ForecastRequestBuilder.Block> blocks) {
        Map<String, Object> diagnostic = new HashMap<>();
        diagnostic.put("Latitude", latitude);
        diagnostic.put("Longitude", longitude);
        diagnostic.put("Disabled Features", blocks);
        return diagnostic;
    }

}
