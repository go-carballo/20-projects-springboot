package com.alec.working.repository;

import com.alec.working.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    /**
     * Buscar un contacto por su email
     * Spring Data genera: SELECT * FROM contacts WHERE email = ?
     * @param email el email del contacto a buscar
     * @return Optional con el contacto si existe, vacío si no
     */
    Optional<Contact> findByEmail(String email);

    /**
     * Verificar si existe un email en la DB
     * Más eficiente que findByEmail cuando solo necesitas un boolean
     * Spring Data genera: SELECT COUNT(*) > 0 FROM contacts WHERE email = ?
     * @param email el email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);
}
