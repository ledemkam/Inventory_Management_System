package com.kte.backend.repository;

import com.kte.backend.models.entity.Category;
import com.kte.backend.models.entity.Product;
import com.kte.backend.models.entity.Transaction;
import com.kte.backend.models.enums.TransactionStatus;
import com.kte.backend.models.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TransactionRepository Test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product saveProduct(String name, String sku) {
        Category category = categoryRepository.save(
                Category.builder()
                        .name("Category for " + name)
                        .build()
        );
        return productRepository.save(
                Product.builder()
                        .name(name)
                        .sku(sku)
                        .price(new BigDecimal("15.0"))
                        .stockQuantity(50)
                        .category(category)
                        .description("Test product description")
                        .build()
        );
    }

    private Transaction saveTransaction(Product product, String description, TransactionStatus status) {
        return transactionRepository.save(
                Transaction.builder()
                        .totalProducts(5)
                        .totalPrice(new BigDecimal("75.0"))
                        .transactionType(TransactionType.SALE)
                        .status(status)
                        .description(description)
                        .product(product)
                        .build()
        );
    }

    @Test
    @DisplayName("Find transactions created in the current month and year")
    void findAllByMonthAndYear_ReturnsTransaction_WhenCreatedInGivenMonthAndYear() {
        //Given
        Product product = saveProduct("Wireless Mouse", "WM-001");
        saveTransaction(product, "Monthly stock sale", TransactionStatus.COMPLETED);
        LocalDateTime now = LocalDateTime.now();

        //When
        Page<Transaction> result = transactionRepository.findAllByMonthAndYear(
                now.getMonthValue(), now.getYear(), PageRequest.of(0, 10));

        //Then
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("Return empty page when no transaction matches the given month and year")
    void findAllByMonthAndYear_ReturnsEmpty_WhenNoTransactionMatches() {
        //Given
        Product product = saveProduct("Keyboard", "KB-001");
        saveTransaction(product, "Old sale", TransactionStatus.COMPLETED);

        //When (an obviously non-matching month/year)
        Page<Transaction> result = transactionRepository.findAllByMonthAndYear(1, 2000, PageRequest.of(0, 10));

        //Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Search transactions by product name")
    void searchTransactions_ReturnsTransaction_WhenProductNameMatches() {
        //Given
        Product product = saveProduct("Gaming Chair", "GC-001");
        saveTransaction(product, "Office furniture sale", TransactionStatus.PENDING);

        //When
        Page<Transaction> result = transactionRepository.searchTransactions("gaming", PageRequest.of(0, 10));

        //Then
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("Search transactions by product sku")
    void searchTransactions_ReturnsTransaction_WhenProductSkuMatches() {
        //Given
        Product product = saveProduct("Desk Lamp", "DL-001");
        saveTransaction(product, "Standard sale", TransactionStatus.PROCESSING);

        //When
        Page<Transaction> result = transactionRepository.searchTransactions("dl-001", PageRequest.of(0, 10));

        //Then
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("Search transactions by description")
    void searchTransactions_ReturnsTransaction_WhenDescriptionMatches() {
        //Given
        Product product = saveProduct("Monitor", "MN-001");
        saveTransaction(product, "Bulk warehouse restock", TransactionStatus.PROCESSING);

        //When
        Page<Transaction> result = transactionRepository.searchTransactions("restock", PageRequest.of(0, 10));

        //Then
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("Return all transactions when search text is null")
    void searchTransactions_ReturnsAll_WhenSearchTextIsNull() {
        //Given
        Product product = saveProduct("Webcam", "WC-001");
        saveTransaction(product, "Standard sale", TransactionStatus.COMPLETED);

        //When
        Page<Transaction> result = transactionRepository.searchTransactions(null, PageRequest.of(0, 10));

        //Then
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("Return empty page when search text matches nothing")
    void searchTransactions_ReturnsEmpty_WhenNoMatch() {
        //Given
        Product product = saveProduct("Router", "RT-001");
        saveTransaction(product, "Standard sale", TransactionStatus.COMPLETED);

        //When
        Page<Transaction> result = transactionRepository.searchTransactions("nonexistent-term", PageRequest.of(0, 10));

        //Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Find transaction by id")
    void find_Transaction_By_Id() {
        //Given
        Product product = saveProduct("Router", "RT-001");
        Transaction transaction = saveTransaction(product, "Standard sale", TransactionStatus.COMPLETED);

        //When
        Optional<Transaction> foundTransaction = transactionRepository.findById(transaction.getId());

        //Then
        assertThat(foundTransaction).isPresent();
        assertThat(foundTransaction.get().getId()).isEqualTo(transaction.getId());
    }

    @Test
    @DisplayName("Delete transaction by id")
    void delete_Transaction_By_Id() {
        //Given
        Product product = saveProduct("Router", "RT-001");
        Transaction transaction = saveTransaction(product, "Standard sale", TransactionStatus.COMPLETED);

        //When
        transactionRepository.deleteById(transaction.getId());

        //Then
        Optional<Transaction> deletedTransaction = transactionRepository.findById(transaction.getId());
        assertThat(deletedTransaction).isEmpty();
    }
}