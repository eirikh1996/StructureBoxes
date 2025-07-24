package io.github.eirikh1996.structureboxes.processing;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface public interface MonadicPredicate<T> {
    
    @Contract(pure = true)
    @NotNull Result validate(@NotNull T t);

    @Contract(pure = true)
    default @NotNull MonadicPredicate<T> or(@NotNull MonadicPredicate<T> other) {
        return t -> {
            var result = this.validate(t);
            if (result.isSuccess()) {
                return result;
            }
            return other.validate(t);
        };
    }
    @Contract(pure = true)
    default @NotNull MonadicPredicate<T> and(@NotNull MonadicPredicate<T> other) {
        return t -> {
            var result = this.validate(t);
            if (result.isFailure()) {
                return result;
            }
            return other.validate(t);
        };
    }
}
