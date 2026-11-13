// 모든 페이지 공통 스크립트 (layout.html 에서 로드, 14168 44강)

// 마크다운 에디터 (3-14) : data-markdown-editor 속성이 있는 textarea 를 EasyMDE 로 바꾼다
// forceSync : 입력할 때마다 원래 textarea 에 값을 복사 → 폼 전송·서버 검증이 그대로 동작
document.addEventListener("DOMContentLoaded", () => {
    if (typeof EasyMDE === "undefined") return;
    document.querySelectorAll("textarea[data-markdown-editor]:not([disabled])").forEach(textarea => {
        new EasyMDE({
            element: textarea,
            forceSync: true,
            spellChecker: false,
            status: false,
            minHeight: "200px",
        });
    });
});

// 폼 검증 오류가 있으면 화면상 가장 위에 있는 오류 필드에 커서를 둔다 (14168 15강)
// (서버의 오류 메시지 순서는 일정하지 않으므로, 폼 안의 필드 순서를 기준으로 찾는다)
document.addEventListener("DOMContentLoaded", () => {
    const errorFieldNames = new Set(
        [...document.querySelectorAll("[data-error-field-name]")]
            .map(el => el.getAttribute("data-error-field-name"))
    );
    if (errorFieldNames.size === 0) return;

    const firstErrorField = [...document.querySelectorAll("input[name], textarea[name], select[name]")]
        .find(el => errorFieldNames.has(el.name));
    if (firstErrorField) firstErrorField.focus();
});
