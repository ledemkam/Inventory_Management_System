package com.kte.backend.mapper;

import com.kte.backend.models.dto.request.TransactionRequest;
import com.kte.backend.models.dto.response.TransactionResponse;
import com.kte.backend.models.entity.Product;
import com.kte.backend.models.entity.Supplier;
import com.kte.backend.models.entity.Transaction;
import com.kte.backend.models.entity.User;
import com.kte.backend.models.enums.TransactionStatus;
import com.kte.backend.models.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("TransactionMapper Test")
class TransactionMapperTest {

    @Autowired
    private TransactionMapper transactionMapper;

    @Test
    @DisplayName("Test entity to DTO mapping")
    void entity_To_Dto() {
        //Given
        Product product = Product.builder()
                .id("1L")
                .name("Laptop")
                .build();

        Supplier supplier = Supplier.builder()
                .id("1L")
                .name("Tech Supplies")
                .build();

        User user = User.builder()
                .id("1L")
                .name("John Doe")
                .build();

        Transaction transaction = Transaction.builder()
                .id("1L")
                .totalProducts(5)
                .totalPrice(BigDecimal.valueOf(500))
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.PENDING)
                .description("Purchase of laptops")
                .user(user)
                .product(product)
                .supplier(supplier)
                .build();

        //When
        TransactionResponse dto = transactionMapper.entityToDto(transaction);

        //Then
        assertNotNull(dto);
        assertEquals(5, dto.totalProducts());
        assertEquals(BigDecimal.valueOf(500), dto.totalPrice());
        assertEquals(TransactionType.PURCHASE, dto.transactionType());
        assertEquals(TransactionStatus.PENDING, dto.status());
        assertEquals("Purchase of laptops", dto.description());
        assertNotNull(dto.user());
        assertNotNull(dto.product());
        assertNotNull(dto.supplier());
    }

    @Test
    @DisplayName("Test DTO to entity mapping")
    void dto_To_Entity() {
        //Given
        TransactionRequest transactionRequest = TransactionRequest.builder()
                .productId("1L")
                .quantity(5)
                .supplierId("1L")
                .description("Purchase of laptops")
                .build();

        //When
        Transaction entity = transactionMapper.dtoToEntity(transactionRequest);

        //Then
        assertNotNull(entity);
        assertEquals(transactionRequest.description(), entity.getDescription());
    }

    @Test
    @DisplayName("Test list of entities to list of DTOs mapping")
    void to_Dto_List() {
        //Given
        Transaction transaction1 = Transaction.builder()
                .totalProducts(5)
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.PENDING)
                .description("Purchase of laptops")
                .build();

        Transaction transaction2 = Transaction.builder()
                .totalProducts(2)
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .description("Sale of mice")
                .build();

        //When
        List<TransactionResponse> dtoList = transactionMapper.toDtoList(List.of(transaction1, transaction2));

        //Then
        assertNotNull(dtoList);
        assertEquals(2, dtoList.size());
        assertEquals("Purchase of laptops", dtoList.get(0).description());
        assertEquals("Sale of mice", dtoList.get(1).description());
    }

    @Test
    @DisplayName("Test updating entity from DTO")
    void update_Entity_From_Dto() {
        //Given
        Transaction transaction = Transaction.builder()
                .id("1L")
                .totalProducts(5)
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.PENDING)
                .description("Purchase of laptops")
                .build();

        TransactionRequest transactionRequest = TransactionRequest.builder()
                .productId("1L")
                .quantity(10)
                .description("Updated purchase description")
                .build();

        //When
        transactionMapper.updateEntityFromDto(transactionRequest, transaction);

        //Then
        assertEquals("Updated purchase description", transaction.getDescription());
        assertEquals("1L", transaction.getId());
    }
}