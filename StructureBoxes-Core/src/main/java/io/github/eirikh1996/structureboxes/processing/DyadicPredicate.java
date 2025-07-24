package io.github.eirikh1996.structureboxes.processing;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface DyadicPredicate<T, U> {
    Result validate(T t, U u);

    @Contract(pure = true)
    default @NotNull DyadicPredicate<T, U> or(DyadicPredicate<T, U> other) {
        return (t, u) -> {
            var result = this.validate(t, u);
            if (result.isSuccess()) {
                return result;
            }
            return other.validate(t, u);
        };
    }

    @Contract(pure = true)
    default @NotNull DyadicPredicate<T, U> and(DyadicPredicate<T, U> other) {
        return (t, u) -> {
            var result = this.validate(t, u);
            if (result.isFailure()) {
                return result;
            }
            return other.validate(t, u);
        };
    }

}
