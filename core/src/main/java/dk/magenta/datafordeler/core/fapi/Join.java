package dk.magenta.datafordeler.core.fapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Join {
    private final String base;
    private final String member;
    private final String alias;

    public Join(String base, String member) {
        this.base = base;
        this.member = member;
        this.alias = base + "__" + member;
    }

    public String getBase() {
        return this.base;
    }

    public String getMember() {
        return this.member;
    }

    public String getAlias() {
        return this.alias;
    }

    public static List<Join> fromPath(String path, boolean lastIsField) {
        // entity.foo.bar.baz
        ArrayList<Join> joins = new ArrayList<>();
        String[] parts = path.split("\\.");
        int r = lastIsField ? 2 : 1;
        String base = parts[0];
        for (int i = 0; i < parts.length - r; i++) {
            Join join = new Join(base, parts[i + 1]);
            base = join.getAlias();
            joins.add(join);
        }
        return joins;
    }

    public String toHql() {
        return "LEFT JOIN " + this.base + "." + this.member + " " + this.alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Join join = (Join) o;
        return base.equals(join.base) && member.equals(join.member);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, member);
    }
}
