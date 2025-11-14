package com.payoyo.gestor_notas_personales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payoyo.gestor_notas_personales.entity.Note;

/*
 * Repositorio para la gestion de notas personales
 * Proporciona operaciones CRUD básicas mediante JpaRepository
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, Long>{
    
}
