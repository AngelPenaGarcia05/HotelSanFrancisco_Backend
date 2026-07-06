package com.sanfrancisco.api.shared.specification;

import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Helpers reutilizables para construir Specifications dinámicas.
 * No se aplican filtros cuando el valor es null o blank, lo que permite
 * encadenar predicados opcionales sin ramificación condicional en el caller.
 */
public final class SpecificationUtils {

    private SpecificationUtils() {
    }

    public static <T> Specification<T> equalsIfPresent(String attribute, Object value) {
        if (value == null) return Specification.unrestricted();
        if (value instanceof String s && s.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(resolve(root, attribute), value);
    }

    public static <T> Specification<T> likeIfPresent(String attribute, String value) {
        if (value == null || value.isBlank()) return Specification.unrestricted();
        String pattern = "%" + value.toLowerCase().trim() + "%";
        return (root, query, cb) -> cb.like(cb.lower(resolve(root, attribute)), pattern);
    }

    public static <T> Specification<T> dateBetween(String attribute, LocalDate from, LocalDate to) {
        if (from == null && to == null) return Specification.unrestricted();
        return (root, query, cb) -> {
            Path<LocalDate> path = resolve(root, attribute);
            if (from != null && to != null) return cb.between(path, from, to);
            if (from != null) return cb.greaterThanOrEqualTo(path, from);
            return cb.lessThanOrEqualTo(path, to);
        };
    }

    public static <T> Specification<T> dateTimeBetween(String attribute, LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) return Specification.unrestricted();
        return (root, query, cb) -> {
            Path<LocalDateTime> path = resolve(root, attribute);
            if (from != null && to != null) return cb.between(path, from, to);
            if (from != null) return cb.greaterThanOrEqualTo(path, from);
            return cb.lessThanOrEqualTo(path, to);
        };
    }

    /**
     * Rango de días de calendario sobre un atributo LocalDateTime.
     * El servidor fija los límites: desde las 00:00:00 de {@code from} (inclusive)
     * hasta el inicio del día siguiente a {@code to} (exclusivo). El límite superior
     * exclusivo evita el hueco clásico de "hasta 23:59:59", que deja fuera los
     * registros con fracción de segundo (p.ej. 23:59:59.500).
     */
    public static <T> Specification<T> dateTimeInDayRange(String attribute, LocalDate from, LocalDate to) {
        if (from == null && to == null) return Specification.unrestricted();
        LocalDateTime desde = from != null ? from.atStartOfDay() : null;
        LocalDateTime hastaExclusivo = to != null ? to.plusDays(1).atStartOfDay() : null;
        return (root, query, cb) -> {
            Path<LocalDateTime> path = resolve(root, attribute);
            if (desde != null && hastaExclusivo != null)
                return cb.and(cb.greaterThanOrEqualTo(path, desde), cb.lessThan(path, hastaExclusivo));
            if (desde != null) return cb.greaterThanOrEqualTo(path, desde);
            return cb.lessThan(path, hastaExclusivo);
        };
    }

    public static <T, V extends Comparable<? super V>> Specification<T> greaterOrEqual(String attribute, V value) {
        if (value == null) return Specification.unrestricted();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(resolve(root, attribute), value);
    }

    public static <T, V extends Comparable<? super V>> Specification<T> lessOrEqual(String attribute, V value) {
        if (value == null) return Specification.unrestricted();
        return (root, query, cb) -> cb.lessThanOrEqualTo(resolve(root, attribute), value);
    }

    @SuppressWarnings("unchecked")
    private static <T, V> Path<V> resolve(jakarta.persistence.criteria.Root<T> root, String attribute) {
        if (!attribute.contains(".")) return (Path<V>) root.get(attribute);
        String[] parts = attribute.split("\\.");
        Path<?> path = root.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            path = path.get(parts[i]);
        }
        return (Path<V>) path;
    }
}
