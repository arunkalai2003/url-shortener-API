package com.example.shortener.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String id=req.getHeader("X-Correlation-Id"); if(id==null || id.isBlank()) id=UUID.randomUUID().toString();
        req.setAttribute("correlationId",id); res.setHeader("X-Correlation-Id",id); chain.doFilter(req,res);
    }
}
