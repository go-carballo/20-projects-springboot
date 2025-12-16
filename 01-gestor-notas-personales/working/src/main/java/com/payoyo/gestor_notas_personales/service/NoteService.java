package com.payoyo.gestor_notas_personales.service;

import com.payoyo.gestor_notas_personales.entity.Note;

import java.util.List;

public interface NoteService {
    Note createNote(Note note);
    List<Note> getAllNotes();
    Note getNoteById(Long id);
    Note updateNote(Long id, Note noteDetails);
    void deleteNoteById(Long id);
}
