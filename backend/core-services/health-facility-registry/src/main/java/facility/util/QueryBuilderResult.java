package facility.util;

import lombok.Getter;

import java.util.List;

@Getter
public class QueryBuilderResult {
    private final String whereClause;
    private final List<Object> params;

    public QueryBuilderResult(String whereClause, List<Object> params) {
        this.whereClause = whereClause;
        this.params = params;
    }

}
