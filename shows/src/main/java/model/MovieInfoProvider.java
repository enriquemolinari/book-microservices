package model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MovieInfoProvider {
    Map<Long, MovieInfo> movies(List<Long> ids);
}

record MovieInfo(Long id, String name, String duration, Set<String> genres) {
}