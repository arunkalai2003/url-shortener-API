package com.example.shortener.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class CreateRateLimitFilter extends OncePerRequestFilter {
    private final DistributedRateLimiter limiter;
    public CreateRateLimitFilter(DistributedRateLimiter limiter){this.limiter=limiter;}
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
        if("POST".equals(req.getMethod()) && "/api/v1/urls".equals(req.getRequestURI())){
            String ip=req.getHeader("X-Forwarded-For"); if(ip==null||ip.isBlank())ip=req.getRemoteAddr(); else ip=ip.split(",")[0].trim();
            if(!limiter.allow(ip)){
                res.setStatus(429);res.setContentType(MediaType.APPLICATION_JSON_VALUE);res.getWriter().write("{\"code\":\"RATE_LIMITED\",\"message\":\"Too many create requests\"}");return;
            }
        }
        chain.doFilter(req,res);
    }
}
