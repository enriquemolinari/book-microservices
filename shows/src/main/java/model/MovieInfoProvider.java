package model;

import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public interface MovieInfoProvider {
    Map<Long, MovieInfo> moviesBy(List<Long> ids);
}
