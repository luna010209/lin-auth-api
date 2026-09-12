package io.lin.auth.utils.file;

public final class FileKeys {

    public static String materialImageKey(Long materialId, String ext, boolean ai){
        return ai ? "reference-material/" + materialId + "/ai_image." + ext :
                "reference-material/" + materialId + "/image." + ext;
    }

    public static String materialAudioKey(Long materialId, String ext, boolean ai){
        return ai ? "reference-material/" + materialId + "/ai_audio." + ext :
                "reference-material/" + materialId + "/audio." + ext;
    }

    public static String contentImageKey(Long contentId, String ext, boolean ai){
        return ai ? "content/" + contentId + "/ai_image." + ext :
                "content/" + contentId + "/image." + ext;
    }

    public static String contentAudioKey(Long contentId, String ext, boolean ai){
        return ai ? "content/" + contentId + "/ai_audio." + ext :
                "content/" + contentId + "/audio." + ext;
    }

    public static String contentOptionKey(Long contentId, String optionKey, String ext, boolean ai){
        return ai ? "content/" + contentId + "/option/ai_" + optionKey + "." + ext :
                "content/" + contentId + "/option/" + optionKey + "." + ext;
    }

    public static String learningCoverKey(Long learningId, boolean ai){
        return ai ? "learning/" + learningId + "/ai_cover.jpg" :
                "learning/" + learningId + "/cover.jpg";
    }

    public static String feedbackEvidenceKey(Long feedbackId, String ext) {
        return "feedback/" + feedbackId + "/evidence." + ext;
    }

}
