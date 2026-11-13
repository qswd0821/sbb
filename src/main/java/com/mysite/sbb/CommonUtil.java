package com.mysite.sbb;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

/**
 * 마크다운 → HTML 변환기. 템플릿에서는 ${commonUtil.markdown(...)} 으로 호출 (GlobalModelAttributes 참고).
 */
@Component
public class CommonUtil {
    private final Parser parser = Parser.builder().build();

    // 책과 차이: 사용자가 입력한 HTML 태그(<script> 등)는 그대로 실행되지 않도록 escape,
    //          javascript: 같은 위험한 링크 URL 은 제거 → th:utext 로 출력해도 XSS 방지
    private final HtmlRenderer renderer = HtmlRenderer.builder()
            .escapeHtml(true)
            .sanitizeUrls(true)
            .build();

    public String markdown(String markdown) {
        Node document = parser.parse(markdown == null ? "" : markdown);
        return renderer.render(document);
    }
}
