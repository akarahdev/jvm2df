package dev.akarah.jvm2df.tree.instructions;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public record WithContext<T, C>(
        T value,
        C context
) {
    public <U> WithContext<U, C> map(Function<T, U> mapper) {
        return new WithContext<U, C>(
                mapper.apply(value),
                context
        );
    }

    public <U> WithContext<U, C> map(BiFunction<T, C, U> mapper) {
        return new WithContext<U, C>(
                mapper.apply(value, context),
                context
        );
    }

    public WithContext<T, C> inspect(BiConsumer<T, C> consumer) {
        consumer.accept(this.value, this.context);
        return this;
    }
}
