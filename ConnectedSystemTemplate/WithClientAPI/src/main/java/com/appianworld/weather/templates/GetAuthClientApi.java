package com.appianworld.weather.templates;

import com.appian.connectedsystems.simplified.sdk.SimpleClientApi;
import com.appian.connectedsystems.simplified.sdk.SimpleClientApiRequest;
import com.appian.connectedsystems.templateframework.sdk.ClientApiResponse;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@TemplateId(name = "GetAuth")
public class GetAuthClientApi extends SimpleClientApi {
    private static final String url = "https://darksky.net/widget/default/0,0/us12/en.js";

    @Override
    protected ClientApiResponse execute(
            SimpleClientApiRequest simpleClientApiRequest, ExecutionContext executionContext) {
        Map<String, Object> inputValues = simpleClientApiRequest.getPayload();
        Map<String, Object> response = new HashMap<>();

        HttpTransport transport = new NetHttpTransport();
        try {
            final HttpResponse result = transport.createRequestFactory().buildGetRequest(new GenericUrl(url)).execute();
            String html = ByteString.readFrom(result.getContent()).toStringUtf8();
            Pattern pattern = Pattern.compile("&auth=(.*)&");
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()){
                response.put("authKey",matcher.group(1));
            }else{
                response.put("error","Could not find authkey in " + html);
            }
        }catch(IOException e){
            response.put("error",e.getMessage());
        }
        return new ClientApiResponse(response);
    }
}
