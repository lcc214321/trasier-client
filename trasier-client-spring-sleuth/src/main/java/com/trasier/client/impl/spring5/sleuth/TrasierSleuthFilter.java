package com.trasier.client.impl.spring5.sleuth;

import com.google.gson.GsonBuilder;
import com.trasier.client.TrasierConstants;
import com.trasier.client.impl.spring5.interceptor.servlet.AbstractTrasierFilter;
import com.trasier.client.impl.spring5.sleuth.servlet.CachedServletRequestWrapper;
import com.trasier.client.impl.spring5.sleuth.servlet.CachedServletResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanAccessor;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class TrasierSleuthFilter extends AbstractTrasierFilter {
    private final SpanAccessor spanAccessor;

    @Autowired
    public TrasierSleuthFilter(SpanAccessor spanAccessor) {
        this.spanAccessor = spanAccessor;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (isEnabled(servletRequest)) {
            CachedServletRequestWrapper request = CachedServletRequestWrapper.create((HttpServletRequest) servletRequest);
            CachedServletResponseWrapper response = CachedServletResponseWrapper.create((HttpServletResponse) servletResponse);

            Span currentSpan = spanAccessor.getCurrentSpan();
            String conversationId = request.getHeader(TrasierConstants.HEADER_CONVERSATION_ID);
            currentSpan.tag(TrasierSleuthConstants.TAG_CONVERSATION_ID, conversationId);
            currentSpan.tag(TrasierSleuthConstants.TAG_OPERATION_NAME, extractOperationName(request));

            Map<String, String> requestHeaders = getRequestHeaders(request);
            Map<String, List<String>> parameters = getRequestParameters(request);
            String requestMessage = new GsonBuilder().setPrettyPrinting().create().toJson(Arrays.asList(requestHeaders, parameters));
            currentSpan.tag(TrasierSleuthConstants.TAG_REQUEST_MESSAGE, requestMessage);

            try {
                filterChain.doFilter(request, response);
                String responseBody = new String(response.getContentAsByteArray());
                currentSpan.tag(TrasierSleuthConstants.TAG_RESPONSE_MESSAGE, responseBody);
            } catch (Exception e) {
                currentSpan.tag(TrasierSleuthConstants.TAG_RESPONSE_MESSAGE, Boolean.toString(true));
                currentSpan.tag(TrasierSleuthConstants.TAG_RESPONSE_MESSAGE, extractStackTrace(e));
                //handle exception to log and rethrow
            }

            //TODO handle header and parameters
//            Map<String, Integer> statusMap = Collections.singletonMap("status", response.getStatus());
//            Map<String, String> responseHeaders = getResponseHeaders(response);
//            String responseMessage = new GsonBuilder().setPrettyPrinting().create().toJson(Arrays.asList(statusMap, responseHeaders, responseBody));


        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private String extractStackTrace(Exception e) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outputStream);
        e.printStackTrace(ps);
        ps.close();
        return outputStream.toString();
    }
}