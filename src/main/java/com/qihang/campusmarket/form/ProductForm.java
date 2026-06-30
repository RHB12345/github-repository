package com.qihang.campusmarket.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductForm {
    @NotBlank(message = "标题不能为空")
    @Size(max = 60, message = "标题最多 60 个字")
    private String title;

    @NotBlank(message = "请选择分类")
    private String category;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;

    @NotBlank(message = "请选择成色")
    private String conditionLabel;

    @NotBlank(message = "请填写商品描述")
    @Size(max = 1000, message = "描述最多 1000 个字")
    private String description;

    @NotBlank(message = "请选择校区")
    private String campusArea;

    @NotBlank(message = "请填写交易地点")
    private String tradePlace;
}
