package dev.sara.micos_color_code.feedback;

public class FeedbackRequestDTO {
    private String email;
    private String message;

    public FeedbackRequestDTO() {}

    public FeedbackRequestDTO(String email, String message) {
        this.email = email;
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}