package com.alec.gestor_gastos_personales.controller;

import com.alec.gestor_gastos_personales.model.CategoryEnum;
import com.alec.gestor_gastos_personales.model.Expense;
import com.alec.gestor_gastos_personales.model.PaymentMethodEnum;
import com.alec.gestor_gastos_personales.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para la gestión de gastos personales.
 */
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Crea un nuevo gasto.
     * POST /api/expenses
     */
    @PostMapping
    public ResponseEntity<Expense> createExpense(@Valid @RequestBody Expense expense) {
        Expense createdExpense = expenseService.createExpense(expense);
        return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
    }

    /**
     * Obtiene todos los gastos.
     * GET /api/expenses
     */
    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses() {
        List<Expense> expenses = expenseService.getAllExpenses();
        return ResponseEntity.ok(expenses);
    }

    /**
     * Obtiene un gasto por su ID.
     * GET /api/expenses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id) {
        Expense expense = expenseService.getExpenseById(id);
        return ResponseEntity.ok(expense);
    }

    /**
     * Actualiza un gasto existente.
     * PUT /api/expenses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody Expense expense) {
        Expense updatedExpense = expenseService.updateExpense(id, expense);
        return ResponseEntity.ok(updatedExpense);
    }

    /**
     * Elimina un gasto por su ID.
     * DELETE /api/expenses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Filtra gastos por categoría.
     * GET /api/expenses/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Expense>> getExpensesByCategory(@PathVariable CategoryEnum category) {
        List<Expense> expenses = expenseService.getExpensesByCategory(category);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Filtra gastos por rango de fechas.
     * GET /api/expenses/between?startDate=2024-11-01&endDate=2024-11-30
     */
    @GetMapping("/between")
    public ResponseEntity<List<Expense>> getExpensesBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Expense> expenses = expenseService.getExpensesBetweenDates(startDate, endDate);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Filtra gastos por método de pago.
     * GET /api/expenses/payment-method/{paymentMethod}
     */
    @GetMapping("/payment-method/{paymentMethod}")
    public ResponseEntity<List<Expense>> getExpensesByPaymentMethod(@PathVariable PaymentMethodEnum paymentMethod) {
        List<Expense> expenses = expenseService.getExpensesByPaymentMethod(paymentMethod);
        return ResponseEntity.ok(expenses);
    }
}
