package com.kte.backend.transaction.internal.repository;


import com.kte.backend.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findAllByUser_Id(String userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t " +
            "WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    Page<Transaction> findAllByMonthAndYear(@Param("month") int month, @Param("year") int year, Pageable pageable);


    //we are searching these fields: Transaction's description, status, Product's name, sku
    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN t.product p " +
            "WHERE (:searchText IS NULL OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(CAST(t.status AS string)) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    Page<Transaction> searchTransactions(@Param("searchText") String searchText, Pageable pageable);

}
