package com.alec.gestor_gastos_personales.repository;

import com.alec.gestor_gastos_personales.model.CategoryEnum;
import com.alec.gestor_gastos_personales.model.Expense;
import com.alec.gestor_gastos_personales.model.PaymentMethodEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad Expense.
 * Incluye query methods derivados para filtrado y consultas.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Busca todos los gastos de una categoría específica ordenados por fecha descendente.
     */
    List<Expense> findByCategoryOrderByDateDesc(CategoryEnum category);

    /**
     * Busca todos los gastos entre dos fechas (inclusivo).
     */
    List<Expense> findByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Busca todos los gastos por método de pago ordenados por fecha descendente.
     */
    List<Expense> findByPaymentMethodOrderByDateDesc(PaymentMethodEnum paymentMethod);

    /**
     * Busca todos los gastos ordenados por fecha descendente.
     */
    List<Expense> findAllByOrderByDateDesc();
}
