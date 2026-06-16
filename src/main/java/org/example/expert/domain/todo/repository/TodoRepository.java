package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    // 검색 조건이 null이면 해당 조건을 건너뛰고, 값이 있으면 날씨와 수정일 범위로 필터링합니다.
    @Query(value = "SELECT t FROM Todo t " +
            "LEFT JOIN FETCH t.user u " +
            "WHERE (:weather IS NULL OR t.weather = :weather) " +
            "AND (:startDateTime IS NULL OR t.modifiedAt >= :startDateTime) " +
            "AND (:endDateTime IS NULL OR t.modifiedAt < :endDateTime) " +
            "ORDER BY t.modifiedAt DESC",
            countQuery = "SELECT COUNT(t) FROM Todo t " +
                    "WHERE (:weather IS NULL OR t.weather = :weather) " +
                    "AND (:startDateTime IS NULL OR t.modifiedAt >= :startDateTime) " +
                    "AND (:endDateTime IS NULL OR t.modifiedAt < :endDateTime)")
    Page<Todo> searchTodos(
            @Param("weather") String weather,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable
    );

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
