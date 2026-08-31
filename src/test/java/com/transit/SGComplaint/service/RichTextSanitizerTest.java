package com.transit.SGComplaint.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RichTextSanitizerTest {

    private final RichTextSanitizer sanitizer = new RichTextSanitizer();

    @Test
    void keepsEditorFormattingAndRemovesDangerousMarkup() {
        String result = sanitizer.sanitize(
                "<div onclick=\"alert(1)\"><b>안전한 내용</b>" +
                "<script>alert(1)</script><img src=x onerror=alert(1)></div>"
        );

        assertThat(result).contains("<b>안전한 내용</b>");
        assertThat(result).doesNotContain("onclick", "script", "src=\"x\"", "onerror", "alert(1)");
    }

    @Test
    void keepsOnlyManagedNoticeImages() {
        String result = sanitizer.sanitize(
                "<p>본문</p><img src=\"notice-image:0\" alt=\"노선 안내\" onerror=\"alert(1)\">" +
                "<img src=\"https://example.com/tracker.png\">"
        );

        assertThat(result).contains("<img src=\"notice-image:0\" alt=\"노선 안내\">");
        assertThat(result).doesNotContain("https://", "onerror", "alert(1)");
    }

    @Test
    void countsOnlyVisibleText() {
        assertThat(sanitizer.plainTextLength("<p><b>민원</b> 내용</p>"))
                .isEqualTo(5);
    }
}
