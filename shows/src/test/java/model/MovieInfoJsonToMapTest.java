package model;

import api.ShowsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MovieInfoJsonToMapTest {

    @Test
    public void validSerialization() {
        String json = """
                [
                    {
                        "id": 3,
                        "name": "Crash Tea",
                        "duration": "1hr 45mins",
                        "genres": [
                            "Comedy"
                        ]
                    },
                    {
                        "id": 2,
                        "name": "Small Fish",
                        "duration": "2hrs 05mins",
                        "genres": [
                            "Adventure",
                            "Drama"
                        ]
                    }
                ]
                """;
        var map = new MovieInfoJsonToMap(json).convert();
        assertEquals(2, map.size()); // 2 movies
        assertEquals("Crash Tea", map.get(3L).name());
        assertEquals("Small Fish", map.get(2L).name());
        assertEquals("1hr 45mins", map.get(3L).duration());
        assertEquals("2hrs 05mins", map.get(2L).duration());
        assertEquals(1, map.get(3L).genres().size());
        assertEquals(2, map.get(2L).genres().size());
        assertTrue(map.get(3L).genres().contains("Comedy"));
        assertTrue(map.get(2L).genres().contains("Drama"));
        assertTrue(map.get(2L).genres().contains("Adventure"));
    }

    @Test
    void testConverWithEmptyJsonString() {
        var json = "";
        var e = serializeWithThrows(json);
        assertEquals(MovieInfoJsonToMap.REQUIRES_A_VALID_JSON_STRING, e.getMessage());
    }

    @Test
    void testConvertWithInvalidJson() {
        var json = "[{]";
        var e = serializeWithThrows(json);
        assertEquals(MovieInfoJsonToMap.INVALID_JSON_STRING_FORMAT, e.getMessage());
    }

    private ShowsException serializeWithThrows(String json) {
        return assertThrows(ShowsException.class
                , () -> new MovieInfoJsonToMap(json).convert());
    }
}

