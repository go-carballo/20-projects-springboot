package com.payoyo.gestor_notas_personales.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.payoyo.gestor_notas_personales.entity.Note;
import com.payoyo.gestor_notas_personales.service.INoteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/*
 * Controlador REST para la gestion de notas personales
 * Expone endpoints para operaciones CRUD sobre notas
 */
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    
    private final INoteService noteService;

    /*
     * Crea un nueva nota
     * 
     * @param note -> la nota a crear (sin ID)
     * @return la nota creada con su ID generado y status 201 CREATED
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Note createNote(@Valid @RequestBody Note note){
        return noteService.createNote(note);
    }

    /**
     * Actualiza una nota existente.
     * 
     * @param id el ID de la nota a actualizar
     * @param note los nuevos datos de la nota
     * @return la nota actualizada y status 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(
            @PathVariable Long id, 
            @Valid @RequestBody Note note) {
        Note updatedNote = noteService.updateNote(id, note);
        return ResponseEntity.ok(updatedNote);
    }

    /**
     * Obtiene todas las notas del sistema.
     * 
     * @return lista de todas las notas y status 200 OK
     */
    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        List<Note> notes = noteService.getAllNotes();
        return ResponseEntity.ok(notes);
    }
    

    /**
     * Obtiene una nota específica por su ID.
     * 
     * @param id el ID de la nota a buscar
     * @return la nota encontrada y status 200 OK
     */
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        Note note = noteService.getNoteById(id);
        return ResponseEntity.ok(note);
    }

    /**
     * Elimina una nota del sistema.
     * 
     * @param id el ID de la nota a eliminar
     * @return status 204 NO CONTENT
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNoteById(@PathVariable Long id) {
        noteService.deleteNoteById(id);
    }
   

}
