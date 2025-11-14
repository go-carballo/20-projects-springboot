package com.payoyo.gestor_notas_personales.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payoyo.gestor_notas_personales.entity.Note;
import com.payoyo.gestor_notas_personales.exception.NoteNotFoundException;
import com.payoyo.gestor_notas_personales.repository.NoteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * Implementacion del servicio de gestion de notas
 * Maneja la logica de negocio y las transaccionnes de las operaciones CRUD
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements INoteService{

    private final NoteRepository noteRepository;

    /*
     * {@inheritDoc} -> hereda la documentacion de la interfaz
     */
    @Override
    @Transactional
    public Note createNote(Note note) {
        log.info("Creando nueva nota con titulo: '{}'", note.getTitle());
        Note savedNote = noteRepository.save(note);
        log.info("Nota creada exitosamente con ID: {}", savedNote.getId());
        return savedNote;
    }

    /*
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Note updateNote(Long id, Note note) {
        log.info("Actualizando la nota coon ID: {}", id);

        Note existingNote = noteRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Intento de actualizar nota existente con ID: {}", id);
                return new NoteNotFoundException(id);
            });

        existingNote.setTitle(note.getTitle());
        existingNote.setContent(note.getContent());

        Note updatedNote = noteRepository.save(existingNote);
        log.info("Nota con ID: {} actualizada exitosamente", id);
        return updatedNote;
    }

    /*
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Note> getAllNotes() {
        log.info("Obteniendo todas las notas");
        List<Note> notes = noteRepository.findAll();
        log.info("Se encontraron {} nota(s)", notes.size());
        return notes;
    }

    /*
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Note getNoteById(Long id) {
        log.info("Buscando nota con ID: {}", id);
        return noteRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Nota no encontrada con id: {}", id);
                return new NoteNotFoundException(id);
            });
    }

    /*
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteNoteById(Long id) {
        log.info("Eliminando nota con ID: {}", id);

        if (!noteRepository.existsById(id)) {
            log.error("Intento de eliminar nota existente con ID: {}", id);
            throw new NoteNotFoundException(id);
        }

        noteRepository.deleteById(id);
        log.info("Nota con ID: {} eliminada exitosamente", id);
    }
    
}
