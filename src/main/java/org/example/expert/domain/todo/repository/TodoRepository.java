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

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);

    //weather를 기준으로 일정 전체 검색
    @Query("select t from Todo t where t.weather = :weather")
    Page<Todo> findAllByWeather(@Param("weather") String weather, Pageable pageable);

    //수정일을 기준으로 특정 기간 일정 찾기
    @Query("select t from Todo  t where t.modifiedAt >= :startDate And t.modifiedAt<= :endDate order by t.modifiedAt desc")
    Page<Todo> findSchedulesBetweenDates(Pageable pageable,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

}
