package com.payoyo.gestor_notas_personales.repository;

import com.payoyo.gestor_notas_personales.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
}
