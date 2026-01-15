package org.ostech.gtdcardsbackend.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Slf4j
@Component
public class HttpLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        long startTime = System.currentTimeMillis();

        RequestWrapper wrappedRequest = new RequestWrapper(httpRequest);
        ResponseWrapper wrappedResponse = new ResponseWrapper(httpResponse);

        try {
            logRequest(wrappedRequest);
            chain.doFilter(wrappedRequest, wrappedResponse);
            long processingTime = System.currentTimeMillis() - startTime;
            logResponse(wrappedResponse, processingTime);
            wrappedResponse.writeToResponse(httpResponse);
        } catch (Exception ex) {
            log.error("Filter error: {}", ex.getMessage());
            throw new ServletException(ex);
        }
    }

    private void logRequest(RequestWrapper request) {
        StringBuilder sb = new StringBuilder();
        sb.append("[REQUEST] ").append(request.getMethod()).append(" ").append(request.getRequestURI());
        if (request.getQueryString() != null) {
            sb.append("?").append(request.getQueryString());
        }
        log.info(sb.toString());

        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String headerName = headers.nextElement();
            //TODO:: Mask sensitive data in the Headers
            String headerValue = isSensitiveHeader(headerName) ? "***MASKED***" : request.getHeader(headerName);
            log.info("  {}: {}", headerName, headerValue);
        }

        String body = request.getBody();
        //TODO:: Mask sensitive data in the body
        if (body != null && !body.isEmpty()) {
            log.info("  Body: {}", body);
        }
    }

    private void logResponse(ResponseWrapper response, long processingTime) {
        log.info("[RESPONSE] Status: {} | Time: {}ms | Content-Type: {}",
                response.getStatus(), processingTime, response.getContentType());

        for (String headerName : response.getHeaderNames()) {
            String headerValue = isSensitiveHeader(headerName) ? "***MASKED***" : response.getHeader(headerName);
            log.info("  {}: {}", headerName, headerValue);
        }

        String body = response.getContentAsString();
        if (body != null && !body.isEmpty()) {
            //TODO:: Mask sensitive data in the body
            log.info("  Body: {}", body);
        }
    }

    private boolean isSensitiveHeader(String header) {
        String lower = header.toLowerCase();
        return lower.contains("authorization") || lower.contains("cookie") ||
               lower.contains("token") || lower.contains("password") || lower.contains("api-key");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    public static class RequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        private byte[] cachedBody;

        public RequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            this.cachedBody = request.getInputStream().readAllBytes();
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new ServletInputStream() {
                private ByteArrayInputStream stream = new ByteArrayInputStream(cachedBody);

                @Override
                public int read() throws IOException {
                    return stream.read();
                }

                @Override
                public boolean isFinished() {
                    return stream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                }
            };
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cachedBody)));
        }

        public String getBody() {
            return new String(cachedBody, StandardCharsets.UTF_8);
        }
    }

    public static class ResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        private ServletOutputStream servletOutput;

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
            this.servletOutput = new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    output.write(b);
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener listener) {
                }
            };
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return servletOutput;
        }

        public String getContentAsString() {
            return output.toString(StandardCharsets.UTF_8);
        }

        public void writeToResponse(HttpServletResponse response) throws IOException {
            byte[] content = output.toByteArray();
            response.getOutputStream().write(content);
            response.getOutputStream().flush();
        }
    }
}
