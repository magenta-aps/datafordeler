package dk.magenta.datafordeler.core.util;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Objects;

public class BitemporalityComparator<B extends Bitemporality> implements Comparator<B> {

    public enum Type {
        REGISTRATION_FROM,
        REGISTRATION_TO,
        EFFECT_FROM,
        EFFECT_TO,
        ALL
    }

    private final Type type;

    public BitemporalityComparator(Type type) {
        this.type = type;
    }

    private static final Comparator<OffsetDateTime> nullsFirst = Comparator.nullsFirst(OffsetDateTime.timeLineOrder());
    private static final Comparator<OffsetDateTime> nullsLast = Comparator.nullsLast(OffsetDateTime.timeLineOrder());

    private static final Type[] comparisonOrder = new Type[]{Type.REGISTRATION_FROM, Type.REGISTRATION_TO, Type.EFFECT_FROM, Type.EFFECT_TO};

    @Override
    public int compare(Bitemporality o1, Bitemporality o2) {
        if (this.type == Type.ALL) {
            int c;
            for (Type t : comparisonOrder) {
                if (t != Type.ALL) {
                    c = this.compareSingle(o1, o2, t);
                    if (c != 0) {
                        return c;
                    }
                }
            }
        }
        return this.compareSingle(o1, o2, this.type);
    }

    private int compareSingle(Bitemporality o1, Bitemporality o2, Type type) {
        switch (type) {
            case REGISTRATION_FROM:
                return nullsFirst.compare(o1.registrationFrom, o2.registrationFrom);
            case REGISTRATION_TO:
                return nullsLast.compare(o1.registrationTo, o2.registrationTo);
            case EFFECT_FROM:
                return nullsFirst.compare(o1.effectFrom, o2.effectFrom);
            case EFFECT_TO:
                return nullsLast.compare(o1.effectTo, o2.effectTo);
        }
        return 0;
    }

    public BitemporalityComparator<B> thenComparing(BitemporalityComparator<? super B> other) {
        Objects.requireNonNull(other);
        return new BitemporalityComparator<B>(this.type) {
            @Override
            public int compare(Bitemporality c1, Bitemporality c2) {
                int res = super.compare(c1, c2);
                return (res != 0) ? res : other.compare(c1, c2);
            }
        };
    }

    public static final BitemporalityComparator<Bitemporality> ALL = new BitemporalityComparator<>(Type.ALL);
    public static final BitemporalityComparator<Bitemporality> REGISTRATION_FROM = registrationFrom(Bitemporality.class);
    public static final BitemporalityComparator<Bitemporality> REGISTRATION_TO = registrationTo(Bitemporality.class);
    public static final BitemporalityComparator<Bitemporality> EFFECT_FROM = effectFrom(Bitemporality.class);
    public static final BitemporalityComparator<Bitemporality> EFFECT_TO = effectTo(Bitemporality.class);

    public static final BitemporalityComparator<Bitemporality> REGISTRATION = REGISTRATION_FROM.thenComparing(REGISTRATION_TO);
    public static final BitemporalityComparator<Bitemporality> EFFECT = EFFECT_FROM.thenComparing(EFFECT_TO);


    public static <T extends Bitemporality> BitemporalityComparator<T> registrationFrom(Class<T> bitemporalityClass) {
        return new BitemporalityComparator<T>(Type.REGISTRATION_FROM);
    }

    public static <B extends Bitemporality> BitemporalityComparator<B> registrationTo(Class<B> bitemporalityClass) {
        return new BitemporalityComparator<B>(Type.REGISTRATION_TO);
    }

    public static <B extends Bitemporality> BitemporalityComparator<B> registration(Class<B> bitemporalityClass) {
        return registrationFrom(bitemporalityClass).thenComparing(registrationTo(bitemporalityClass));
    }

    public static <B extends Bitemporality> BitemporalityComparator<B> effectFrom(Class<B> bitemporalityClass) {
        return new BitemporalityComparator<B>(Type.EFFECT_FROM);
    }

    public static <B extends Bitemporality> BitemporalityComparator<B> effectTo(Class<B> bitemporalityClass) {
        return new BitemporalityComparator<B>(Type.EFFECT_FROM);
    }

    public static <B extends Bitemporality> BitemporalityComparator<B> effect(Class<B> bitemporalityClass) {
        return effectFrom(bitemporalityClass).thenComparing(effectTo(bitemporalityClass));
    }

    public static <B extends Bitemporality> BitemporalityComparator<B> all(Class<B> bitemporalityClass) {
        return new BitemporalityComparator<B>(Type.ALL);
    }

}
