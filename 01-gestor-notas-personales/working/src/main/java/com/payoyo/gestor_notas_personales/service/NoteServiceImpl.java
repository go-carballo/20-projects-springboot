package com.payoyo.gestor_notas_personales.service;

import com.payoyo.gestor_notas_personales.entity.Note;
import com.payoyo.gestor_notas_personales.exception.NoteNotFoundException;
import com.payoyo.gestor_notas_personales.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {


    private final NoteRepository noteRepository;


    @Override
    @Transactional
    public Note createNote(Note note) {
        log.info("Creating note with title: {}", note.getTitle());
        Note savedNote = noteRepository.save(note);
        log.info("Note created with ID: {}", savedNote.getId());
        return savedNote;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Note> getAllNotes() {
        log.info("Fetching all notes");
        List<Note> notes = noteRepository.findAll();
        log.info("Fetching all notes", notes.size());
        return notes;
    }

    @Override
    public Note getNoteById(Long id) {
        log.info("Fetching note with ID: {}", id);
        return noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("Nota no encontrada con ID: " + id));
    }

    @Override
    public Note updateNote(Long id, Note note) {
        log.info("Updating note with ID: {}", id);

        Note existnote = noteRepository.findById(id).orElseThrow(()->{
            log.error("Note no encontrada con ID: {}", id);
            return new NoteNotFoundException("Note no encontrada con ID: " + id);
        });

        existnote.setTitle(note.getTitle());
        existnote.setContent(note.getContent());

        Note updatedNote = noteRepository.save(existnote);
        log.info("Note updated with ID: {}", id);
        return updatedNote;
    }

    @Override
    public void deleteNoteById(Long id) {
        log.info("Deleting note with ID: {}", id);

        if (noteRepository.existsById(id)) {
            log.info("Deleting note with ID: {}", id);
            throw  new NoteNotFoundException("Note no encontrada con ID: " + id);
        }
        noteRepository.deleteById(id);
        log.info("Note deleted with ID: {}", id);
    }

}
