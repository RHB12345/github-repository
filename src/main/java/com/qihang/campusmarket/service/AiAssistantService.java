package com.qihang.campusmarket.service;

import com.qihang.campusmarket.dto.AiCustomerRequest;
import com.qihang.campusmarket.dto.AiCustomerResponse;
import com.qihang.campusmarket.dto.AiCustomerMessage;
import com.qihang.campusmarket.dto.AiProductInsight;
import com.qihang.campusmarket.dto.AiProductRequest;
import com.qihang.campusmarket.dto.AiProductSuggestion;
import com.qihang.campusmarket.dto.AiSearchRequest;
import com.qihang.campusmarket.dto.AiSearchResult;
import com.qihang.campusmarket.entity.Product;
import com.qihang.campusmarket.entity.User;
import com.qihang.campusmarket.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class AiAssistantService {
    private final ProductMapper productMapper;
    private final RestClient.Builder restClientBuilder;

    @Value("${campus.ai.customer.api-url:}")
    private String customerApiUrl;

    @Value("${campus.ai.customer.api-key:}")
    private String customerApiKey;

    @Value("${campus.ai.customer.model:deepseek-v4-flash}")
    private String customerModel;

    public AiAssistantService(ProductMapper productMapper, RestClient.Builder restClientBuilder) {
        this.productMapper = productMapper;
        this.restClientBuilder = restClientBuilder;
    }

    public AiProductSuggestion suggest(AiProductRequest request) {
        String category = clean(request.getCategory());
        String condition = clean(request.getConditionLabel());
        String title = clean(request.getTitle());
        String description = clean(request.getDescription());

        BigDecimal marketBase = marketBase(category);
        BigDecimal suggested = request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) > 0
                ? request.getPrice()
                : marketBase.multiply(BigDecimal.valueOf(conditionFactor(condition)));
        suggested = money(suggested);

        AiProductSuggestion suggestion = new AiProductSuggestion();
        suggestion.setSuggestedPrice(suggested);
        suggestion.setMinPrice(money(suggested.multiply(BigDecimal.valueOf(0.86))));
        suggestion.setMaxPrice(money(suggested.multiply(BigDecimal.valueOf(1.12))));
        suggestion.setOptimizedTitle(buildTitle(title, category, condition));
        suggestion.setPolishedDescription(buildDescription(title, category, condition, description, request));
        suggestion.setConditionAdvice(conditionAdvice(condition));
        suggestion.setSellingPoints(sellingPoints(category));
        suggestion.setRiskAlerts(riskAlerts(title, description, request.getPrice(), suggested));
        suggestion.setTags(tags(category, condition, description));
        suggestion.setQualityScore(qualityScore(request, suggestion.getRiskAlerts()));
        return suggestion;
    }

    public AiSearchResult searchIntent(AiSearchRequest request) {
        String query = clean(request.getQuery());
        String normalized = query == null ? "" : query.toLowerCase(Locale.ROOT);

        AiSearchResult result = new AiSearchResult();
        result.setCategory(inferCategory(normalized));
        result.setSort(inferSort(normalized));
        result.setKeyword(extractKeyword(query, result.getCategory()));
        result.setSummary(buildSearchSummary(query, result.getCategory(), result.getSort()));
        result.setChips(buildSearchChips(result.getCategory(), result.getSort()));
        return result;
    }

    public AiCustomerResponse customerService(AiCustomerRequest request) {
        String message = clean(request == null ? null : request.getMessage());
        String text = message == null ? "" : message.toLowerCase(Locale.ROOT);

        AiCustomerResponse external = askExternalCustomerApi(request, message);
        if (external != null) {
            enrichCustomerAction(external, text, message);
            return external;
        }

        AiCustomerResponse response = new AiCustomerResponse();
        response.setQuickReplies(List.of("怎么发布商品？", "帮我找便宜耳麦", "后台怎么打开？", "交易安全吗？"));

        if (!StringUtils.hasText(message)) {
            response.setAnswer("你好，我是码上启航 AI 客服。你可以问我如何发布闲置、如何预订商品、怎么联系卖家，或者直接说出想找的商品。");
            response.setActionLabel("浏览商品");
            response.setActionUrl("/products");
            response.setIntent("welcome");
            return response;
        }
        if (containsAny(text, "发布", "上架", "卖", "出售", "转让", "图片", "估价", "文案")) {
            response.setAnswer("发布商品可以按 4 步完成：上传真实图片，填写标题和分类，写清成色/瑕疵/配件，选择校内面交地点。发布页内置 AI 文案与估价助手，会根据同类商品和成色给出标题、描述、价格区间和风险提示。");
            response.setActionLabel("去发布商品");
            response.setActionUrl("/products/new");
            response.setIntent("publish");
            response.setQuickReplies(List.of("标题怎么写更容易卖？", "如何给商品估价？", "需要上传几张图？", "发布后怎么下架？"));
            return response;
        }
        if (containsAny(text, "预订", "购买", "下单", "买", "想要", "订单", "确认", "取消")) {
            response.setAnswer("买家进入商品详情后点击“立即预订”会生成订单，并联动商品状态。建议先私信确认配件、瑕疵、面交时间和地点；卖家确认后，双方在校内公共区域验货，买家再确认完成交易。订单不合适时可以取消。");
            response.setActionLabel("进入商品广场");
            response.setActionUrl("/products");
            response.setIntent("order");
            response.setQuickReplies(List.of("怎么确认收货？", "订单可以取消吗？", "卖家多久确认？", "如何联系卖家？"));
            return response;
        }
        if (containsAny(text, "联系", "私信", "聊天", "消息", "卖家")) {
            response.setAnswer("在商品详情页的卖家信息区域点击“私信”即可进入站内对话。聊天记录会保存在消息中心，适合记录面交地点、时间、配件清单和验货结果。");
            response.setActionLabel("查看消息");
            response.setActionUrl("/messages");
            response.setIntent("message");
            response.setQuickReplies(List.of("私信入口在哪？", "消息记录会保存吗？", "怎么和卖家约面交？", "交易安全吗？"));
            return response;
        }
        if (containsAny(text, "后台", "管理", "admin", "统计", "管理员", "审核")) {
            response.setAnswer("后台入口在导航栏的“后台”，管理员账号登录后可查看用户、商品、订单、消息和可视化统计。演示管理员账号是 2023000001，密码 123456。商品管理支持筛选、排序、分页并局部刷新。");
            response.setActionLabel("打开后台");
            response.setActionUrl("/admin");
            response.setIntent("admin");
            response.setQuickReplies(List.of("管理员账号是什么？", "商品管理怎么筛选？", "后台统计有哪些？", "用户怎么管理？"));
            return response;
        }
        if (containsAny(text, "安全", "被骗", "风险", "验货", "面交", "定金", "先款")) {
            response.setAnswer("安全建议：优先校内公共区域面交，不提前转定金或走外部链接；数码类当场测试充电、屏幕、按键和蓝牙；教材资料确认版本年份；确认无误后再完成订单。平台也有学号认证、Session 鉴权、接口限流和站内消息留痕。");
            response.setActionLabel("查看交易流程");
            response.setActionUrl("/#process");
            response.setIntent("safety");
            response.setQuickReplies(List.of("数码商品怎么验货？", "可以先付定金吗？", "面交地点怎么选？", "遇到风险怎么办？"));
            return response;
        }
        if (containsAny(text, "登录", "注册", "账号", "密码", "学号", "认证")) {
            response.setAnswer("新用户需要使用手机号/邮箱、学号和基础信息完成注册；登录后才能发布、预订、收藏、私信和查看个人中心。如果忘记密码，当前演示版建议联系管理员重置，后续可接入短信或邮箱验证码。");
            response.setActionLabel("去登录");
            response.setActionUrl("/login");
            response.setIntent("account");
            response.setQuickReplies(List.of("怎么注册账号？", "为什么要学号认证？", "忘记密码怎么办？", "个人中心在哪？"));
            return response;
        }
        if (containsAny(text, "收藏", "喜欢", "心愿", "收藏夹")) {
            response.setAnswer("在商品详情页点击收藏后，会保存到个人中心的收藏区域。适合先标记感兴趣的教材、耳机、台灯或自行车，再统一联系卖家比较价格和成色。");
            response.setActionLabel("查看个人中心");
            response.setActionUrl("/user/profile");
            response.setIntent("favorite");
            response.setQuickReplies(List.of("收藏在哪里看？", "怎么取消收藏？", "推荐便宜商品", "怎么联系卖家？"));
            return response;
        }

        AiSearchRequest searchRequest = new AiSearchRequest();
        searchRequest.setQuery(message);
        AiSearchResult result = searchIntent(searchRequest);
        String keyword = StringUtils.hasText(result.getKeyword()) ? result.getKeyword() : message;
        StringBuilder url = new StringBuilder("/products?keyword=").append(encode(keyword));
        if (StringUtils.hasText(result.getCategory())) {
            url.append("&category=").append(encode(result.getCategory()));
        }
        if (StringUtils.hasText(result.getSort())) {
            url.append("&sort=").append(result.getSort());
        }
        response.setAnswer(result.getSummary() + " 我已经帮你整理好了搜索条件，可以直接跳转查看匹配商品。");
        response.setActionLabel("查看推荐商品");
        response.setActionUrl(url.toString());
        response.setIntent("search");
        response.setQuickReplies(List.of("按低价排序", "只看数码电子", "帮我找教材资料", "我要发布商品"));
        return response;
    }

    private AiCustomerResponse askExternalCustomerApi(AiCustomerRequest request, String message) {
        if (!StringUtils.hasText(customerApiUrl) || !StringUtils.hasText(customerApiKey) || !StringUtils.hasText(message)) {
            return null;
        }
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content", buildCustomerSystemPrompt(request)
            ));
            if (request != null && request.getHistory() != null) {
                request.getHistory().stream()
                        .filter(item -> item != null && StringUtils.hasText(item.getContent()))
                        .skip(Math.max(0, request.getHistory().size() - 8))
                        .forEach(item -> messages.add(Map.of(
                                "role", normalizeChatRole(item),
                                "content", item.getContent()
                        )));
            }
            messages.add(Map.of("role", "user", "content", message));

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", StringUtils.hasText(customerModel) ? customerModel : "deepseek-v4-flash");
            payload.put("messages", messages);
            payload.put("temperature", 0.35);
            payload.put("stream", false);

            Map<?, ?> body = restClientBuilder.build()
                    .post()
                    .uri(customerApiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
            String answer = extractExternalAnswer(body);
            if (!StringUtils.hasText(answer)) {
                return null;
            }
            AiCustomerResponse response = new AiCustomerResponse();
            response.setAnswer(answer);
            response.setSource("external");
            response.setQuickReplies(List.of("继续帮我推荐", "怎么交易更安全？", "打开商品广场", "联系卖家"));
            return response;
        } catch (RestClientException ex) {
            return null;
        }
    }

    private void enrichCustomerAction(AiCustomerResponse response, String text, String message) {
        if (StringUtils.hasText(response.getActionUrl())) {
            return;
        }
        response.setQuickReplies(response.getQuickReplies().isEmpty()
                ? List.of("继续帮我推荐", "怎么交易更安全？", "打开商品广场", "联系卖家")
                : response.getQuickReplies());
        if (containsAny(text, "发布", "上架", "出售", "转让")) {
            response.setActionLabel("去发布商品");
            response.setActionUrl("/products/new");
            response.setIntent("publish");
            return;
        }
        if (containsAny(text, "后台", "管理", "admin", "统计")) {
            response.setActionLabel("打开后台");
            response.setActionUrl("/admin");
            response.setIntent("admin");
            return;
        }
        if (containsAny(text, "消息", "私信", "联系", "卖家")) {
            response.setActionLabel("查看消息");
            response.setActionUrl("/messages");
            response.setIntent("message");
            return;
        }
        if (containsAny(text, "安全", "风险", "验货", "面交")) {
            response.setActionLabel("查看交易流程");
            response.setActionUrl("/#process");
            response.setIntent("safety");
            return;
        }
        AiSearchRequest searchRequest = new AiSearchRequest();
        searchRequest.setQuery(message);
        AiSearchResult result = searchIntent(searchRequest);
        String keyword = StringUtils.hasText(result.getKeyword()) ? result.getKeyword() : message;
        StringBuilder url = new StringBuilder("/products?keyword=").append(encode(keyword));
        if (StringUtils.hasText(result.getCategory())) {
            url.append("&category=").append(encode(result.getCategory()));
        }
        if (StringUtils.hasText(result.getSort())) {
            url.append("&sort=").append(result.getSort());
        }
        response.setActionLabel("查看推荐商品");
        response.setActionUrl(url.toString());
        response.setIntent("search");
    }

    private String buildCustomerSystemPrompt(AiCustomerRequest request) {
        String page = request == null ? "" : clean(request.getPageUrl());
        return "你是“码上启航”校园二手交易平台的 AI 客服。请用简洁中文回答，优先帮助安徽信息工程学院师生完成发布闲置、搜索商品、预订订单、站内私信、交易安全、后台入口等任务。"
                + "强调学号认证、校内公共区域面交、当场验货、不提前支付定金、不使用外部可疑链接。"
                + (StringUtils.hasText(page) ? "用户当前页面：" + page + "。" : "");
    }

    private String normalizeChatRole(AiCustomerMessage item) {
        String role = item.getRole() == null ? "" : item.getRole().toLowerCase(Locale.ROOT);
        if ("assistant".equals(role) || "bot".equals(role)) {
            return "assistant";
        }
        return "user";
    }

    private String extractExternalAnswer(Map<?, ?> body) {
        if (body == null) {
            return null;
        }
        Object answer = body.get("answer");
        if (answer instanceof String text && StringUtils.hasText(text)) {
            return text;
        }
        Object content = body.get("content");
        if (content instanceof String text && StringUtils.hasText(text)) {
            return text;
        }
        Object choices = body.get("choices");
        if (choices instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> choice) {
            Object message = choice.get("message");
            if (message instanceof Map<?, ?> messageMap) {
                Object value = messageMap.get("content");
                if (value instanceof String text && StringUtils.hasText(text)) {
                    return text;
                }
            }
            Object value = choice.get("text");
            if (value instanceof String text && StringUtils.hasText(text)) {
                return text;
            }
        }
        return null;
    }

    public AiProductInsight analyze(Product product, User seller, int imageCount, int favoriteCount, int commentCount) {
        BigDecimal average = productMapper.averagePriceByCategory(product.getCategory());
        List<String> alerts = riskAlerts(product.getTitle(), product.getDescription(), product.getPrice(), average);
        List<String> tips = new ArrayList<>();
        int score = 84;

        if (!"ON_SALE".equals(product.getStatus())) {
            score -= 12;
            alerts.add("商品当前不是在售状态，交易前需要再次确认。");
        }
        if (!StringUtils.hasText(product.getDescription()) || product.getDescription().length() < 30) {
            score -= 10;
            alerts.add("描述信息偏短，建议询问配件、瑕疵和购买时间。");
        }
        if (imageCount <= 1) {
            score -= 6;
            tips.add("可以让卖家补充实拍细节图，尤其是边角、接口和磨损位置。");
        }
        if (!StringUtils.hasText(seller.getDormitory())) {
            score -= 5;
            tips.add("卖家未填写常用地点，建议优先约校内公共区域。");
        }
        if (favoriteCount >= 2 || commentCount >= 2) {
            score += 4;
        }
        if (average != null && average.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal high = average.multiply(BigDecimal.valueOf(1.35));
            BigDecimal low = average.multiply(BigDecimal.valueOf(0.55));
            if (product.getPrice().compareTo(high) > 0) {
                score -= 7;
            } else if (product.getPrice().compareTo(low) < 0) {
                score -= 5;
            }
        }

        tips.add("面交时当场检查商品状态，确认无误后再完成订单。");
        tips.add("数码类商品建议测试充电、按键、屏幕和蓝牙连接。");
        score = Math.max(35, Math.min(98, score));

        AiProductInsight insight = new AiProductInsight();
        insight.setTrustScore(score);
        insight.setLevel(score >= 82 ? "稳妥" : score >= 65 ? "需确认" : "谨慎交易");
        insight.setSummary(buildSummary(product, score, favoriteCount, commentCount));
        insight.setPriceSignal(priceSignal(product.getPrice(), average));
        insight.setAlerts(alerts);
        insight.setTips(tips);
        insight.setTags(tags(product.getCategory(), product.getConditionLabel(), product.getDescription()));
        return insight;
    }

    private BigDecimal marketBase(String category) {
        BigDecimal average = StringUtils.hasText(category) ? productMapper.averagePriceByCategory(category) : null;
        if (average != null && average.compareTo(BigDecimal.ZERO) > 0) {
            return average;
        }
        return switch (category == null ? "" : category) {
            case "教材资料" -> BigDecimal.valueOf(42);
            case "数码电子" -> BigDecimal.valueOf(120);
            case "生活用品" -> BigDecimal.valueOf(36);
            case "运动户外" -> BigDecimal.valueOf(180);
            case "美妆服饰" -> BigDecimal.valueOf(55);
            default -> BigDecimal.valueOf(50);
        };
    }

    private String inferCategory(String text) {
        if (containsAny(text, "书", "教材", "考研", "四六级", "资料", "真题", "java", "spring")) {
            return "教材资料";
        }
        if (containsAny(text, "键盘", "耳机", "耳麦", "电脑", "ipad", "平板", "手机", "蓝牙", "数码", "充电")) {
            return "数码电子";
        }
        if (containsAny(text, "台灯", "桌", "收纳", "宿舍", "椅", "床", "生活")) {
            return "生活用品";
        }
        if (containsAny(text, "车", "自行车", "球", "运动", "户外", "健身")) {
            return "运动户外";
        }
        if (containsAny(text, "衣", "鞋", "包", "美妆", "护肤", "香水")) {
            return "美妆服饰";
        }
        return null;
    }

    private String inferSort(String text) {
        if (containsAny(text, "便宜", "低价", "最省", "预算", "划算")) {
            return "priceAsc";
        }
        if (containsAny(text, "高端", "贵", "性能", "配置")) {
            return "priceDesc";
        }
        if (containsAny(text, "热门", "推荐", "大家", "收藏", "抢手")) {
            return "hot";
        }
        return "hot";
    }

    private String extractKeyword(String query, String category) {
        if (!StringUtils.hasText(query)) {
            return "";
        }
        String keyword = query;
        for (String word : List.of("帮我找", "想买", "有没有", "推荐", "便宜", "热门", "适合", "同校", "面交", "的", "一个", "一台", "一本")) {
            keyword = keyword.replace(word, " ");
        }
        if (category != null) {
            keyword = keyword.replace(category, " ");
        }
        keyword = keyword.replaceAll("\\s+", " ").trim();
        return keyword.length() > 18 ? keyword.substring(0, 18) : keyword;
    }

    private String buildSearchSummary(String query, String category, String sort) {
        String target = category == null ? "全站商品" : category;
        String order = switch (sort) {
            case "priceAsc" -> "优先展示低价好物";
            case "priceDesc" -> "优先展示高价高配商品";
            case "hot" -> "优先展示热度和收藏更高的商品";
            default -> "优先展示最新商品";
        };
        if (!StringUtils.hasText(query)) {
            return "AI 将从全站商品中为你推荐更适合校内面交的好物。";
        }
        return "AI 理解为：" + target + "，" + order + "。";
    }

    private List<String> buildSearchChips(String category, String sort) {
        List<String> chips = new ArrayList<>();
        chips.add(category == null ? "全站搜索" : category);
        chips.add(switch (sort) {
            case "priceAsc" -> "预算优先";
            case "priceDesc" -> "品质优先";
            case "hot" -> "热度优先";
            default -> "最新优先";
        });
        chips.add("校内面交");
        chips.add("AI 智能匹配");
        return chips;
    }

    private String buildTitle(String title, String category, String condition) {
        String base = StringUtils.hasText(title) ? title.replace("出售", "").replace("转让", "").trim() : category + "好物";
        if (!StringUtils.hasText(base)) {
            base = "校园面交好物";
        }
        String prefix = StringUtils.hasText(condition) ? condition + " " : "";
        String suffix = base.contains("面交") ? "" : " 校内面交";
        String optimized = prefix + base + suffix;
        return optimized.length() > 60 ? optimized.substring(0, 60) : optimized;
    }

    private String buildDescription(String title, String category, String condition, String description, AiProductRequest request) {
        List<String> lines = new ArrayList<>();
        lines.add("【商品情况】" + (StringUtils.hasText(description) ? description : "保存良好，适合同校同学继续使用。"));
        lines.add("【核心亮点】" + String.join("；", sellingPoints(category)) + "。");
        lines.add("【成色说明】" + (StringUtils.hasText(condition) ? condition : "建议补充真实成色") + "，可面交时当场检查。");
        lines.add("【交易方式】" + (StringUtils.hasText(request.getTradePlace()) ? request.getTradePlace() : "校内公共区域") + "面交，时间可私信沟通。");
        if (StringUtils.hasText(title)) {
            lines.add("【搜索关键词】" + title + "、" + category + "、校园二手。");
        }
        return String.join("\n", lines);
    }

    private List<String> sellingPoints(String category) {
        return switch (category == null ? "" : category) {
            case "教材资料" -> List.of("课程适配度高", "重点内容清晰", "适合复习备考");
            case "数码电子" -> List.of("校内可当场测试", "配件状态透明", "通勤学习都能用");
            case "生活用品" -> List.of("宿舍场景实用", "即买即用", "搬寝省心");
            case "运动户外" -> List.of("适合校园通勤", "支持现场试用", "性价比高");
            case "美妆服饰" -> List.of("状态直观", "可线下确认", "适合同校快速流转");
            default -> List.of("校内面交方便", "描述清楚", "价格可沟通");
        };
    }

    private List<String> riskAlerts(String title, String description, BigDecimal inputPrice, BigDecimal referencePrice) {
        List<String> alerts = new ArrayList<>();
        String text = ((title == null ? "" : title) + " " + (description == null ? "" : description)).toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(title) || title.length() < 6) {
            alerts.add("标题信息偏少，建议补充品牌、型号或课程名称。");
        }
        if (!StringUtils.hasText(description) || description.length() < 24) {
            alerts.add("描述信息偏短，买家可能会反复追问商品细节。");
        }
        if (containsAny(text, "先款", "定金", "链接", "外校", "快递到付", "不退不换")) {
            alerts.add("文案中出现可能影响信任的交易词，建议改成校内面交、当场验货。");
        }
        if (inputPrice != null && referencePrice != null && referencePrice.compareTo(BigDecimal.ZERO) > 0) {
            if (inputPrice.compareTo(referencePrice.multiply(BigDecimal.valueOf(1.6))) > 0) {
                alerts.add("当前价格明显高于同类均值，建议说明溢价原因。");
            }
            if (inputPrice.compareTo(referencePrice.multiply(BigDecimal.valueOf(0.35))) < 0) {
                alerts.add("当前价格明显低于同类均值，建议注明是否有瑕疵。");
            }
        }
        return alerts;
    }

    private int qualityScore(AiProductRequest request, List<String> alerts) {
        int score = 72;
        if (StringUtils.hasText(request.getTitle()) && request.getTitle().length() >= 10) {
            score += 8;
        }
        if (StringUtils.hasText(request.getDescription()) && request.getDescription().length() >= 60) {
            score += 10;
        }
        if (StringUtils.hasText(request.getCategory())) {
            score += 4;
        }
        if (StringUtils.hasText(request.getConditionLabel())) {
            score += 4;
        }
        if (StringUtils.hasText(request.getTradePlace())) {
            score += 4;
        }
        score -= alerts.size() * 6;
        return Math.max(35, Math.min(99, score));
    }

    private String conditionAdvice(String condition) {
        if (!StringUtils.hasText(condition)) {
            return "建议补充成色，并说明购买时间、使用频率和瑕疵位置。";
        }
        if (condition.contains("全新") || condition.contains("九成")) {
            return "成色较好，可以突出购买时间、保修或配件完整度。";
        }
        if (condition.contains("七成") || condition.contains("使用痕迹")) {
            return "建议主动写清磨损位置，降低买家预期差。";
        }
        return "成色表达清楚，再补充实拍细节会更容易成交。";
    }

    private String priceSignal(BigDecimal price, BigDecimal average) {
        if (average == null || average.compareTo(BigDecimal.ZERO) <= 0) {
            return "同类样本较少，建议结合成色和配件灵活议价。";
        }
        if (price.compareTo(average.multiply(BigDecimal.valueOf(1.25))) > 0) {
            return "价格高于同类均值，适合补充保修、配件或稀缺性说明。";
        }
        if (price.compareTo(average.multiply(BigDecimal.valueOf(0.7))) < 0) {
            return "价格低于同类均值，成交吸引力强，但建议确认是否有隐藏瑕疵。";
        }
        return "价格接近同类均值，议价空间相对合理。";
    }

    private String buildSummary(Product product, int score, int favoriteCount, int commentCount) {
        return "AI 综合标题、描述、价格、互动热度和卖家信息后，给出 " + score
                + " 分可信度。当前收藏 " + favoriteCount + " 次，留言 " + commentCount
                + " 条，适合先私信确认细节再面交。";
    }

    private List<String> tags(String category, String condition, String description) {
        Set<String> tags = new LinkedHashSet<>();
        if (StringUtils.hasText(category)) {
            tags.add(category);
        }
        if (StringUtils.hasText(condition)) {
            tags.add(condition);
        }
        String text = description == null ? "" : description;
        if (containsAny(text, "考研", "真题", "复习")) {
            tags.add("备考友好");
        }
        if (containsAny(text, "送", "赠", "配件")) {
            tags.add("配件明确");
        }
        if (containsAny(text, "面交", "图书馆", "宿舍")) {
            tags.add("面交方便");
        }
        if (tags.size() < 3) {
            tags.add("校内流转");
        }
        return new ArrayList<>(tags);
    }

    private double conditionFactor(String condition) {
        if (!StringUtils.hasText(condition)) {
            return 0.82;
        }
        if (condition.contains("全新")) {
            return 1.15;
        }
        if (condition.contains("九成")) {
            return 1.0;
        }
        if (condition.contains("八成")) {
            return 0.82;
        }
        if (condition.contains("七成")) {
            return 0.66;
        }
        return 0.56;
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal money(BigDecimal value) {
        return value.max(BigDecimal.valueOf(1)).setScale(2, RoundingMode.HALF_UP);
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
