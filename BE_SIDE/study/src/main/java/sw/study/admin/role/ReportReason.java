package sw.study.admin.role;

public enum ReportReason {
    SPAM_ADVERTISING("스팸홍보/도배입니다."),
    ILLEGAL_CONTENT("불법정보를 포함하고 있습니다."),
    CONTENT_FOR_YOUTH("청소년에 유해한 내용입니다."),
    VULGAR_OR_DISCRIMINATORY_EXPRESSION("욕설/생명경시/혐오/차별적 표현입니다."),
    PERSONAL_INFORMATION_LEAK("개인정보가 노출되었습니다."),
    INAPPROPRIATE_EXPRESSION("불쾌한 표현이 있습니다.");

    private final String description;

    // 생성자를 통해 설명 값을 받음
    ReportReason(String description) {
        this.description = description;
    }

    // 설명을 반환하는 메서드
    public String getDescription() {
        return description;
    }
}
