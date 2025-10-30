package be.kdg.keepdishesgoing.order.domain.valueObjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Value object representing monetary amount
 */
public record Money(BigDecimal amount, Currency currency) {
    
    public static final Money ZERO = new Money(BigDecimal.ZERO, Currency.getInstance("EUR"));
    
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        // Always use 2 decimal places for money
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
    
    public static Money euros(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("EUR"));
    }
    
    public static Money euros(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance("EUR"));
    }
    
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract money with different currencies");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
    
    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }
    
    public boolean isGreaterThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare money with different currencies");
        }
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

}