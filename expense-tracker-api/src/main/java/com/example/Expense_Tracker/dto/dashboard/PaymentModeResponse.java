package com.example.Expense_Tracker.dto.dashboard;

import com.example.Expense_Tracker.entity.Expense;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentModeResponse {
    private Expense.PaymentMode paymentMode;
    private BigDecimal amount;
}
