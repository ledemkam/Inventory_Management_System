package com.kte.backend.factory;

import com.kte.backend.exception.NameValueRequiredException;
import com.kte.backend.models.dto.request.TransactionRequest;
import com.kte.backend.models.entity.Product;
import com.kte.backend.models.entity.Supplier;
import com.kte.backend.models.entity.Transaction;
import com.kte.backend.models.enums.TransactionStatus;
import com.kte.backend.models.enums.TransactionType;
import com.kte.backend.repository.ProductRepository;
import com.kte.backend.services.authentication.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TransactionsFactory {
    private final UserService userService;
    private final ProductRepository productRepository;

    public Transaction buildTransaction(final TransactionRequest request,
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

    /**
     * Applies the stock change a transaction represents (called when it is completed).
     */
    public void applyStockMovement(final Transaction transaction) {
        final Product product = transaction.getProduct();
        final int quantity = transaction.getTotalProducts();

        switch (transaction.getTransactionType()) {
            case PURCHASE -> product.setStockQuantity(currentStock(product) + quantity);
            case SALE, RETURN_TO_SUPPLIER -> {
                ensureSufficientStock(product, quantity);
                product.setStockQuantity(currentStock(product) - quantity);
            }
        }
        productRepository.save(product);
    }

    private void ensureSufficientStock(final Product product, final int quantity) {
        final int available = currentStock(product);
        if (available < quantity) {
            throw new NameValueRequiredException(
                    "Insufficient stock for product %s: available %d, requested %d"
                            .formatted(product.getName(), available, quantity));
        }
    }

    private int currentStock(final Product product) {
        return product.getStockQuantity() == null ? 0 : product.getStockQuantity();
    }

    public void reverseStockMovement(final Transaction transaction) {
        final Product product = transaction.getProduct();
        final int quantity = transaction.getTotalProducts();

        switch (transaction.getTransactionType()) {
            case PURCHASE -> {
                ensureSufficientStock(product, quantity);
                product.setStockQuantity(currentStock(product) - quantity);
            }
            case SALE, RETURN_TO_SUPPLIER -> product.setStockQuantity(currentStock(product) + quantity);
        }
        productRepository.save(product);
    }

}
