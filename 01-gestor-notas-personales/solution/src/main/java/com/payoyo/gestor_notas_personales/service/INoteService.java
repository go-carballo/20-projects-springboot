package com.payoyo.gestor_notas_personales.service;

import java.util.List;

import com.payoyo.gestor_notas_personales.entity.Note;

/*
 * Servicio para la gestion de notas personales
 * Define las operaciones de negocio disponibles para las notas
 */
public interface INoteService {
    
    /*
     * Crea una nueva nota en el sistema
     * 
     * @param note -> la nota a crear (sin ID)
     * @return la nota creada con su ID generado
     */
    Note createNote(Note note);

    /*
     * Actualiza una nota existente
     * 
     * @param id -> el ID de la nota a actualizar
     * @param note -> los nuevos datos de la nota
     * @return la nota actualizada
     * @throws -> NoteNotFoundException si no se encuentra la nota
     */
    Note updateNote(Long id, Note note);

    /*
     * Obtiene todas las notas
     * 
     * @return -> lista de todas las notas (puede estar vacia)
     */
    List<Note> getAllNotes();

    /*
     * Obtiene una nota específica por su ID
     * 
     * @param id -> el ID de la nota a buscar
     * @return la nota encontrada
     * @throws -> NoteNotFoundException si no se encuentra la nota
     */
    Note getNoteById(Long id);

    /*
     * Elimina una nota del sistema
     * 
     * @param id -> el ID de la nota a eliminar
     * @throws -> NoteNotFoundException si no se encuentra la nota
     */
    void deleteNoteById(Long id);

}
