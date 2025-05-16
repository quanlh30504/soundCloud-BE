package com.example.soundCloud_BE.zingMp3;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;


@Configuration
public class ZingMp3Config {

    @Bean
    public RestTemplate zingMp3RestTemplate(ObjectMapper objectMapper) {
        // Tạo HttpClient hỗ trợ giải nén gzip tự động
        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setResponseTimeout(Timeout.ofSeconds(10)) // Timeout 10s
                                .build()
                )
                .build();

        // Sử dụng HttpComponentsClientHttpRequestFactory để hỗ trợ gzip
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(Duration.ofSeconds(5)); // Timeout kết nối 5s

        // Khởi tạo RestTemplate
        RestTemplate restTemplate = new RestTemplate(factory);

        // Cấu hình converters

        // JSON converter
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        jsonConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        // String converter hỗ trợ text/html, text/plain, application/octet-stream
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.TEXT_HTML,
                MediaType.TEXT_PLAIN,
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_OCTET_STREAM
        ));

        // ByteArray converter cho dữ liệu nhị phân
        ByteArrayHttpMessageConverter byteArrayConverter = new ByteArrayHttpMessageConverter();
        byteArrayConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_OCTET_STREAM,
                MediaType.ALL
        ));
        restTemplate.setMessageConverters(Arrays.asList(jsonConverter, stringConverter));

        // Thêm interceptor để debug (tùy chọn)
        restTemplate.getInterceptors().add((request, body, execution) -> {
            // Log request
            System.out.println("Request URL: " + request.getURI());
//            System.out.println("Request Headers: " + request.getHeaders());
//
            ClientHttpResponse response = execution.execute(request, body);
//
//            // Log response headers
//            System.out.println("Response Headers: " + response.getHeaders());
            return response;
        });

        return restTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
    }


}
