package com.example.shortener.service;

import com.example.shortener.config.ShortenerProperties;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ShortCodeGeneratorTest {
    private ShortenerProperties p(){return new ShortenerProperties("http://x",8,5,Duration.ofSeconds(30),Duration.ofMinutes(30),Duration.ofSeconds(20),Duration.ofDays(1825),60, Set.of());}
    @Test void generatesHumanFriendlyUnpredictableLookingCodes(){
        ShortCodeGenerator g=new ShortCodeGenerator(p());
        String a=g.generate(),b=g.generate();
        assertEquals(8,a.length()); assertNotEquals(a,b); assertTrue(a.matches("[123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]{8}"));
    }
}
