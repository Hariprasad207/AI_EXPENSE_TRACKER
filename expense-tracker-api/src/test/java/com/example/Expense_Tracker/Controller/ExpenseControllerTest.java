package com.example.Expense_Tracker.Controller;

import com.example.Expense_Tracker.Config.JWTService;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.Service.ExpenseService;
import com.example.Expense_Tracker.dto.Expense.CreateExpenseRequest;
import com.example.Expense_Tracker.dto.Expense.ExpenseResponse;
import com.example.Expense_Tracker.dto.Expense.UpdateExpenseRequest;
import com.example.Expense_Tracker.entity.Expense;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
public class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserRepo userRepo;

    @Test
    void createExpense_shouldReturnCreated() throws Exception {

        CreateExpenseRequest request = new CreateExpenseRequest();

        request.setCategoryId(1L);
        request.setAmount(new BigDecimal("500.00"));
        request.setPaymentMode(Expense.PaymentMode.CASH);
        request.setExpenseDate(LocalDate.of(2026, 10, 6));
        request.setTitle("Groceries");
        request.setDescription("Monthly groceries");

        ExpenseResponse response = ExpenseResponse.builder()
                .id(1L)
                .categoryId(1L)
                .categoryName("Food")
                .amount(new BigDecimal("500.00"))
                .paymentMode(Expense.PaymentMode.CASH)
                .expenseDate(LocalDate.of(2026, 10, 6))
                .title("Groceries")
                .description("Monthly groceries")
                .build();

        when(expenseService.currentExpense(any(CreateExpenseRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/expenses")
                        .with(csrf())
                        .with(user("testUser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.categoryName").value("Food"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.paymentMode").value("CASH"))
                .andExpect(jsonPath("$.title").value("Groceries"));

        verify(expenseService).currentExpense(any(CreateExpenseRequest.class));
    }

    @Test
    void createExpense_shouldRejectInvalidRequest() throws Exception {
        CreateExpenseRequest request = new CreateExpenseRequest();

        mockMvc.perform(post("/api/expenses")
                        .with(csrf())
                        .with(user("testUser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(expenseService, never())
                .currentExpense(any(CreateExpenseRequest.class));
    }

    @Test
    void createExpense_shouldRejectZeroAmount() throws Exception {
        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setCategoryId(1L);
        request.setAmount(BigDecimal.ZERO);
        request.setPaymentMode(Expense.PaymentMode.UPI);
        request.setExpenseDate(LocalDate.of(2026, 10, 6));
        request.setTitle("Invalid Expense");

        mockMvc.perform(post("/api/expenses")
                        .with(csrf())
                        .with(user("testUser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(expenseService, never())
                .currentExpense(any(CreateExpenseRequest.class));
    }

    @Test
    void getExpenseById_shouldReturnExpense() throws Exception {
        ExpenseResponse response = ExpenseResponse.builder()
                .id(1L)
                .categoryId(1L)
                .categoryName("Food")
                .amount(new BigDecimal("500.00"))
                .paymentMode(Expense.PaymentMode.UPI)
                .expenseDate(LocalDate.of(2026, 10, 6))
                .title("Groceries")
                .description("Monthly groceries")
                .build();

        when(expenseService.getExpenseById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/expenses/1")
                        .with(user("testUser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.categoryName").value("Food"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.paymentMode").value("UPI"));

        verify(expenseService).getExpenseById(1L);
    }

    @Test
    void getExpense_shouldReturnPageOFExpense() throws Exception {
        ExpenseResponse response = ExpenseResponse.builder()
                .id(1L)
                .categoryId(1L)
                .categoryName("Food")
                .amount(new BigDecimal("500.00"))
                .paymentMode(Expense.PaymentMode.UPI)
                .expenseDate(LocalDate.of(2026, 10, 6))
                .title("Groceries")
                .build();

        PageImpl<ExpenseResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);
        when(expenseService.getExpenses(isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/expenses")
                        .with(user("testUser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].categoryName").value("Food"))
                .andExpect(jsonPath("$.content[0].amount").value(500.00))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(expenseService).getExpenses(isNull(), isNull(), isNull(), isNull(), any());
    }

    @Test
    void getExpenses_shouldAcceptFilters() throws Exception {

        ExpenseResponse response = ExpenseResponse.builder()
                .id(1L)
                .categoryId(2L)
                .categoryName("Travel")
                .amount(new BigDecimal("1000.00"))
                .paymentMode(Expense.PaymentMode.CASH)
                .expenseDate(LocalDate.of(2026, 10, 5))
                .title("Taxi")
                .build();

        PageImpl<ExpenseResponse> page = new PageImpl<>(List.of(response));

        when(expenseService.getExpenses(eq(2L),
                eq(Expense.PaymentMode.CASH),
                eq(LocalDate.of(2026, 10, 1)),
                eq(LocalDate.of(2026, 10, 6)),
                any()
        )).thenReturn(page);

        mockMvc.perform(get("/api/expenses")
                        .with(user("testUser"))
                        .param("categoryId", "2")
                        .param("paymentMode", "CASH")
                        .param("fromDate", "2026-10-01")
                        .param("toDate", "2026-10-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].categoryName").value("Travel"))
                .andExpect(jsonPath("$.content[0].paymentMode").value("CASH"));
        verify(expenseService).getExpenses(
                eq(2L),
                eq(Expense.PaymentMode.CASH),
                eq(LocalDate.of(2026, 10, 1)),
                eq(LocalDate.of(2026, 10, 6)),
                any()
        );
    }

    @Test
    void updateExpense_shouldReturnUpdatedExpense() throws Exception {

        UpdateExpenseRequest request = new UpdateExpenseRequest();
        request.setCategoryId(1L);
        request.setAmount(new BigDecimal("700.00"));
        request.setPaymentMode(Expense.PaymentMode.DEBIT_CARD);
        request.setExpenseDate(LocalDate.of(2026, 10, 6));
        request.setTitle("Updated Groceries");
        request.setDescription("Updated description");

        ExpenseResponse response = ExpenseResponse.builder()
                .id(1L)
                .categoryId(1L)
                .categoryName("Food")
                .amount(new BigDecimal("750.00"))
                .paymentMode(Expense.PaymentMode.DEBIT_CARD)
                .expenseDate(LocalDate.of(2026, 10, 6))
                .title("Updated Groceries")
                .description("Updated description")
                .build();

        when(expenseService.updateExpense(eq(1L),any(UpdateExpenseRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/expenses/1")
                        .with(csrf())
                        .with(user("testUser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(750.00))
                .andExpect(jsonPath("$.paymentMode").value("DEBIT_CARD"))
                .andExpect(jsonPath("$.title").value("Updated Groceries"));
        verify(expenseService).updateExpense(eq(1L),any(UpdateExpenseRequest.class));
    }

    @Test
    void updateExpense_shouldRejectInvalidRequest() throws Exception {

        UpdateExpenseRequest request = new UpdateExpenseRequest();

        mockMvc.perform(put("/api/expenses/1")
                        .with(csrf())
                        .with(user("testUser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(expenseService, never()).updateExpense(
                anyLong(),
                any(UpdateExpenseRequest.class)
        );
    }

    @Test
    void deleteExpense_shouldReturnNoContent() throws Exception {

        doNothing().when(expenseService).deleteExpense(1L);

        mockMvc.perform(delete("/api/expenses/1")
                        .with(csrf())
                        .with(user("testUser")))
                .andExpect(status().isNoContent());

        verify(expenseService).deleteExpense(1L);
    }
}