package model;

import java.util.Set;

public record MovieInfo(Long id, String name, String duration,
                        Set<String> genres) {
}
