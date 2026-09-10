package com.kte.backend.utils;

import com.kte.backend.models.dto.request.TransactionRequest;
import com.kte.backend.models.entity.Product;
import com.kte.backend.models.entity.Supplier;
import com.kte.backend.models.entity.Transaction;
import com.kte.backend.models.enums.TransactionStatus;
import com.kte.backend.models.enums.TransactionType;
import com.kte.backend.repository.ProductRepository;
import com.kte.backend.services.authentication.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class TransactionsUtils {
    private final UserService userService;
    private final ProductRepository productRepository;

    private Transaction buildTransaction(final TransactionRequest request,
                                         final Product product,
                                         final Supplier supplier,
                                         final int quantity,
                                         final TransactionType type) {
        final BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        return Transaction.builder()
                .totalProducts(quantity)
                .totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .transactionType(type)
                .status(TransactionStatus.PENDING)
                .description(resolveDescription(request.description(), type, quantity, product))
                .user(userService.getCurrentLoggedInUser())
                .product(product)
                .supplier(supplier)
                .build();
    }

    private String resolveDescription(final String description,
                                      final TransactionType type,
                                      final int quantity,
                                      final Product product) {
        return StringUtils.hasText(description)
                ? description
                : "%s - %d x %s".formatted(type, quantity, product.getName());
    }
}
