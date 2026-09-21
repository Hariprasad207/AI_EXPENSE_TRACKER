package com.example.Expense_Tracker.dto.Category;

import com.example.Expense_Tracker.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Category name must contains character")
    @Size(max=50,message = "Category name must be below 50 character")
    private String name;

    @NotNull(message = "Catergory type is essential")
    private Category.CategoryType type;

    @Size(max=50,message = "Category icons must be below 50 character")
    private String icon;

}
