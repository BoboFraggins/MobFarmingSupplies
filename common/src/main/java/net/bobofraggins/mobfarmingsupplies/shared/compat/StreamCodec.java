package net.bobofraggins.mobfarmingsupplies.shared.compat;

import net.minecraft.network.FriendlyByteBuf;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Minecraft 1.20.1 has no {@code net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec}; this is a cut-down
 * stand-in with the calls this mod uses, so packet and recipe code keeps the shape it has on the
 * newer branches. {@code B} is in practice always {@link net.minecraft.network.FriendlyByteBuf}.
 */
public interface StreamCodec<B, V> {

    V decode(B buf);

    void encode(B buf, V value);

    @FunctionalInterface
    interface Decoder<B, V> {
        V decode(B buf);
    }

    @FunctionalInterface
    interface Encoder<B, V> {
        void encode(B buf, V value);
    }

    @FunctionalInterface
    interface CodecOperation<B, S, T> {
        StreamCodec<B, T> apply(StreamCodec<B, S> codec);
    }

    static <B, V> StreamCodec<B, V> of(Encoder<B, V> encoder, Decoder<B, V> decoder) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buf) {
                return decoder.decode(buf);
            }

            @Override
            public void encode(B buf, V value) {
                encoder.encode(buf, value);
            }
        };
    }

    /** Like {@link #of} but with the encoder taking (value, buffer), as vanilla's {@code ofMember}. */
    static <B, V> StreamCodec<B, V> ofMember(BiConsumer<V, B> encoder, Decoder<B, V> decoder) {
        return of((buf, v) -> encoder.accept(v, buf), decoder);
    }

    static <B, V> StreamCodec<B, V> unit(V value) {
        return of((buf, v) -> {
            if (!v.equals(value)) throw new IllegalStateException("Can't encode '" + v + "', expected '" + value + "'");
        }, buf -> value);
    }

    default <O> StreamCodec<B, O> map(Function<? super V, ? extends O> to, Function<? super O, ? extends V> from) {
        StreamCodec<B, V> self = this;
        return of((buf, o) -> self.encode(buf, from.apply(o)), buf -> to.apply(self.decode(buf)));
    }

    default <O> StreamCodec<B, O> apply(CodecOperation<B, V, O> op) {
        return op.apply(this);
    }

    @SuppressWarnings("unchecked")
    default <S extends B> StreamCodec<S, V> cast() {
        return (StreamCodec<S, V>) this;
    }

    // --- composite ---------------------------------------------------------------------------

    @FunctionalInterface interface F3<A, B, C, R> { R apply(A a, B b, C c); }
    @FunctionalInterface interface F4<A, B, C, D, R> { R apply(A a, B b, C c, D d); }
    @FunctionalInterface interface F5<A, B, C, D, E, R> { R apply(A a, B b, C c, D d, E e); }
    @FunctionalInterface interface F6<A, B, C, D, E, F, R> { R apply(A a, B b, C c, D d, E e, F f); }

    static <B, C, T1> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> c1, Function<C, T1> g1,
            Function<T1, C> factory) {
        return of((buf, v) -> c1.encode(buf, g1.apply(v)), buf -> factory.apply(c1.decode(buf)));
    }

    static <B, C, T1, T2> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> c1, Function<C, T1> g1,
            StreamCodec<? super B, T2> c2, Function<C, T2> g2,
            java.util.function.BiFunction<T1, T2, C> factory) {
        return of((buf, v) -> {
            c1.encode(buf, g1.apply(v));
            c2.encode(buf, g2.apply(v));
        }, buf -> {
            T1 t1 = c1.decode(buf);
            T2 t2 = c2.decode(buf);
            return factory.apply(t1, t2);
        });
    }

    static <B, C, T1, T2, T3> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> c1, Function<C, T1> g1,
            StreamCodec<? super B, T2> c2, Function<C, T2> g2,
            StreamCodec<? super B, T3> c3, Function<C, T3> g3,
            F3<T1, T2, T3, C> factory) {
        return of((buf, v) -> {
            c1.encode(buf, g1.apply(v));
            c2.encode(buf, g2.apply(v));
            c3.encode(buf, g3.apply(v));
        }, buf -> {
            T1 t1 = c1.decode(buf);
            T2 t2 = c2.decode(buf);
            T3 t3 = c3.decode(buf);
            return factory.apply(t1, t2, t3);
        });
    }

    static <B, C, T1, T2, T3, T4> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> c1, Function<C, T1> g1,
            StreamCodec<? super B, T2> c2, Function<C, T2> g2,
            StreamCodec<? super B, T3> c3, Function<C, T3> g3,
            StreamCodec<? super B, T4> c4, Function<C, T4> g4,
            F4<T1, T2, T3, T4, C> factory) {
        return of((buf, v) -> {
            c1.encode(buf, g1.apply(v));
            c2.encode(buf, g2.apply(v));
            c3.encode(buf, g3.apply(v));
            c4.encode(buf, g4.apply(v));
        }, buf -> {
            T1 t1 = c1.decode(buf);
            T2 t2 = c2.decode(buf);
            T3 t3 = c3.decode(buf);
            T4 t4 = c4.decode(buf);
            return factory.apply(t1, t2, t3, t4);
        });
    }

    static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> c1, Function<C, T1> g1,
            StreamCodec<? super B, T2> c2, Function<C, T2> g2,
            StreamCodec<? super B, T3> c3, Function<C, T3> g3,
            StreamCodec<? super B, T4> c4, Function<C, T4> g4,
            StreamCodec<? super B, T5> c5, Function<C, T5> g5,
            F5<T1, T2, T3, T4, T5, C> factory) {
        return of((buf, v) -> {
            c1.encode(buf, g1.apply(v));
            c2.encode(buf, g2.apply(v));
            c3.encode(buf, g3.apply(v));
            c4.encode(buf, g4.apply(v));
            c5.encode(buf, g5.apply(v));
        }, buf -> {
            T1 t1 = c1.decode(buf);
            T2 t2 = c2.decode(buf);
            T3 t3 = c3.decode(buf);
            T4 t4 = c4.decode(buf);
            T5 t5 = c5.decode(buf);
            return factory.apply(t1, t2, t3, t4, t5);
        });
    }

    static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> c1, Function<C, T1> g1,
            StreamCodec<? super B, T2> c2, Function<C, T2> g2,
            StreamCodec<? super B, T3> c3, Function<C, T3> g3,
            StreamCodec<? super B, T4> c4, Function<C, T4> g4,
            StreamCodec<? super B, T5> c5, Function<C, T5> g5,
            StreamCodec<? super B, T6> c6, Function<C, T6> g6,
            F6<T1, T2, T3, T4, T5, T6, C> factory) {
        return of((buf, v) -> {
            c1.encode(buf, g1.apply(v));
            c2.encode(buf, g2.apply(v));
            c3.encode(buf, g3.apply(v));
            c4.encode(buf, g4.apply(v));
            c5.encode(buf, g5.apply(v));
            c6.encode(buf, g6.apply(v));
        }, buf -> {
            T1 t1 = c1.decode(buf);
            T2 t2 = c2.decode(buf);
            T3 t3 = c3.decode(buf);
            T4 t4 = c4.decode(buf);
            T5 t5 = c5.decode(buf);
            T6 t6 = c6.decode(buf);
            return factory.apply(t1, t2, t3, t4, t5, t6);
        });
    }
}
