package be.kdg.keepdishesgoing.order.domain.valueObjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * An amount of money in a given currency.
 *
 * <p>Always rounded to two decimal places.
 *
 * @param amount   how much
 * @param currency which currency it is in
 */
public record Money(BigDecimal amount, Currency currency) {

    /** No money at all, in euros. */
    public static final Money ZERO = new Money(BigDecimal.ZERO, Currency.getInstance("EUR"));

    /** @throws IllegalArgumentException if the amount or the currency is missing */
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Builds an amount in euros.
     *
     * @param amount how much
     * @return that amount in euros
     */
    public static Money euros(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("EUR"));
    }

    /**
     * Builds an amount in euros.
     *
     * @param amount how much
     * @return that amount in euros
     */
    public static Money euros(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance("EUR"));
    }

    /**
     * Adds another amount to this one.
     *
     * @param other the amount to add
     * @return the total
     * @throws IllegalArgumentException if the two are in different currencies
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Takes another amount off this one.
     *
     * @param other the amount to subtract
     * @return what is left
     * @throws IllegalArgumentException if the two are in different currencies
     */
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract money with different currencies");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * Multiplies this amount.
     *
     * @param multiplier how many times
     * @return the multiplied amount
     */
    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }

    /**
     * Compares this amount with another.
     *
     * @param other the amount to compare against
     * @return {@code true} if this one is bigger
     * @throws IllegalArgumentException if the two are in different currencies
     */
    public boolean isGreaterThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare money with different currencies");
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Checks whether this is nothing at all.
     *
     * @return {@code true} if the amount is zero
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
}
