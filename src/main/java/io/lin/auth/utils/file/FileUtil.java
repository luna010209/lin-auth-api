package io.lin.auth.utils.file;

import io.lin.auth.common.dto.FileInfo;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public final class FileUtil {

    private FileUtil() {}

    /** =========================
     * Allowed content types
     ========================== */
    public static final Map<String, Set<String>> ALLOWED_TYPES = Map.of(

            // 🖼️ Images
            "image", Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            ),

            // 📄 Documents
            "document", Set.of(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "text/plain"
            ),

            // 📊 Excel
            "excel", Set.of(
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ),

            // 🎵 Audio
            "audio", Set.of(
                    "audio/mpeg",
                    "audio/wav",
                    "audio/ogg",
                    "audio/webm"
            ),

            // 🎬 Video
            "video", Set.of(
                    "video/mp4",
                    "video/webm"
            )
    );

    public static final Map<String, String> CONTENT_TYPE_TO_EXT = Map.ofEntries(

            // 🖼️ Images
            Map.entry("image/jpeg", "jpg"),
            Map.entry("image/png", "png"),
            Map.entry("image/webp", "webp"),

            // 📄 Documents
            Map.entry("application/pdf", "pdf"),
            Map.entry("application/msword", "doc"),
            Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
            Map.entry("text/plain", "txt"),

            // 📊 Excel
            Map.entry("application/vnd.ms-excel", "xls"),
            Map.entry("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),

            // 🎵 Audio
            Map.entry("audio/mpeg", "mp3"),
            Map.entry("audio/wav", "wav"),
            Map.entry("audio/ogg", "ogg"),
            Map.entry("audio/webm", "webm"),

            // 🎬 Video
            Map.entry("video/mp4", "mp4"),
            Map.entry("video/webm", "webm")
    );


    public static boolean isValid(String category, String contentType) {
        if (contentType==null || contentType.isEmpty()) return false;
        return ALLOWED_TYPES.containsKey(category)
                && ALLOWED_TYPES.get(category).contains(contentType);
    }

    public static boolean isAllowed(String contentType) {
        return ALLOWED_TYPES.values().stream()
                .anyMatch(set -> set.contains(contentType));
    }

    /** =========================
     * Get extension from content type
     ========================== */

    public static FileInfo processFile(MultipartFile file) throws IOException {

        String contentType = file.getContentType();

        // IMAGE → compress + resize (keep ratio)
        if (FileUtil.isValid("image", contentType)) {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thumbnails.of(file.getInputStream())
                    .size(1024, 1024)
                    .keepAspectRatio(true)
                    .outputQuality(0.7)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);

            return new FileInfo(outputStream.toByteArray(), "image/jpeg", "jpg");

        }

        // OTHER FILES → keep original
        return new FileInfo(file.getBytes(), contentType, CONTENT_TYPE_TO_EXT.get(contentType));
    }
}
