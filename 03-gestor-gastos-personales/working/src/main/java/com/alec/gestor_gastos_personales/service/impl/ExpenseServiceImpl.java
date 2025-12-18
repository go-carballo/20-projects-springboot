package com.alec.gestor_gastos_personales.service.impl;

import com.alec.gestor_gastos_personales.exception.ExpenseNotFoundException;
import com.alec.gestor_gastos_personales.model.CategoryEnum;
import com.alec.gestor_gastos_personales.model.Expense;
import com.alec.gestor_gastos_personales.model.PaymentMethodEnum;
import com.alec.gestor_gastos_personales.repository.ExpenseRepository;
import com.alec.gestor_gastos_personales.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementación del servicio de gestión de gastos.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    public Expense createExpense(Expense expense) {
        validateExpense(expense);
        return expenseRepository.save(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAllByOrderByDateDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    @Override
    public Expense updateExpense(Long id, Expense expense) {
        Expense existingExpense = getExpenseById(id);
        
        validateExpense(expense);
        
        existingExpense.setDescription(expense.getDescription());
        existingExpense.setAmount(expense.getAmount());
        existingExpense.setCategory(expense.getCategory());
        existingExpense.setDate(expense.getDate());
        existingExpense.setPaymentMethod(expense.getPaymentMethod());
        
        return expenseRepository.save(existingExpense);
    }

    @Override
    public void deleteExpense(Long id) {
        Expense expense = getExpenseById(id);
        expenseRepository.delete(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByCategory(CategoryEnum category) {
        return expenseRepository.findByCategoryOrderByDateDesc(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesBetweenDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        return expenseRepository.findByDateBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByPaymentMethod(PaymentMethodEnum paymentMethod) {
        return expenseRepository.findByPaymentMethodOrderByDateDesc(paymentMethod);
    }

    /**
     * Validaciones de negocio adicionales.
     */
    private void validateExpense(Expense expense) {
        // Validar que la fecha no sea futura
        if (expense.getDate() != null && expense.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha del gasto no puede ser futura");
        }
        
        // Validar que el monto sea positivo
        if (expense.getAmount() != null && expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
    }
}
