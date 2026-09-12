package io.lin.auth.storage;

import io.lin.auth.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class R2Storage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloudflare.r2.bucket}")
    private String bucketName;

    @Value("${app.cdn.url}")
    private String cdnUrl;

    /** =========================
     * Upload TEXT
     ========================== */
    public String putString(String key, String contentType, String content) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(req, RequestBody.fromString(content));
        return buildUrl(key); // returns public URL
    }

    /** =========================
     * Upload BYTES
     ========================== */
    public String putBytes(String key, String contentType, byte[] bytes) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(req, RequestBody.fromBytes(bytes));
        return buildUrl(key); // returns public URL
    }

    /** =========================
     * Upload MULTIPART
     ========================== */
    public String putMultipart(String folder, MultipartFile file) throws IOException {
        String extension = getExtension(file.getOriginalFilename());
        String key = folder + "/" + UUID.randomUUID() + extension;

        String contentType = file.getContentType() != null
                ? file.getContentType()
                : "application/octet-stream";

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(req, RequestBody.fromBytes(file.getBytes()));
        return buildUrl(key); // returns public URL
    }

    /** =========================
     * PRESIGNED DOWNLOAD (PRIVATE FILE)
     ========================== */
    public String presignDownloadUrl(String key) {
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(getReq)
                .build();

        return s3Presigner.presignGetObject(presignReq).url().toString();
    }

    /** =========================
     * AVATAR (PUBLIC FILE)
     ========================== */
    public String publicUrl(String key) {
        return buildUrl(key); // returns public URL
    }

    /** =========================
     * DELETE
     ========================== */
    public void delete(String key) {
        if (!exists(key)) {
            return;
        }

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    /** =========================
     * CHECK EXISTS
     ========================== */
    public boolean exists(String key) {
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build()
            );
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) return false;
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "error.file.access_failed");
        }
    }

    /** =========================
     * HELPERS
     ========================== */
    private String buildUrl(String key) {
        String base = cdnUrl.endsWith("/") ? cdnUrl.substring(0, cdnUrl.length() - 1) : cdnUrl;
        String path = key.startsWith("/") ? key.substring(1) : key;
        return base + "/" + path;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
