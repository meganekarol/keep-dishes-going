package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Money {
    private final BigDecimal amount;

    // Common constants
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    // Private constructor - use factory methods
    private Money(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    // Factory methods
    public static Money of(BigDecimal amount) {
        validate(amount);
        return new Money(amount);
    }

    public static Money of(double amount) {
        return of(BigDecimal.valueOf(amount));
    }

    public static Money of(String amount) {
        return of(new BigDecimal(amount));
    }

    public static Money of(int amount) {
        return of(BigDecimal.valueOf(amount));
    }

    public static Money zero() {
        return ZERO;
    }

    // Validation
    private static void validate(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    // Arithmetic operations (all return new instances - immutability)
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Result cannot be negative");
        }
        return new Money(result);
    }

    public Money multiply(BigDecimal multiplier) {
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Multiplier cannot be negative");
        }
        return new Money(this.amount.multiply(multiplier));
    }

    public Money multiply(int multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("Multiplier cannot be negative");
        }
        return multiply(BigDecimal.valueOf(multiplier));
    }

    public Money divide(BigDecimal divisor) {
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Divisor must be positive");
        }
        return new Money(
                this.amount.divide(divisor, 2, RoundingMode.HALF_UP)
        );
    }

    public Money divide(int divisor) {
        if (divisor <= 0) {
            throw new IllegalArgumentException("Divisor must be positive");
        }
        return divide(BigDecimal.valueOf(divisor));
    }

    public Money percentage(BigDecimal percentage) {
        return multiply(percentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
    }

    public Money percentage(int percentage) {
        return percentage(BigDecimal.valueOf(percentage));
    }

    // Comparison operations
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isLessThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) <= 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    // Getters
    public BigDecimal getAmount() {
        return amount;
    }

    // Display formatting
    public String format() {
        return String.format("€%.2f", amount);
    }

    public String formatWithoutSymbol() {
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }

    // Value Object equality (based on value, not identity)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    public String toString() {
        return format();
    }
}
