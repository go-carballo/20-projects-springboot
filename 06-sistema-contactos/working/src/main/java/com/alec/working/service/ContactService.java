package com.alec.working.service;

import com.alec.working.dto.ContactCreateDTO;
import com.alec.working.dto.ContactResponseDTO;
import com.alec.working.dto.ContactUpdateDTO;
import com.alec.working.entity.Contact;
import com.alec.working.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    /**
     * Crear un nuevo contacto
     * @param createDTO datos del contacto a crear
     * @return ContactResponseDTO con el contacto creado
     * @throws IllegalArgumentException si el email ya existe
     */
    @Transactional
    public ContactResponseDTO create(ContactCreateDTO createDTO) {
        // Validar unicidad del email
        if (contactRepository.existsByEmail(createDTO.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado: " + createDTO.getEmail());
        }

        Contact contact = mapToEntity(createDTO);
        Contact saved = contactRepository.save(contact);
        return mapToResponseDTO(saved);
    }

    /**
     * Obtener todos los contactos
     * @return lista de ContactResponseDTO
     */
    @Transactional(readOnly = true)
    public List<ContactResponseDTO> findAll() {
        return contactRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Obtener un contacto por su ID
     * @param id el ID del contacto
     * @return ContactResponseDTO con el contacto encontrado
     * @throws IllegalArgumentException si no existe el contacto
     */
    @Transactional(readOnly = true)
    public ContactResponseDTO findById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado con ID: " + id));
        return mapToResponseDTO(contact);
    }

    /**
     * Buscar un contacto por su email
     * @param email el email del contacto
     * @return ContactResponseDTO con el contacto encontrado
     * @throws IllegalArgumentException si no existe el contacto
     */
    @Transactional(readOnly = true)
    public ContactResponseDTO findByEmail(String email) {
        Contact contact = contactRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado con email: " + email));
        return mapToResponseDTO(contact);
    }

    /**
     * Actualizar parcialmente un contacto (PATCH)
     * Solo actualiza los campos que no son null en el DTO
     * @param id el ID del contacto a actualizar
     * @param updateDTO datos a actualizar
     * @return ContactResponseDTO con el contacto actualizado
     * @throws IllegalArgumentException si no existe el contacto
     */
    @Transactional
    public ContactResponseDTO update(Long id, ContactUpdateDTO updateDTO) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado con ID: " + id));

        // Actualización parcial: solo campos presentes (no null)
        if (updateDTO.getFirstName() != null) {
            contact.setFirstName(updateDTO.getFirstName());
        }
        if (updateDTO.getLastName() != null) {
            contact.setLastName(updateDTO.getLastName());
        }
        if (updateDTO.getPhone() != null) {
            contact.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getAddress() != null) {
            contact.setAddress(updateDTO.getAddress());
        }
        if (updateDTO.getBirthDate() != null) {
            contact.setBirthDate(updateDTO.getBirthDate());
        }
        if (updateDTO.getNotes() != null) {
            contact.setNotes(updateDTO.getNotes());
        }

        Contact updated = contactRepository.save(contact);
        return mapToResponseDTO(updated);
    }

    /**
     * Eliminar un contacto por su ID
     * @param id el ID del contacto a eliminar
     * @throws IllegalArgumentException si no existe el contacto
     */
    @Transactional
    public void delete(Long id) {
        if (!contactRepository.existsById(id)) {
            throw new IllegalArgumentException("Contacto no encontrado con ID: " + id);
        }
        contactRepository.deleteById(id);
    }

    // ==================== MÉTODOS DE MAPEO ====================

    /**
     * Convertir ContactCreateDTO a Entity
     * @param dto el DTO de creación
     * @return Contact entity
     */
    private Contact mapToEntity(ContactCreateDTO dto) {
        Contact contact = new Contact();
        contact.setFirstName(dto.getFirstName());
        contact.setLastName(dto.getLastName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        contact.setAddress(dto.getAddress());
        contact.setBirthDate(dto.getBirthDate());
        contact.setNotes(dto.getNotes());
        return contact;
    }

    /**
     * Convertir Entity a ContactResponseDTO
     * @param contact la entidad Contact
     * @return ContactResponseDTO
     */
    private ContactResponseDTO mapToResponseDTO(Contact contact) {
        return new ContactResponseDTO(
                contact.getId(),
                contact.getFirstName(),
                contact.getLastName(),
                contact.getEmail(),
                contact.getPhone(),
                contact.getAddress(),
                contact.getBirthDate(),
                contact.getNotes()
        );
    }
}
