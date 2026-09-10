package com.example.shortener.service;

import com.example.shortener.dto.AnalyticsResponse;
import com.example.shortener.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AnalyticsService {
    private final AnalyticsSummaryRepository summary; private final AnalyticsGeoRepository geo;
    public AnalyticsService(AnalyticsSummaryRepository summary,AnalyticsGeoRepository geo){this.summary=summary;this.geo=geo;}
    public AnalyticsResponse get(String code){
        var s=summary.findById(code).orElse(null);
        long count=s==null?0:s.getAccessCount();
        var last=s==null?null:s.getLastAccessedAt();
        List<AnalyticsResponse.GeoCount> loc=geo.findByShortCode(code).stream().map(g->new AnalyticsResponse.GeoCount(g.getCountry(),g.getRegion(),g.getAccessCount())).toList();
        return new AnalyticsResponse(code,count,last,loc);
    }
}
