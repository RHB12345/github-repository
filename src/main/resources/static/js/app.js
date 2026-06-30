$(function () {
    let latestAiSuggestion = null;

    initQihangPremiumShell();
    initBlurWords();
    initReveal();
    initCountUp();
    initFavorite();
    initAiPublishAssistant();
    initAiSearch();
    initLoginExperience();
    initToasts();
    initLazyMedia();
    initAiCustomerService();
    initAdminProductAjax();
    initCharts();

    function initQihangPremiumShell() {
        const path = window.location.pathname;
        if (path === "/" || $("body").hasClass("premium-home") || $("#qihangPremiumBg").length) {
            return;
        }

        const pageClass = path.startsWith("/products/new") || path.startsWith("/products/publish") ? "qihang-page-publish"
            : /^\/products\/\d+/.test(path) ? "qihang-page-detail"
                : path.startsWith("/products") ? "qihang-page-market"
                    : path.startsWith("/admin") ? "qihang-page-admin"
                        : path.startsWith("/messages") ? "qihang-page-message"
                            : path.startsWith("/orders") ? "qihang-page-order"
                                : path.startsWith("/user") ? "qihang-page-profile"
                                    : path.startsWith("/login") || path.startsWith("/register") ? "qihang-page-auth"
                                        : "qihang-page-general";

        $("body").addClass("qihang-premium-page " + pageClass);
        $("body").prepend(
            "<div id=\"qihangPremiumBg\" class=\"qihang-premium-bg\" aria-hidden=\"true\">" +
            "<div class=\"qihang-premium-bg-image\"></div>" +
            "<div class=\"qihang-premium-bg-grid\"></div>" +
            "<div class=\"qihang-premium-bg-vignette\"></div>" +
            "</div>"
        );
    }

    function initReveal() {
        const revealTargets = document.querySelectorAll(".product-card, .value-grid > div, .form-board, .side-card, .chart-card, .order-item, .message-item, .gallery-board, .detail-info, .comments-board, .ai-panel, .ai-detail-board, .reveal-card, .report-card, .blur-words");
        if ("IntersectionObserver" in window) {
            const observer = new IntersectionObserver(function (entries) {
                entries.forEach(function (entry) {
                    if (entry.isIntersecting) {
                        entry.target.classList.add("is-visible");
                        observer.unobserve(entry.target);
                    }
                });
            }, {threshold: 0.14});
            revealTargets.forEach(function (target) {
                target.classList.add("reveal-ready");
                observer.observe(target);
            });
        } else {
            revealTargets.forEach(function (target) {
                target.classList.add("is-visible");
            });
        }
    }

    function initBlurWords() {
        $(".blur-words").each(function () {
            const element = this;
            if (element.dataset.blurReady === "true") {
                return;
            }
            const words = (element.textContent || "").trim().split(/\s+/);
            element.innerHTML = words.map(function (word, index) {
                return "<span style=\"--word-index:" + index + "\">" + escapeHtml(word) + "</span>";
            }).join(" ");
            element.dataset.blurReady = "true";
        });
    }

    function initCountUp() {
        $(".count-up").each(function () {
            const $item = $(this);
            const target = Number($item.data("count")) || Number($item.text()) || 0;
            const duration = 900;
            const start = Date.now();
            const tick = function () {
                const progress = Math.min((Date.now() - start) / duration, 1);
                const eased = 1 - Math.pow(1 - progress, 3);
                $item.text(Math.round(target * eased));
                if (progress < 1) {
                    requestAnimationFrame(tick);
                }
            };
            tick();
        });
    }

    function initFavorite() {
        $(".favorite-btn").on("click", function () {
            const $button = $(this);
            const productId = $button.data("product-id");
            $.ajax({
                url: "/favorites/" + productId + "/toggle",
                method: "POST",
                success: function (data) {
                    $button.toggleClass("active", data.favorited);
                    $button.find("i")
                        .toggleClass("bi-heart-fill", data.favorited)
                        .toggleClass("bi-heart", !data.favorited);
                    $button.find(".favorite-count").text(data.count);
                },
                error: function () {
                    window.location.href = "/login?redirect=/products/" + productId;
                }
            });
        });
    }

    function initAiPublishAssistant() {
        $("#aiPolishBtn").on("click", function () {
            const $button = $(this);
            const payload = {
                title: $("#title").val(),
                category: $("#category").val(),
                price: $("#price").val() ? Number($("#price").val()) : null,
                conditionLabel: $("#conditionLabel").val(),
                description: $("#description").val(),
                campusArea: $("#campusArea").val(),
                tradePlace: $("#tradePlace").val()
            };

            $("#aiStatus").text("AI 正在分析同类商品、标题质量和交易风险...");
            $button.prop("disabled", true).addClass("is-loading");
            $.ajax({
                url: "/ai/product-assistant",
                method: "POST",
                contentType: "application/json",
                data: JSON.stringify(payload),
                success: function (data) {
                    latestAiSuggestion = data;
                    renderAiSuggestion(data);
                    $("#aiApplyBtn").prop("disabled", false);
                    $("#aiStatus").text("已生成建议，可以一键应用到表单。");
                },
                error: function () {
                    $("#aiStatus").text("AI 助手暂时不可用，请确认已经登录。");
                },
                complete: function () {
                    $button.prop("disabled", false).removeClass("is-loading");
                }
            });
        });

        $("#aiApplyBtn").on("click", function () {
            if (!latestAiSuggestion) {
                return;
            }
            $("#title").val(latestAiSuggestion.optimizedTitle || $("#title").val());
            $("#description").val(latestAiSuggestion.polishedDescription || $("#description").val());
            if (latestAiSuggestion.suggestedPrice) {
                $("#price").val(latestAiSuggestion.suggestedPrice);
            }
            $("#aiStatus").text("AI 建议已应用，发布前再检查图片和面交地点。");
        });
    }

    function initAiSearch() {
        $("#aiSearchBtn").on("click", runAiSearch);
        $("#aiSearchInput").on("keydown", function (event) {
            if (event.key === "Enter") {
                event.preventDefault();
                runAiSearch();
            }
        });
    }

    function runAiSearch() {
        const query = ($("#aiSearchInput").val() || "").trim();
        const $button = $("#aiSearchBtn");
        const $result = $("#aiSearchResult");
        $button.prop("disabled", true).addClass("is-loading");
        $result.html("<span>AI 正在读取你的需求并匹配校园好物...</span>").addClass("active");
        $.ajax({
            url: "/ai/search",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify({query: query}),
            success: function (data) {
                const chips = (data.chips || []).map(function (chip) {
                    return "<b>" + escapeHtml(chip) + "</b>";
                }).join("");
                $result.html("<span>" + escapeHtml(data.summary || "AI 已完成匹配。") + "</span><div>" + chips + "</div>");
                const params = new URLSearchParams();
                if (data.keyword) {
                    params.set("keyword", data.keyword);
                }
                if (data.category) {
                    params.set("category", data.category);
                }
                params.set("sort", data.sort || "hot");
                window.setTimeout(function () {
                    window.location.href = "/products?" + params.toString();
                }, 900);
            },
            error: function () {
                $result.html("<span>AI 搜索暂时不可用，可以先使用普通搜索。</span>");
            },
            complete: function () {
                $button.prop("disabled", false).removeClass("is-loading");
            }
        });
    }

    function initLoginExperience() {
        const $page = $(".login-cinematic-page");
        if (!$page.length) {
            return;
        }
        window.setTimeout(function () {
            $page.addClass("login-ready");
        }, 80);

        const savedAccount = localStorage.getItem("campus_market_account");
        if (savedAccount) {
            $("#loginAccount").val(savedAccount);
            $("#rememberAccount").prop("checked", true);
        }

        $(".password-toggle").on("click", function () {
            const $input = $("#loginPassword");
            const showing = $input.attr("type") === "text";
            $input.attr("type", showing ? "password" : "text");
            $(this).find("i").toggleClass("bi-eye", showing).toggleClass("bi-eye-slash", !showing);
        });

        $(".demo-fill").on("click", function () {
            $("#loginAccount").val($(this).data("account"));
            $("#loginPassword").val($(this).data("password"));
            $(".refined-login-card").addClass("card-confirm");
            window.setTimeout(function () {
                $(".refined-login-card").removeClass("card-confirm");
            }, 480);
        });

        $("#loginForm").on("submit", function () {
            if ($("#rememberAccount").prop("checked")) {
                localStorage.setItem("campus_market_account", $("#loginAccount").val());
            } else {
                localStorage.removeItem("campus_market_account");
            }
            $page.addClass("login-submitting");
            $(this).find(".login-submit").prop("disabled", true).addClass("is-loading");
        });
    }

    function initToasts() {
        if (!window.bootstrap) {
            return;
        }
        document.querySelectorAll(".toast").forEach(function (toastEl) {
            bootstrap.Toast.getOrCreateInstance(toastEl).show();
        });
    }

    function initLazyMedia() {
        $(".lazy-img").each(function () {
            const image = this;
            const markLoaded = function () {
                image.classList.add("is-loaded");
                $(image).closest(".skeleton-media").addClass("image-loaded");
            };
            if (image.complete && image.naturalWidth > 0) {
                markLoaded();
            } else {
                $(image).one("load error", markLoaded);
            }
        });
    }

    function initAiCustomerService() {
        if ($("#aiCustomerWidget").length) {
            return;
        }
        $("body").append(`
            <div id="aiCustomerWidget" class="ai-customer-widget" aria-live="polite">
                <button class="ai-customer-fab" type="button" aria-label="打开 AI 客服">
                    <i class="bi bi-robot"></i>
                    <span>AI</span>
                </button>
                <section class="ai-customer-panel" aria-label="码上启航 AI 客服">
                    <div class="ai-customer-head">
                        <div>
                            <span>AI SERVICE</span>
                            <strong>码上启航客服</strong>
                        </div>
                        <div class="ai-customer-tools">
                            <button type="button" class="ai-customer-clear" aria-label="清空对话"><i class="bi bi-trash3"></i></button>
                            <button type="button" class="ai-customer-max" aria-label="大屏模式"><i class="bi bi-arrows-fullscreen"></i></button>
                            <button type="button" class="ai-customer-close" aria-label="关闭"><i class="bi bi-x-lg"></i></button>
                        </div>
                    </div>
                    <div class="ai-customer-context">
                        <span class="ai-customer-status">在线 · 本地智能客服</span>
                        <span>支持发布、搜索、订单、私信、后台与交易安全</span>
                    </div>
                    <div class="ai-customer-stream"></div>
                    <div class="ai-customer-quick"></div>
                    <form class="ai-customer-form">
                        <input placeholder="输入你的问题，Shift+Enter 换行" autocomplete="off">
                        <button type="submit" aria-label="发送"><i class="bi bi-send"></i></button>
                    </form>
                </section>
            </div>
        `);

        const $widget = $("#aiCustomerWidget");
        const widget = $widget[0];
        const $fab = $widget.find(".ai-customer-fab");
        const $panel = $widget.find(".ai-customer-panel");
        const panel = $panel[0];
        const $stream = $widget.find(".ai-customer-stream");
        const $input = $widget.find("input");
        const $sendButton = $widget.find(".ai-customer-form button");
        const $status = $widget.find(".ai-customer-status");
        const positionKey = "qihang.ai.customer.position";
        const historyKey = "qihang.ai.customer.history";
        const sessionKey = "qihang.ai.customer.session";
        const defaultQuickReplies = ["怎么发布商品？", "帮我找便宜耳麦", "后台怎么打开？", "交易安全吗？"];
        const sessionId = getSessionId();
        let conversation = readJson(historyKey, []);
        let hasCustomPosition = false;
        let dragState = null;
        let fabDragState = null;
        let ignoreFabClickUntil = 0;

        applyInitialWidgetPosition();
        renderConversation();
        updateQuickReplies(defaultQuickReplies);

        $fab.on("click", function (event) {
            if (Date.now() < ignoreFabClickUntil) {
                event.preventDefault();
                return;
            }
            $widget.toggleClass("open");
            if ($widget.hasClass("open")) {
                ensurePanelPosition();
                $input.trigger("focus");
            }
        });

        $fab.on("pointerdown", function (event) {
            const pointer = event.originalEvent;
            if (pointer.pointerType === "mouse" && pointer.button !== 0) {
                return;
            }
            event.preventDefault();
            const rect = widget.getBoundingClientRect();
            fabDragState = {
                pointerId: pointer.pointerId,
                startX: pointer.clientX,
                startY: pointer.clientY,
                offsetX: pointer.clientX - rect.left,
                offsetY: pointer.clientY - rect.top,
                moved: false
            };
            try {
                this.setPointerCapture(fabDragState.pointerId);
            } catch (ignore) {
                // Some embedded browsers do not expose pointer capture on synthetic events.
            }
            $widget.addClass("fab-dragging");
            document.addEventListener("pointermove", handleFabDrag, {passive: false});
            document.addEventListener("pointerup", endFabDrag, {passive: true});
            document.addEventListener("pointercancel", endFabDrag, {passive: true});
        });

        function handleFabDrag(event) {
            if (!fabDragState) {
                return;
            }
            if (event.pointerId !== fabDragState.pointerId) {
                return;
            }
            event.preventDefault();
            const distance = Math.abs(event.clientX - fabDragState.startX) + Math.abs(event.clientY - fabDragState.startY);
            if (distance > 4) {
                fabDragState.moved = true;
            }
            const width = $fab.outerWidth();
            const height = $fab.outerHeight();
            const nextLeft = Math.min(window.innerWidth - width - 8, Math.max(8, event.clientX - fabDragState.offsetX));
            const nextTop = Math.min(Math.max(8, window.innerHeight - height - 88), Math.max(8, event.clientY - fabDragState.offsetY));
            setWidgetPosition(nextLeft, nextTop);
            if ($widget.hasClass("open") && !$widget.hasClass("expanded")) {
                alignPanelToWidget();
            }
        }

        function endFabDrag(event) {
            if (!fabDragState) {
                return;
            }
            if (!event.pointerId || event.pointerId === fabDragState.pointerId) {
                if (fabDragState.moved) {
                    ignoreFabClickUntil = Date.now() + 260;
                    saveWidgetPosition();
                }
                fabDragState = null;
                $widget.removeClass("fab-dragging");
                document.removeEventListener("pointermove", handleFabDrag);
                document.removeEventListener("pointerup", endFabDrag);
                document.removeEventListener("pointercancel", endFabDrag);
            }
        }

        $widget.find(".ai-customer-close").on("click", function () {
            $widget.removeClass("open");
        });

        $widget.find(".ai-customer-max").on("click", function () {
            const expanded = !$widget.hasClass("expanded");
            $widget.toggleClass("expanded", expanded);
            if (expanded) {
                centerPanel(true);
                $(this).find("i").removeClass("bi-arrows-fullscreen").addClass("bi-fullscreen-exit");
            } else {
                resetPanelPosition();
                $(this).find("i").removeClass("bi-fullscreen-exit").addClass("bi-arrows-fullscreen");
            }
        });

        $widget.find(".ai-customer-clear").on("click", function () {
            conversation = [];
            saveConversation();
            renderConversation();
            updateQuickReplies(defaultQuickReplies);
        });

        $widget.find(".ai-customer-head").on("pointerdown", function (event) {
            if ($(event.target).closest("button").length) {
                return;
            }
            event.preventDefault();
            ensurePanelPosition();
            const rect = panel.getBoundingClientRect();
            dragState = {
                pointerId: event.originalEvent.pointerId,
                offsetX: event.clientX - rect.left,
                offsetY: event.clientY - rect.top
            };
            try {
                panel.setPointerCapture(dragState.pointerId);
            } catch (ignore) {
                // Pointer capture is optional; document-level movement still keeps the panel usable.
            }
            $widget.addClass("dragging");
        });

        $panel.on("pointermove", function (event) {
            if (!dragState) {
                return;
            }
            const width = panel.offsetWidth;
            const height = panel.offsetHeight;
            const nextLeft = Math.min(window.innerWidth - width - 8, Math.max(8, event.clientX - dragState.offsetX));
            const nextTop = Math.min(window.innerHeight - height - 8, Math.max(8, event.clientY - dragState.offsetY));
            setPanelPosition(nextLeft, nextTop);
        });

        $panel.on("pointerup pointercancel", function () {
            if (!dragState) {
                return;
            }
            try {
                panel.releasePointerCapture(dragState.pointerId);
            } catch (ignore) {
                // Nothing to release in browsers without capture support.
            }
            dragState = null;
            $widget.removeClass("dragging");
        });

        $(window).on("resize", function () {
            keepWidgetInViewport();
            if ($widget.hasClass("expanded")) {
                centerPanel(true);
            } else if (hasCustomPosition) {
                keepPanelInViewport();
            }
        });

        $widget.on("click", ".ai-customer-quick button", function () {
            sendCustomerMessage($(this).text());
        });

        $widget.find(".ai-customer-form").on("submit", function (event) {
            event.preventDefault();
            const message = ($input.val() || "").trim();
            if (!message) {
                return;
            }
            $input.val("");
            sendCustomerMessage(message);
        });

        function renderConversation() {
            $stream.empty();
            if (!conversation.length) {
                appendBubble("assistant", "你好，我是码上启航 AI 客服。你可以问我如何发布、预订、联系卖家、进入后台，也可以直接说想找什么商品。", {persist: false, source: "local"});
                return;
            }
            conversation.forEach(function (item) {
                appendBubble(item.role, item.content, {
                    persist: false,
                    actionLabel: item.actionLabel,
                    actionUrl: item.actionUrl,
                    source: item.source
                });
            });
        }

        function appendBubble(role, content, options) {
            const config = $.extend({persist: true}, options || {});
            const type = role === "user" || role === "mine" ? "mine" : "bot";
            const action = config.actionUrl
                ? "<a class=\"ai-customer-action\" href=\"" + escapeHtml(config.actionUrl) + "\">" + escapeHtml(config.actionLabel || "立即查看") + "</a>"
                : "";
            const source = config.source && type === "bot"
                ? "<small class=\"ai-customer-source\">" + (config.source === "external" ? "大模型接入" : "本地智能规则") + "</small>"
                : "";
            $stream.append(
                "<div class=\"ai-customer-bubble " + type + "\">" +
                "<span class=\"ai-customer-text\">" + escapeHtml(content) + "</span>" +
                action +
                source +
                "</div>"
            );
            $stream.scrollTop($stream.prop("scrollHeight"));
            if (config.persist) {
                conversation.push({
                    role: type === "mine" ? "user" : "assistant",
                    content: content,
                    actionLabel: config.actionLabel || "",
                    actionUrl: config.actionUrl || "",
                    source: config.source || ""
                });
                conversation = conversation.slice(-18);
                saveConversation();
            }
        }

        function ensurePanelPosition() {
            if (!hasCustomPosition || !$panel.attr("style")) {
                alignPanelToWidget();
            }
        }

        function resetPanelPosition() {
            hasCustomPosition = true;
            const width = $widget.hasClass("expanded") ? Math.min(860, window.innerWidth - 32) : Math.min(380, window.innerWidth - 32);
            const height = $widget.hasClass("expanded") ? Math.min(720, window.innerHeight - 32) : Math.min(620, window.innerHeight - 116);
            const left = window.innerWidth - width - 24;
            const top = window.innerHeight - height - 96;
            setPanelPosition(Math.max(8, left), Math.max(8, top));
        }

        function alignPanelToWidget() {
            hasCustomPosition = true;
            const widgetRect = widget.getBoundingClientRect();
            const width = Math.min(380, window.innerWidth - 32);
            const height = Math.min(620, window.innerHeight - 116);
            const left = Math.min(window.innerWidth - width - 8, Math.max(8, widgetRect.left + widgetRect.width / 2 - width / 2));
            const preferredTop = widgetRect.top > height + 24 ? widgetRect.top - height - 12 : widgetRect.bottom + 12;
            const top = Math.min(window.innerHeight - height - 8, Math.max(8, preferredTop));
            setPanelPosition(left, top);
        }

        function centerPanel(expanded) {
            hasCustomPosition = true;
            window.setTimeout(function () {
                const width = expanded ? Math.min(860, window.innerWidth - 32) : panel.offsetWidth;
                const height = expanded ? Math.min(720, window.innerHeight - 32) : panel.offsetHeight;
                setPanelPosition(Math.max(8, (window.innerWidth - width) / 2), Math.max(8, (window.innerHeight - height) / 2));
            }, 0);
        }

        function setPanelPosition(left, top) {
            panel.style.left = left + "px";
            panel.style.top = top + "px";
            panel.style.right = "auto";
            panel.style.bottom = "auto";
        }

        function keepPanelInViewport() {
            const rect = panel.getBoundingClientRect();
            const nextLeft = Math.min(window.innerWidth - rect.width - 8, Math.max(8, rect.left));
            const nextTop = Math.min(window.innerHeight - rect.height - 8, Math.max(8, rect.top));
            setPanelPosition(nextLeft, nextTop);
        }

        function setWidgetPosition(left, top) {
            $widget.css({left: left + "px", top: top + "px", right: "auto", bottom: "auto"});
        }

        function keepWidgetInViewport() {
            const rect = widget.getBoundingClientRect();
            if (!rect.width || !rect.height) {
                return;
            }
            const nextLeft = Math.min(window.innerWidth - rect.width - 8, Math.max(8, rect.left));
            const nextTop = Math.min(Math.max(8, window.innerHeight - rect.height - 88), Math.max(8, rect.top));
            setWidgetPosition(nextLeft, nextTop);
            saveWidgetPosition();
        }

        function sendCustomerMessage(message) {
            if ($widget.data("sending")) {
                return;
            }
            appendBubble("user", message);
            setSending(true);
            const $typing = $("<div class=\"ai-customer-bubble bot typing\"><span class=\"typing-dots\"><i></i><i></i><i></i></span><span>AI 正在分析你的问题...</span></div>");
            $stream.append($typing);
            $stream.scrollTop($stream.prop("scrollHeight"));
            $.ajax({
                url: "/ai/customer-service",
                method: "POST",
                contentType: "application/json",
                data: JSON.stringify({
                    message: message,
                    sessionId: sessionId,
                    pageUrl: window.location.pathname + window.location.search + window.location.hash,
                    history: conversation.slice(0, -1).slice(-10)
                }),
                success: function (data) {
                    $typing.remove();
                    appendBubble("assistant", data.answer || "我已经收到你的问题。", {
                        actionLabel: data.actionLabel,
                        actionUrl: data.actionUrl,
                        source: data.source || "local"
                    });
                    updateQuickReplies(data.quickReplies && data.quickReplies.length ? data.quickReplies : defaultQuickReplies);
                    $widget.data("lastSource", data.source || "local");
                    $status.text(data.source === "external" ? "在线 · 大模型客服" : "在线 · 本地智能客服");
                    $stream.scrollTop($stream.prop("scrollHeight"));
                },
                error: function () {
                    $typing.remove();
                    appendBubble("assistant", "AI 客服暂时不可用，可以先使用导航进入商品广场、发布页面或后台。", {source: "local"});
                },
                complete: function () {
                    setSending(false);
                }
            });
        }

        function updateQuickReplies(items) {
            $widget.find(".ai-customer-quick").html(items.map(function (item) {
                return "<button type=\"button\">" + escapeHtml(item) + "</button>";
            }).join(""));
        }

        function setSending(sending) {
            $widget.data("sending", sending);
            $input.prop("disabled", sending);
            $sendButton.prop("disabled", sending);
            if (sending) {
                $status.text("思考中 · 正在生成回复");
            } else {
                $status.text($widget.data("lastSource") === "external" ? "在线 · 大模型客服" : "在线 · 本地智能客服");
            }
        }

        function applyInitialWidgetPosition() {
            const stored = readJson(positionKey, null);
            const size = 58;
            const fallback = {
                left: window.innerWidth - size - 24,
                top: Math.min(Math.max(8, window.innerHeight - size - 88), Math.max(92, window.innerHeight * 0.16))
            };
            const storedTooLow = stored && Number.isFinite(stored.top) && stored.top > window.innerHeight * 0.62;
            const left = stored && Number.isFinite(stored.left) && !storedTooLow ? stored.left : fallback.left;
            const top = stored && Number.isFinite(stored.top) && !storedTooLow ? stored.top : fallback.top;
            setWidgetPosition(
                Math.min(window.innerWidth - size - 8, Math.max(8, left)),
                Math.min(Math.max(8, window.innerHeight - size - 88), Math.max(8, top))
            );
            if (storedTooLow) {
                saveWidgetPosition();
            }
        }

        function saveWidgetPosition() {
            const rect = widget.getBoundingClientRect();
            writeJson(positionKey, {left: rect.left, top: rect.top});
        }

        function saveConversation() {
            writeJson(historyKey, conversation);
        }

        function getSessionId() {
            try {
                let value = window.localStorage.getItem(sessionKey);
                if (!value) {
                    value = "cs-" + Date.now() + "-" + Math.random().toString(16).slice(2);
                    window.localStorage.setItem(sessionKey, value);
                }
                return value;
            } catch (ignore) {
                return "cs-" + Date.now();
            }
        }

        function readJson(key, fallback) {
            try {
                const value = window.localStorage.getItem(key);
                return value ? JSON.parse(value) : fallback;
            } catch (ignore) {
                return fallback;
            }
        }

        function writeJson(key, value) {
            try {
                window.localStorage.setItem(key, JSON.stringify(value));
            } catch (ignore) {
                // Storage can be unavailable in private or embedded modes.
            }
        }
    }

    function initAdminProductAjax() {
        const selector = "#adminProductManagement";
        if (!$(selector).length) {
            return;
        }

        $(document).on("submit", "[data-admin-products-filter]", function (event) {
            event.preventDefault();
            loadAdminProducts($(this).attr("action") + "?" + $(this).serialize());
        });

        $(document).on("change", "[data-admin-products-filter] select", function () {
            $(this).closest("form").trigger("submit");
        });

        $(document).on("click", "#adminProductManagement .pager a", function (event) {
            const href = $(this).attr("href");
            if (!href || $(this).hasClass("disabled")) {
                event.preventDefault();
                return;
            }
            event.preventDefault();
            loadAdminProducts(href);
        });

        function loadAdminProducts(url) {
            const $target = $(selector);
            $target.addClass("is-refreshing");
            $.ajax({
                url: url,
                method: "GET",
                headers: {"X-Requested-With": "XMLHttpRequest"},
                success: function (html) {
                    const $newContent = $(html);
                    $target.replaceWith($newContent);
                    window.history.replaceState(null, "", url);
                },
                error: function () {
                    $target.removeClass("is-refreshing");
                    alert("商品管理模块刷新失败，请稍后再试。");
                }
            });
        }
    }

    function renderAiSuggestion(data) {
        $("#aiScore").text(data.qualityScore || "--");
        $("#aiPrice").text(data.suggestedPrice ? "￥" + Number(data.suggestedPrice).toFixed(2) : "等待分析");
        $("#aiRange").text(data.minPrice && data.maxPrice ? "建议区间 ￥" + Number(data.minPrice).toFixed(2) + " - ￥" + Number(data.maxPrice).toFixed(2) : "区间不足");
        $("#aiTitle").text(data.optimizedTitle || "暂未生成标题建议");
        $("#aiDescription").text(data.polishedDescription || "暂未生成描述建议");
        $("#aiTags").html((data.tags || []).map(function (tag) {
            return "<span>" + escapeHtml(tag) + "</span>";
        }).join(""));
        $("#aiAlerts").toggleClass("visible", (data.riskAlerts || []).length > 0)
            .html((data.riskAlerts || []).map(function (alert) {
                return "<div><i class=\"bi bi-exclamation-triangle\"></i> " + escapeHtml(alert) + "</div>";
            }).join(""));
        $("#aiPanel").addClass("is-awake");
    }

    function initCharts() {
        if (!(window.echarts && document.getElementById("categoryChart"))) {
            return;
        }
        const categoryChart = echarts.init(document.getElementById("categoryChart"));
        const orderChart = echarts.init(document.getElementById("orderChart"));
        const productStatusChart = document.getElementById("productStatusChart") ? echarts.init(document.getElementById("productStatusChart")) : null;
        const campusChart = document.getElementById("campusChart") ? echarts.init(document.getElementById("campusChart")) : null;
        const categoryStats = window.__categoryStats || [];
        const orderStats = window.__orderStats || [];
        const productStatusStats = window.__productStatusStats || [];
        const campusStats = window.__campusStats || [];

        categoryChart.setOption({
            color: ["#00c2ff", "#ffd166", "#ff5c7a", "#222a5f", "#ff6bd6", "#39f5c8"],
            tooltip: {trigger: "item"},
            series: [{
                type: "pie",
                radius: ["42%", "72%"],
                data: categoryStats.map(item => ({name: item.label, value: item.value})),
                label: {formatter: "{b}\n{d}%"}
            }]
        });

        orderChart.setOption({
            color: ["#00c2ff"],
            tooltip: {trigger: "axis"},
            grid: {top: 24, right: 16, bottom: 28, left: 36},
            xAxis: {type: "category", data: orderStats.map(item => item.label)},
            yAxis: {type: "value", minInterval: 1},
            series: [{
                type: "bar",
                data: orderStats.map(item => item.value),
                barWidth: 28,
                itemStyle: {borderRadius: [6, 6, 0, 0]}
            }]
        });

        if (productStatusChart) {
            productStatusChart.setOption({
                color: ["#cb8dff", "#39f5c8", "#ffd166", "#8c95a7", "#ff5c7a"],
                tooltip: {trigger: "item"},
                series: [{
                    type: "pie",
                    radius: ["46%", "74%"],
                    data: productStatusStats.map(item => ({name: item.label, value: item.value})),
                    label: {formatter: "{b}\n{d}%"}
                }]
            });
        }

        if (campusChart) {
            campusChart.setOption({
                color: ["#cb8dff"],
                tooltip: {trigger: "axis"},
                grid: {top: 24, right: 16, bottom: 34, left: 36},
                xAxis: {type: "category", data: campusStats.map(item => item.label), axisLabel: {color: "rgba(255,255,255,.7)"}},
                yAxis: {type: "value", minInterval: 1, axisLabel: {color: "rgba(255,255,255,.7)"}},
                series: [{
                    type: "bar",
                    data: campusStats.map(item => item.value),
                    barWidth: 28,
                    itemStyle: {borderRadius: [6, 6, 0, 0]}
                }]
            });
        }

        $(window).on("resize", function () {
            categoryChart.resize();
            orderChart.resize();
            if (productStatusChart) {
                productStatusChart.resize();
            }
            if (campusChart) {
                campusChart.resize();
            }
        });
    }

    function escapeHtml(value) {
        return String(value || "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
});
