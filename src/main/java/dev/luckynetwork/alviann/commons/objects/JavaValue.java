package dev.luckynetwork.alviann.commons.objects;

import kotlin.jvm.functions.Function0;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * when coding with kotlin sometimes we need to create a value
 * that has the same nullability as any normal Java value would be
 * <p>
 * therefore we're using the {@link JavaValue} to help us out with this issues
 */
@RequiredArgsConstructor
@Getter
public final class JavaValue<T> {

    /**
     * the java value value
     */
    private final T value;

    public static <T> T valueOf(T value) {
        return new JavaValue<T>(value).getValue();
    }

    public static <T> T valueFromBlock(Function0<T> block) {
        return block.invoke();
    }

}
