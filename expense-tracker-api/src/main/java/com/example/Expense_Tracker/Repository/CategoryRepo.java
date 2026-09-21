package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {
    @Query("""
    Select c from Category c
    Where c.userId is null
    or c.userId = :userId
""")
    List<Category> findAvailableCategories(@Param("userId") Long userId);

    @Query("""
    select c from Category c
    where (c.userId is null or c.userId = :userId)
     and c.type = :type
""")
    List<Category> findAvailableCategoryByType(@Param("userId")Long userId,
                                               @Param("type") Category.CategoryType type);

    @Query("""
        SELECT c
        FROM Category c
        WHERE c.id = :categoryId
          AND (c.userId IS NULL OR c.userId = :userId)
        """)
    Optional<Category> findAvailableCategory(
            @Param("categoryId") Long categoryId,
            @Param("userId") Long userId
    );
}