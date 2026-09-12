package io.lin.auth.common.dto;

public record FileInfo(
        byte[] fileByte,
        String contentType,
        String ext
) {
}
