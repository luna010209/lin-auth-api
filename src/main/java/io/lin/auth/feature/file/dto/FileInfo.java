package io.lin.auth.feature.file.dto;

public record FileInfo(
        byte[] fileByte,
        String contentType,
        String ext
) {
}
