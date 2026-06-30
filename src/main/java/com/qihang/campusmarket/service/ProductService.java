package com.qihang.campusmarket.service;

import com.qihang.campusmarket.dto.CommentView;
import com.qihang.campusmarket.dto.PageResult;
import com.qihang.campusmarket.dto.ProductCard;
import com.qihang.campusmarket.entity.Comment;
import com.qihang.campusmarket.entity.Product;
import com.qihang.campusmarket.entity.ProductImage;
import com.qihang.campusmarket.entity.User;
import com.qihang.campusmarket.form.ProductForm;
import com.qihang.campusmarket.mapper.CommentMapper;
import com.qihang.campusmarket.mapper.FavoriteMapper;
import com.qihang.campusmarket.mapper.ProductImageMapper;
import com.qihang.campusmarket.mapper.ProductMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProductService {
    private final ProductMapper productMapper;
    private final ProductImageMapper imageMapper;
    private final FavoriteMapper favoriteMapper;
    private final CommentMapper commentMapper;
    private final StorageService storageService;
    private final ViewDedupService viewDedupService;

    public ProductService(ProductMapper productMapper,
                          ProductImageMapper imageMapper,
                          FavoriteMapper favoriteMapper,
                          CommentMapper commentMapper,
                          StorageService storageService,
                          ViewDedupService viewDedupService) {
        this.productMapper = productMapper;
        this.imageMapper = imageMapper;
        this.favoriteMapper = favoriteMapper;
        this.commentMapper = commentMapper;
        this.storageService = storageService;
        this.viewDedupService = viewDedupService;
    }

    @Cacheable(value = "productSearch", key = "'list:' + #keyword + ':' + #category + ':' + #status + ':' + #sort + ':' + #page + ':' + #size")
    public PageResult<ProductCard> search(String keyword, String category, String status, String sort, Integer page, Integer size) {
        int safePage = Math.max(page == null ? 1 : page, 1);
        int safeSize = Math.min(Math.max(size == null ? 12 : size, 1), 36);
        String effectiveStatus = StringUtils.hasText(status) ? status : "ON_SALE";
        List<String> searchTerms = expandSearchTerms(clean(keyword));
        long total = productMapper.countSearch(searchTerms, clean(category), effectiveStatus);
        List<ProductCard> records = productMapper.search(searchTerms, clean(category), effectiveStatus, clean(sort), (safePage - 1) * safeSize, safeSize);
        return new PageResult<>(records, safePage, safeSize, total);
    }

    @Cacheable(value = "productSearch", key = "'all:' + #keyword + ':' + #category + ':' + #sort + ':' + #page + ':' + #size")
    public PageResult<ProductCard> searchAllStatuses(String keyword, String category, String sort, Integer page, Integer size) {
        int safePage = Math.max(page == null ? 1 : page, 1);
        int safeSize = Math.min(Math.max(size == null ? 12 : size, 1), 36);
        List<String> searchTerms = expandSearchTerms(clean(keyword));
        long total = productMapper.countSearch(searchTerms, clean(category), null);
        List<ProductCard> records = productMapper.search(searchTerms, clean(category), null, clean(sort), (safePage - 1) * safeSize, safeSize);
        return new PageResult<>(records, safePage, safeSize, total);
    }

    @CacheEvict(value = {"productSearch", "dashboardStats", "categoryStats", "productStatusStats", "campusStats", "orderStats"}, allEntries = true)
    @Transactional
    public Product publish(ProductForm form, MultipartFile[] images, User seller) throws IOException {
        Product product = new Product();
        product.setSellerId(seller.getId());
        product.setTitle(form.getTitle());
        product.setCategory(form.getCategory());
        product.setPrice(form.getPrice());
        product.setConditionLabel(form.getConditionLabel());
        product.setDescription(form.getDescription());
        product.setStatus("ON_SALE");
        product.setCampusArea(form.getCampusArea());
        product.setTradePlace(form.getTradePlace());
        product.setViewCount(0);
        product.setDeleted(false);
        productMapper.insert(product);

        int sortNo = 0;
        if (images != null) {
            for (MultipartFile image : images) {
                String url = storageService.storeProductImage(image);
                if (url != null) {
                    ProductImage productImage = new ProductImage();
                    productImage.setProductId(product.getId());
                    productImage.setUrl(url);
                    productImage.setSortNo(sortNo++);
                    imageMapper.insert(productImage);
                }
            }
        }
        return product;
    }

    public Product findProduct(Long id, boolean increaseView) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在或已下架");
        }
        if (increaseView) {
            productMapper.increaseView(id);
            product.setViewCount(product.getViewCount() == null ? 1 : product.getViewCount() + 1);
        }
        return product;
    }

    public void recordView(Product product, String viewerKey) {
        if (viewDedupService.firstView(product.getId(), viewerKey)) {
            productMapper.increaseView(product.getId());
            product.setViewCount(product.getViewCount() == null ? 1 : product.getViewCount() + 1);
        }
    }

    public List<ProductImage> imagesFor(Product product) {
        List<ProductImage> images = new ArrayList<>(imageMapper.findByProductId(product.getId()));
        if (images.isEmpty()) {
            ProductImage fallback = new ProductImage();
            fallback.setProductId(product.getId());
            fallback.setUrl(storageService.defaultImage(product.getCategory()));
            fallback.setSortNo(0);
            images.add(fallback);
        }
        return images;
    }

    public List<ProductCard> sellerProducts(Long sellerId) {
        return productMapper.findBySellerId(sellerId);
    }

    public List<ProductCard> similarProducts(Product product, int limit) {
        return productMapper.findSimilar(product.getCategory(), product.getId(), Math.max(1, Math.min(limit, 8)));
    }

    public boolean isFavorite(Long userId, Long productId) {
        return userId != null && favoriteMapper.exists(userId, productId) > 0;
    }

    @CacheEvict(value = "productSearch", allEntries = true)
    @Transactional
    public boolean toggleFavorite(Long userId, Long productId) {
        if (favoriteMapper.exists(userId, productId) > 0) {
            favoriteMapper.delete(userId, productId);
            return false;
        }
        favoriteMapper.insert(userId, productId);
        return true;
    }

    public List<ProductCard> favoriteProducts(Long userId) {
        return favoriteMapper.findProductsByUser(userId);
    }

    public int favoriteCount(Long productId) {
        return favoriteMapper.countByProduct(productId);
    }

    @Transactional
    public void addComment(Long productId, Long userId, String content) {
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("留言内容不能为空");
        }
        Comment comment = new Comment();
        comment.setProductId(productId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        commentMapper.insert(comment);
    }

    public List<CommentView> comments(Long productId) {
        return commentMapper.findByProductId(productId);
    }

    @CacheEvict(value = {"productSearch", "dashboardStats", "categoryStats", "productStatusStats", "campusStats", "orderStats"}, allEntries = true)
    @Transactional
    public void updateStatus(Long productId, String status, User user) {
        Product product = findProduct(productId, false);
        boolean owner = product.getSellerId().equals(user.getId());
        boolean admin = "ADMIN".equals(user.getRole());
        if (!owner && !admin) {
            throw new IllegalArgumentException("无权修改该商品状态");
        }
        productMapper.updateStatus(productId, status);
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private List<String> expandSearchTerms(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        Set<String> terms = new LinkedHashSet<>();
        terms.add(keyword);
        String lower = keyword.toLowerCase();
        Map<String, List<String>> synonyms = Map.ofEntries(
                Map.entry("耳机", List.of("耳麦", "蓝牙", "降噪", "sony")),
                Map.entry("耳麦", List.of("耳机", "蓝牙", "降噪")),
                Map.entry("键盘", List.of("机械键盘", "蓝牙键盘", "茶轴", "罗技")),
                Map.entry("电脑", List.of("笔记本", "显示器", "支架", "数码")),
                Map.entry("平板", List.of("ipad", "保护壳", "电容笔")),
                Map.entry("考研", List.of("真题", "资料", "复习", "数学", "英语")),
                Map.entry("资料", List.of("教材", "真题", "讲义", "复习")),
                Map.entry("书", List.of("教材", "资料", "课程", "真题")),
                Map.entry("宿舍", List.of("台灯", "收纳", "桌", "生活用品")),
                Map.entry("台灯", List.of("护眼灯", "暖光", "宿舍")),
                Map.entry("车", List.of("自行车", "骑行", "头盔", "运动")),
                Map.entry("衣服", List.of("卫衣", "服饰", "美妆服饰"))
        );
        for (Map.Entry<String, List<String>> entry : synonyms.entrySet()) {
            if (lower.contains(entry.getKey().toLowerCase())) {
                terms.addAll(entry.getValue());
            }
        }
        return terms.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .limit(8)
                .toList();
    }
}
