package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Config.CurrentUserService;
import com.example.Expense_Tracker.Repository.CategoryRepo;
import com.example.Expense_Tracker.dto.Category.CategoryResponse;
import com.example.Expense_Tracker.dto.Category.CategoryRequest;
import com.example.Expense_Tracker.entity.Category;
import com.example.Expense_Tracker.Exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepo categoryRepo;
    private final CurrentUserService currentUserService;

    public List<CategoryResponse> getAllCategories(){
        Long userID = currentUserService.getCurrentUserID();
        return categoryRepo.findAvailableCategories(userID).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<CategoryResponse> getCategoryByType(Category.CategoryType type){
        Long userId = currentUserService.getCurrentUserID();
        return categoryRepo.findAvailableCategoryByType(userId,type)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CategoryResponse getCategoryById(Long id){
        Category category= categoryRepo.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("category not found: " +id));
        return mapToResponse(category);
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        Long userId = currentUserService.getCurrentUserID();
        Category category = Category.builder()
                .userId(userId)
                .name(request.getName())
                .type(request.getType())
                .icon(request.getIcon())
                .isCustom(true)
                .build();

        Category savedCategory = categoryRepo.save(category);

        return mapToResponse(savedCategory);
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request) {

        Long userId = currentUserService.getCurrentUserID();
        Category category = categoryRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("That Cateogry not found" + id));

        if(!userId.equals(category.getUserId())){
            throw new ResourceNotFoundException("Category not found");
        }

        category.setName(request.getName());
        category.setType(request.getType());
        category.setIcon(request.getIcon());

        Category updatedCategory = categoryRepo.save(category);
        return mapToResponse(updatedCategory);
    }

    public void deleteCategory(long id){
        Long userId = currentUserService.getCurrentUserID();
        Category category = categoryRepo.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("This Category not found"));

        if(!userId.equals(category.getUserId()))
                throw new ResourceNotFoundException("This category not found");
        categoryRepo.deleteById(id);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId()).name(category.getName()).type(category.getType())
                .icon(category.getIcon()).isCustom((category.getIsCustom())).build();
    }
}
