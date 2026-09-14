package com.atlasops.boot.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/** Normalizes the OpenAPI endpoint path emitted during API scans. */
public class OpenApiTrailingSlashFilter extends OncePerRequestFilter {

  private static final String API_DOCS_WITH_TRAILING_SLASH = "/v3/api-docs/";
  private static final String API_DOCS = "/v3/api-docs";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (API_DOCS_WITH_TRAILING_SLASH.equals(request.getRequestURI())) {
      request.getRequestDispatcher(API_DOCS).forward(request, response);
      return;
    }

    filterChain.doFilter(request, response);
  }
}
