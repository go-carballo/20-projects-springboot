package com.alec.working.controller;

import com.alec.working.dto.ContactCreateDTO;
import com.alec.working.dto.ContactResponseDTO;
import com.alec.working.dto.ContactUpdateDTO;
import com.alec.working.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    /**
     * Crear un nuevo contacto
     * POST /api/contacts
     * @param createDTO datos del contacto a crear
     * @return ContactResponseDTO con status 201 Created
     */
    @PostMapping
    public ResponseEntity<ContactResponseDTO> create(@Valid @RequestBody ContactCreateDTO createDTO) {
        ContactResponseDTO response = contactService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener todos los contactos
     * GET /api/contacts
     * @return Lista de ContactResponseDTO con status 200 OK
     */
    @GetMapping
    public ResponseEntity<List<ContactResponseDTO>> findAll() {
        List<ContactResponseDTO> contacts = contactService.findAll();
        return ResponseEntity.ok(contacts);
    }

    /**
     * Obtener un contacto por su ID
     * GET /api/contacts/{id}
     * @param id el ID del contacto
     * @return ContactResponseDTO con status 200 OK
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContactResponseDTO> findById(@PathVariable Long id) {
        ContactResponseDTO response = contactService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Buscar un contacto por su email
     * GET /api/contacts/email/{email}
     * @param email el email del contacto
     * @return ContactResponseDTO con status 200 OK
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ContactResponseDTO> findByEmail(@PathVariable String email) {
        ContactResponseDTO response = contactService.findByEmail(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar parcialmente un contacto (PATCH)
     * PATCH /api/contacts/{id}
     * @param id el ID del contacto a actualizar
     * @param updateDTO datos a actualizar
     * @return ContactResponseDTO con status 200 OK
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ContactResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ContactUpdateDTO updateDTO) {
        ContactResponseDTO response = contactService.update(id, updateDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar un contacto
     * DELETE /api/contacts/{id}
     * @param id el ID del contacto a eliminar
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
