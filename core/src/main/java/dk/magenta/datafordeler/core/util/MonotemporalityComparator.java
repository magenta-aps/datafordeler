package dk.magenta.datafordeler.core.util;

import java.time.OffsetDateTime;
import java.util.Comparator;

public class MonotemporalityComparator implements Comparator<Monotemporality> {

    public enum Type {
        REGISTRATION_FROM,
        REGISTRATION_TO,
        ALL
    }

    private final Type type;

    public MonotemporalityComparator(Type type) {
        this.type = type;
    }

    private static final Comparator<OffsetDateTime> nullsFirst = Comparator.nullsFirst(OffsetDateTime.timeLineOrder());
    private static final Comparator<OffsetDateTime> nullsLast = Comparator.nullsLast(OffsetDateTime.timeLineOrder());

    private static final Type[] comparisonOrder = new Type[]{Type.REGISTRATION_FROM, Type.REGISTRATION_TO};

    @Override
    public int compare(Monotemporality o1, Monotemporality o2) {
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

    private int compareSingle(Monotemporality o1, Monotemporality o2, Type type) {
        switch (type) {
            case REGISTRATION_FROM:
                return nullsFirst.compare(o1.registrationFrom, o2.registrationFrom);
            case REGISTRATION_TO:
                return nullsLast.compare(o1.registrationTo, o2.registrationTo);
        }
        return 0;
    }

    public static final MonotemporalityComparator ALL = new MonotemporalityComparator(Type.ALL);
    public static final MonotemporalityComparator REGISTRATION_FROM = new MonotemporalityComparator(Type.REGISTRATION_FROM);
    public static final MonotemporalityComparator REGISTRATION_TO = new MonotemporalityComparator(Type.REGISTRATION_TO);

}
