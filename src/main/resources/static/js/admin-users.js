(function () {
    function openDialog(dialog) {
        if (!dialog) return;
        if (typeof dialog.showModal === "function") {
            if (!dialog.open) dialog.showModal();
        } else {
            dialog.setAttribute("open", "open");
        }
    }

    function closeDialog(dialog) {
        if (!dialog) return;
        if (typeof dialog.close === "function") {
            dialog.close();
        } else {
            dialog.removeAttribute("open");
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll("[data-dialog-open]").forEach(function (trigger) {
            trigger.addEventListener("click", function () {
                openDialog(document.getElementById(trigger.getAttribute("data-dialog-open")));
            });
        });

        document.querySelectorAll("[data-dialog-close]").forEach(function (trigger) {
            trigger.addEventListener("click", function () {
                closeDialog(trigger.closest("dialog"));
            });
        });

        document.querySelectorAll("dialog[data-open-on-load='true']").forEach(openDialog);

        document.querySelectorAll("dialog").forEach(function (dialog) {
            dialog.addEventListener("click", function (event) {
                if (event.target === dialog) {
                    closeDialog(dialog);
                }
            });
        });
    });
})();
