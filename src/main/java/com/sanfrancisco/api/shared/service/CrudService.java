package com.sanfrancisco.api.shared.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Contrato base para servicios CRUD. Las implementaciones por módulo deben extender
 * este contrato con métodos de dominio (transiciones de estado, búsquedas, etc.).
 *
 * @param <R>   tipo del response DTO
 * @param <C>   tipo del create request DTO
 * @param <U>   tipo del update request DTO
 * @param <ID>  tipo de la clave primaria
 */
public interface CrudService<R, C, U, ID> {

    R create(C request);

    R update(ID id, U request);

    R findById(ID id);

    Page<R> findAll(Pageable pageable);

    void deleteById(ID id);
}
