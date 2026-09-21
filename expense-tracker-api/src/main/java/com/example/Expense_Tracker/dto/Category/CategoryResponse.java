package com.example.Expense_Tracker.dto.Category;

import com.example.Expense_Tracker.entity.Category;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CategoryResponse {
    private Long id;
    private String name;
    private Category.CategoryType type;
    private String icon;
    private Boolean isCustom;
}
