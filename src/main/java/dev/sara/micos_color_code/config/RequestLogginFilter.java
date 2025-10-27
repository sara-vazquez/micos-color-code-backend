package dev.sara.micos_color_code.config;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class RequestLogginFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
         if (request.getRequestURI().contains("/feedback")) {
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
            
            filterChain.doFilter(wrappedRequest, response);
            
            byte[] content = wrappedRequest.getContentAsByteArray();
            System.out.println("📦 Body RAW length: " + content.length);
            if (content.length > 0) {
                String body = new String(content, wrappedRequest.getCharacterEncoding());
                System.out.println("📦 Body RAW: [" + body + "]");
            } else {
                System.out.println("⚠️ Body está vacío!");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
