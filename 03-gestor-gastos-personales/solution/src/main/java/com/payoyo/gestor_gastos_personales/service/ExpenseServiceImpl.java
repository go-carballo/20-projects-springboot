package com.payoyo.gestor_gastos_personales.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.payoyo.gestor_gastos_personales.entity.Expense;
import com.payoyo.gestor_gastos_personales.entity.enums.CategoryEnum;
import com.payoyo.gestor_gastos_personales.entity.enums.PaymentMethodEnum;
import com.payoyo.gestor_gastos_personales.exceptions.ExpenseNotFoundException;
import com.payoyo.gestor_gastos_personales.repository.ExpenseRepository;

import lombok.RequiredArgsConstructor;

/**
Implementacion del servicio de gestion de gastos personales

Esta clase contiene toda la lógica de negocio para operaciones CRUD, filtros y generación de reportes con cálculos financieros.
@author Jose Luis (Payoyo)
*/
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService{

    private final ExpenseRepository expenseRepository;

    // ==================== OPERACIONES CRUD ====================

    /**
     * Crea un nuevo gasto en el sistema.
     * Valida que la fecha no sea futura antes de guardar.
     * 
     * @param expense Gasto a crear
     * @return Gasto creado con ID generado
     * @throws IllegalArgumentException si la fecha es futura
     */
    @Override
    public Expense createExpense(Expense expense) {
        // validacion -> la fecha no puede ser futura
        if (expense.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha del gasto no puede ser futura");
        }

        return expenseRepository.save(expense);
    }

    /**
     * Obtiene todos los gastos ordenados por fecha descendente.
     * 
     * @return Lista de todos los gastos (más recientes primero)
     */
    @Override
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAllByOrderByDateDesc();
    }

    /**
     * Busca un gasto por su ID.
     * 
     * @param id ID del gasto a buscar
     * @return Gasto encontrado
     * @throws ExpenseNotFoundException si el ID no existe
     */
    @Override
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    /**
     * Actualiza un gasto existente.
     * Valida que el ID exista y que la nueva fecha no sea futura.
     * 
     * @param id ID del gasto a actualizar
     * @param expense Datos actualizados del gasto
     * @return Gasto actualizado
     * @throws ExpenseNotFoundException si el ID no existe
     * @throws IllegalArgumentException si la fecha es futura
     */
    @Override
    public Expense updateExpense(Long id, Expense expense) {
        // Verificar que el gasto existe
        Expense existingExpense = getExpenseById(id);
        
        // Validación de negocio: la fecha no puede ser futura
        if (expense.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha del gasto no puede ser futura");
        }
        
        // Actualizar campos (manteniendo el ID original)
        existingExpense.setDescription(expense.getDescription());
        existingExpense.setAmount(expense.getAmount());
        existingExpense.setCategory(expense.getCategory());
        existingExpense.setDate(expense.getDate());
        existingExpense.setPaymentMethod(expense.getPaymentMethod());
        
        return expenseRepository.save(existingExpense);
    }

    /**
     * Elimina un gasto del sistema.
     * 
     * @param id ID del gasto a eliminar
     * @throws ExpenseNotFoundException si el ID no existe
     */
    @Override
    public void deleteExpense(Long id) {
        // Verificar que el gasto existe antes de eliminar
        Expense expense = getExpenseById(id);
        expenseRepository.delete(expense);
    }

    // ==================== FILTROS Y CONSULTAS ====================

    /**
     * Filtra gastos por categoría.
     * 
     * @param category Categoría para filtrar
     * @return Lista de gastos de la categoría especificada
     */
    @Override
    public List<Expense> getExpensesByCategory(CategoryEnum category) {
        return expenseRepository.findByCategoryOrderByDateDesc(category);
    }

    /**
     * Filtra gastos por rango de fechas (inclusive).
     * 
     * @param startDate Fecha de inicio (inclusive)
     * @param endDate Fecha de fin (inclusive)
     * @return Lista de gastos en el rango de fechas
     */
    @Override
    public List<Expense> getExpensesByDateBetween(LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByDateBetweenOrderByDateDesc(startDate, endDate);
    }

    /**
     * Filtra gastos por método de pago.
     * 
     * @param paymentMethod Método de pago para filtrar
     * @return Lista de gastos con el método de pago especificado
     */
    @Override
    public List<Expense> getExpensesByPaymentMethod(PaymentMethodEnum paymentMethod) {
        return expenseRepository.findByPaymentMethodOrderByDateDesc(paymentMethod);
    }

    // ==================== REPORTES ====================

    /**
     * Genera reporte de gastos agrupados por categoría.
     * Calcula el total y cuenta de gastos para cada categoría.
     * 
     * @return Lista de Maps con información por categoría:
     *         - category: nombre de la categoría
     *         - totalAmount: suma total de gastos
     *         - expenseCount: cantidad de gastos
     */

    @Override
    public List<Map<String, Object>> getReportByCategory() {
        List<Expense> allExpenses = expenseRepository.findAll();

        // agrupar gastos por categoria
        Map<CategoryEnum, List<Expense>> expensesByCategory = allExpenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory));
        // Crear lista de reportes por categoría
        List<Map<String, Object>> report = new ArrayList<>();
        
        for (Map.Entry<CategoryEnum, List<Expense>> entry : expensesByCategory.entrySet()) {
            CategoryEnum category = entry.getKey();
            List<Expense> expenses = entry.getValue();
            
            // Calcular total sumando todos los montos de la categoría
            BigDecimal totalAmount = expenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Crear el Map con los datos del reporte
            Map<String, Object> categoryReport = new HashMap<>();
            categoryReport.put("category", category.name());
            categoryReport.put("totalAmount", totalAmount);
            categoryReport.put("expenseCount", expenses.size());
            
            report.add(categoryReport);
        }

        // Ordenar por total descendente (mayor gasto primero)
        report.sort((a, b) -> {
            BigDecimal totalA = (BigDecimal) a.get("totalAmount");
            BigDecimal totalB = (BigDecimal) b.get("totalAmount");
            return totalB.compareTo(totalA);
        });
        
        return report;
    }

    /**
     * Genera reporte de gastos en un período específico.
     * Calcula total, cantidad y promedio de gastos.
     * 
     * @param startDate Fecha de inicio del período
     * @param endDate Fecha de fin del período
     * @return Map con estadísticas del período:
     *         - startDate: fecha inicial
     *         - endDate: fecha final
     *         - totalAmount: suma total de gastos
     *         - expenseCount: cantidad de gastos
     *         - averageExpense: promedio por gasto
     */
    @Override
    public Map<String, Object> getReportByPeriod(LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByDateBetweenOrderByDateDesc(startDate, endDate);
        
        // Calcular el total sumando todos los montos
        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int expenseCount = expenses.size();
        
        // Calcular promedio (evitar división por cero)
        BigDecimal averageExpense = BigDecimal.ZERO;
        if (expenseCount > 0) {
            averageExpense = totalAmount.divide(
                    BigDecimal.valueOf(expenseCount),
                    2,  // 2 decimales
                    RoundingMode.HALF_UP  // Redondeo estándar
            );
        }
        
        // Construir el Map de respuesta
        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate.toString());
        report.put("endDate", endDate.toString());
        report.put("totalAmount", totalAmount);
        report.put("expenseCount", expenseCount);
        report.put("averageExpense", averageExpense);
        
        return report;
    }

    /**
     * Genera reporte del mes actual con estadísticas completas.
     * Incluye total, cantidad, categoría más cara y más barata.
     * 
     * @return Map con estadísticas del mes actual:
     *         - month: nombre del mes
     *         - year: año actual
     *         - totalAmount: suma total de gastos
     *         - expenseCount: cantidad de gastos
     *         - mostExpensiveCategory: categoría con mayor gasto
     *         - leastExpensiveCategory: categoría con menor gasto
     */
    @Override
    public Map<String, Object> getCurrentMonthReport() {
        // Calcular primer y último día del mes actual
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        
        // Obtener gastos del mes actual  00 .
        List<Expense> monthExpenses = expenseRepository.findByDateBetweenOrderByDateDesc(
                startOfMonth, 
                endOfMonth
        );
        
        // Calcular total del mes
        BigDecimal totalAmount = monthExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Agrupar por categoría para encontrar la más y menos cara
        Map<CategoryEnum, BigDecimal> totalsByCategory = monthExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Expense::getAmount,
                                BigDecimal::add
                        )
                ));
        
        // Encontrar categoría más cara y más barata
        CategoryEnum mostExpensive = null;
        CategoryEnum leastExpensive = null;
        
        if (!totalsByCategory.isEmpty()) {
            // Categoría con mayor gasto
            mostExpensive = totalsByCategory.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            
            // Categoría con menor gasto
            leastExpensive = totalsByCategory.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }
        
        // Construir el Map de respuesta
        Map<String, Object> report = new HashMap<>();
        report.put("month", now.getMonth().name());
        report.put("year", now.getYear());
        report.put("totalAmount", totalAmount);
        report.put("expenseCount", monthExpenses.size());
        report.put("mostExpensiveCategory", mostExpensive != null ? mostExpensive.name() : null);
        report.put("leastExpensiveCategory", leastExpensive != null ? leastExpensive.name() : null);
        
        return report;
    }
    
}
