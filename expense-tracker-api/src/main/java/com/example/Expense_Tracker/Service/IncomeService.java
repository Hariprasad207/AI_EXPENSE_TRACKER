package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Repository.CategoryRepo;
import com.example.Expense_Tracker.Repository.IncomeRepo;
import com.example.Expense_Tracker.dto.Income.IncomeRequest;
import com.example.Expense_Tracker.dto.Income.IncomeResponse;
import com.example.Expense_Tracker.entity.Category;
import com.example.Expense_Tracker.entity.Income;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IncomeService {
    private final IncomeRepo incomeRepo;
    private final CategoryRepo categoryRepo;

    public IncomeResponse createIncome(IncomeRequest request,Long userId){
        Category category = categoryRepo.findAvailableCategory(request.getCategoryId(),userId)
                .orElseThrow(()-> new RuntimeException("That category is not found"));

        if(!"INCOME".equals(category.getType().name())){
            throw  new RuntimeException("Selected category is not income");
        }
        Income income = Income.builder()
                .userId(userId)
                .categoryId(request.getCategoryId())
                .amount(request.getAmount())
                .incomeDate(request.getIncomeDate())
                .title(request.getTitle())
                .source(request.getSource())
                .description(request.getDescription())
                .build();
        Income savedIncome = incomeRepo.save(income);
        return  mapToResponse(savedIncome,category);
    }

    public IncomeResponse getIncomeId(Long id,Long userId){
        Income income = incomeRepo.findByIdAndUserId(id,userId)
                .orElseThrow(()-> new RuntimeException("Income not found"));
        Category category =categoryRepo.findById(income.getCategoryId())
                .orElseThrow(() ->new RuntimeException("Category not found"));
        return mapToResponse(income,category);
    }

    public IncomeResponse updateIncome(IncomeRequest request,Long id,Long userId){
        Income income = incomeRepo.findByIdAndUserId(id,userId)
                .orElseThrow(()-> new RuntimeException("Income not found"));
        Category category = categoryRepo.findAvailableCategory(request.getCategoryId(), userId)
                .orElseThrow(()-> new RuntimeException("Income is not matched with the category"));
        if(!"INCOME".equals(category.getType().name()))
            throw new RuntimeException("Selected Category is not income");
        income.setCategoryId(request.getCategoryId());
        income.setAmount(request.getAmount());
        income.setIncomeDate(request.getIncomeDate());
        income.setTitle(request.getTitle());
        income.setSource(request.getSource());
        income.setDescription(request.getDescription());
        Income updatedIncome =incomeRepo.save(income);

        return mapToResponse(updatedIncome,category);
    }

    public void deleteIncome(Long id,Long userId){
        Income income = incomeRepo.findByIdAndUserId(id,userId)
                .orElseThrow(()-> new RuntimeException("Income not found"));
        incomeRepo.delete(income);
    }

    private IncomeResponse mapToResponse(Income income,Category category){
        return IncomeResponse.builder()
                .id(income.getId())
                .userId(income.getUserId())
                .categoryId(income.getCategoryId())
                .categoryName(category.getName())
                .amount(income.getAmount())
                .incomeDate(income.getIncomeDate())
                .title(income.getTitle())
                .source(income.getSource())
                .description(income.getDescription())
                .createdAt(income.getCreatedAt())
                .updatedAt(income.getUpdatedAt())
                .build();
    }

    public Page<IncomeResponse> getIncome(Long userId,Pageable pageable) {
        return incomeRepo
                .findByUserId(userId, pageable)
                .map(income -> {
                    Category category =categoryRepo
                        .findById(income.getCategoryId())
                        .orElseThrow(() ->new RuntimeException("Category not found"));

                    return mapToResponse(income,category);
                });
    }
}
