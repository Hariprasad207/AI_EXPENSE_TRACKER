package com.example.Expense_Tracker.Specification;

import com.example.Expense_Tracker.entity.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ExpenseSpecification {
    public static Specification<Expense> hasCategory(Long categoryId){
        return (root,query,criteriaBuilder)->
                categoryId == null ? null
                        : criteriaBuilder.equal(root.get("category").get("id"),categoryId);
    }

    public static  Specification<Expense> hasPaymentMode(Expense.PaymentMode paymentMode){
        return ((root, query, criteriaBuilder) ->
                paymentMode == null ? null
                        : criteriaBuilder.equal(root.get("paymentMode"),paymentMode)
                );
    }

    public static Specification<Expense> dateBetween(LocalDate startDate,LocalDate endDate){
        return ((root, query, criteriaBuilder) ->{
            if(startDate!=null && endDate!=null){
                return criteriaBuilder.between(root.get("expenseDate"),startDate,endDate);
            }
            if(startDate!=null){
                return criteriaBuilder.greaterThanOrEqualTo(root.get("expenseDate"),startDate);
            }
            if(endDate!=null){
                return criteriaBuilder.greaterThanOrEqualTo(root.get("expenseDate"),endDate);
            }
            return null;
        }
        );
    }
}
