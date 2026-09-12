package io.lin.auth.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CursorPageResponse<T>(
        List<T> items,
        Long nextCursor,
        boolean hasNext
) {
}
