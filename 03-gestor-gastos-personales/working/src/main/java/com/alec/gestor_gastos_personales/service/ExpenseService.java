package com.alec.gestor_gastos_personales.service;

import com.alec.gestor_gastos_personales.model.CategoryEnum;
import com.alec.gestor_gastos_personales.model.Expense;
import com.alec.gestor_gastos_personales.model.PaymentMethodEnum;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz de servicio para la gestión de gastos.
 */
public interface ExpenseService {

    /**
     * Crea un nuevo gasto.
     */
    Expense createExpense(Expense expense);

    /**
     * Obtiene todos los gastos.
     */
    List<Expense> getAllExpenses();

    /**
     * Obtiene un gasto por su ID.
     */
    Expense getExpenseById(Long id);

    /**
     * Actualiza un gasto existente.
     */
    Expense updateExpense(Long id, Expense expense);

    /**
     * Elimina un gasto por su ID.
     */
    void deleteExpense(Long id);

    /**
     * Obtiene gastos por categoría.
     */
    List<Expense> getExpensesByCategory(CategoryEnum category);

    /**
     * Obtiene gastos entre dos fechas.
     */
    List<Expense> getExpensesBetweenDates(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene gastos por método de pago.
     */
    List<Expense> getExpensesByPaymentMethod(PaymentMethodEnum paymentMethod);
}
