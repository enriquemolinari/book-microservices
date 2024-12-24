package model;

import api.MoviesException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ActorTest {
    @Test
    public void charaterNameMustNotBeBlank() {
        var e = assertThrows(MoviesException.class, () -> {
            new Actor(new Person("abc",
                    "def", "abcdef@mm.com"), "");
            fail("an Actor must not be instantiated with a blank characterName");
        });
        assertEquals(Actor.CHARACTER_NAME_INVALID, e.getMessage());
    }
}

