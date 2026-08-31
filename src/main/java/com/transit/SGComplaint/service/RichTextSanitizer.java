package com.transit.SGComplaint.service;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

@Component
public class RichTextSanitizer {

    private static final Set<HTML.Tag> ALLOWED_TAGS = Set.of(
            HTML.Tag.P, HTML.Tag.DIV, HTML.Tag.BR,
            HTML.Tag.B, HTML.Tag.STRONG, HTML.Tag.I, HTML.Tag.EM, HTML.Tag.U,
            HTML.Tag.UL, HTML.Tag.OL, HTML.Tag.LI, HTML.Tag.BLOCKQUOTE,
            HTML.Tag.FONT, HTML.Tag.IMG
    );
    private static final Set<String> ALLOWED_FONTS = Set.of(
            "Arial", "Georgia", "Tahoma", "Verdana", "맑은 고딕", "Malgun Gothic"
    );

    public String sanitize(String html) {
        if (html == null) return "";
        StringBuilder result = new StringBuilder();

        parse(html, new HTMLEditorKit.ParserCallback() {
            private int suppressedDepth;

            @Override
            public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
                if (isSuppressed(tag)) {
                    suppressedDepth++;
                    return;
                }
                if (suppressedDepth > 0 || !ALLOWED_TAGS.contains(tag)) return;
                result.append('<').append(tag);
                appendSafeFontAttributes(result, tag, attributes);
                if (tag == HTML.Tag.IMG) appendSafeImageAttributes(result, attributes);
                result.append('>');
            }

            @Override
            public void handleEndTag(HTML.Tag tag, int position) {
                if (isSuppressed(tag)) {
                    suppressedDepth = Math.max(0, suppressedDepth - 1);
                    return;
                }
                if (suppressedDepth == 0 && ALLOWED_TAGS.contains(tag) && tag != HTML.Tag.BR) {
                    if (tag == HTML.Tag.IMG) return;
                    result.append("</").append(tag).append('>');
                }
            }

            @Override
            public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
                if (suppressedDepth > 0) return;
                if (tag == HTML.Tag.BR) result.append("<br>");
                if (tag == HTML.Tag.IMG) {
                    result.append("<img");
                    appendSafeImageAttributes(result, attributes);
                    result.append('>');
                }
            }

            @Override
            public void handleText(char[] data, int position) {
                if (suppressedDepth == 0) {
                    result.append(HtmlUtils.htmlEscape(new String(data)));
                }
            }
        });
        return result.toString().trim();
    }

    public int plainTextLength(String html) {
        if (html == null) return 0;
        StringBuilder text = new StringBuilder();
        parse(html, new HTMLEditorKit.ParserCallback() {
            private int suppressedDepth;

            @Override
            public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
                if (isSuppressed(tag)) suppressedDepth++;
            }

            @Override
            public void handleEndTag(HTML.Tag tag, int position) {
                if (isSuppressed(tag)) suppressedDepth = Math.max(0, suppressedDepth - 1);
            }

            @Override
            public void handleText(char[] data, int position) {
                if (suppressedDepth == 0) text.append(data);
            }
        });
        return text.toString().trim().length();
    }

    private void parse(String html, HTMLEditorKit.ParserCallback callback) {
        try {
            new ParserDelegator().parse(new StringReader(html), callback, true);
        } catch (IOException exception) {
            throw new ComplaintException("민원 내용을 처리할 수 없습니다.", exception);
        }
    }

    private static boolean isSuppressed(HTML.Tag tag) {
        return tag == HTML.Tag.SCRIPT || tag == HTML.Tag.STYLE;
    }

    private static void appendSafeFontAttributes(
            StringBuilder result,
            HTML.Tag tag,
            MutableAttributeSet attributes) {
        if (tag != HTML.Tag.FONT) return;

        Object faceAttribute = attributes.getAttribute(HTML.Attribute.FACE);
        String face = faceAttribute == null ? "" : faceAttribute.toString();
        if (ALLOWED_FONTS.contains(face)) {
            result.append(" face=\"").append(HtmlUtils.htmlEscape(face)).append('"');
        }

        Object sizeAttribute = attributes.getAttribute(HTML.Attribute.SIZE);
        String size = sizeAttribute == null ? "" : sizeAttribute.toString();
        if (size.matches("[1-7]")) {
            result.append(" size=\"").append(size).append('"');
        }
    }

    private static void appendSafeImageAttributes(
            StringBuilder result,
            MutableAttributeSet attributes) {
        Object sourceAttribute = attributes.getAttribute(HTML.Attribute.SRC);
        String source = sourceAttribute == null ? "" : sourceAttribute.toString();
        if (!(source.matches("notice-image:[0-4]")
                || source.matches("/notices/images/[0-9]+"))) {
            return;
        }
        result.append(" src=\"").append(source).append('"');

        Object altAttribute = attributes.getAttribute(HTML.Attribute.ALT);
        String alt = altAttribute == null ? "공지사항 이미지" : altAttribute.toString();
        result.append(" alt=\"")
                .append(HtmlUtils.htmlEscape(alt.length() > 100 ? alt.substring(0, 100) : alt))
                .append('"');
    }
}
