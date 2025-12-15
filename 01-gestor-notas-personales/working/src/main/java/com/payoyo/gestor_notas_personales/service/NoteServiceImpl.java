package com.payoyo.gestor_notas_personales.service;

import com.payoyo.gestor_notas_personales.entity.Note;
import com.payoyo.gestor_notas_personales.exception.NoteNotFoundException;
import com.payoyo.gestor_notas_personales.repository.NoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;

    public NoteServiceImpl(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public Note createNote(Note note) {
        log.info("Creating note with title: {}", note.getTitle());
        note.setCreatedAt(LocalDateTime.now());
        note.setLastModified(LocalDateTime.now());
        Note savedNote = noteRepository.save(note);
        log.info("Note created with ID: {}", savedNote.getId());
        return savedNote;
    }

    @Override
    public List<Note> getAllNotes() {
        log.info("Fetching all notes");
        return noteRepository.findAll();
    }

    @Override
    public Note getNoteById(Long id) {
        log.info("Fetching note with ID: {}", id);
        return noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("Nota no encontrada con ID: " + id));
    }

    @Override
    public Note updateNote(Long id, Note noteDetails) {
        log.info("Updating note with ID: {}", id);
        Note note = getNoteById(id);
        note.setTitle(noteDetails.getTitle());
        note.setContent(noteDetails.getContent());
        note.setLastModified(LocalDateTime.now());
        Note updatedNote = noteRepository.save(note);
        log.info("Note updated with ID: {}", id);
        return updatedNote;
    }

    @Override
    public void deleteNote(Long id) {
        log.info("Deleting note with ID: {}", id);
        Note note = getNoteById(id);
        noteRepository.delete(note);
        log.info("Note deleted with ID: {}", id);
    }

}
