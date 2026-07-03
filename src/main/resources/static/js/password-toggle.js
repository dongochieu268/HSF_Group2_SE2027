document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("[data-password-toggle]").forEach(function (button) {
        var targetId = button.getAttribute("data-password-toggle");
        var input = document.getElementById(targetId);

        if (!input || input.tagName !== "INPUT") {
            return;
        }

        button.addEventListener("click", function () {
            var shouldShow = input.type === "password";
            input.type = shouldShow ? "text" : "password";
            button.textContent = shouldShow ? "Hide" : "Show";
            button.setAttribute("aria-label", shouldShow ? "Hide password" : "Show password");
            button.setAttribute("aria-pressed", String(shouldShow));
        });
    });
});
