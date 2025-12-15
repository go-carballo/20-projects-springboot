package com.payoyo.gestor_notas_personales.controller;

import com.payoyo.gestor_notas_personales.entity.Note;
import com.payoyo.gestor_notas_personales.service.NoteService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@Slf4j
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<Note> createNote(@Valid @RequestBody Note note) {
        log.info("POST /api/notes - Creating new note");
        Note createdNote = noteService.createNote(note);
        return new ResponseEntity<>(createdNote, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        log.info("GET /api/notes - Fetching all notes");
        List<Note> notes = noteService.getAllNotes();
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        log.info("GET /api/notes/{} - Fetching note", id);
        Note note = noteService.getNoteById(id);
        return ResponseEntity.ok(note);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody Note noteDetails) {
        log.info("PUT /api/notes/{} - Updating note", id);
        Note updatedNote = noteService.updateNote(id, noteDetails);
        return ResponseEntity.ok(updatedNote);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        log.info("DELETE /api/notes/{} - Deleting note", id);
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}
