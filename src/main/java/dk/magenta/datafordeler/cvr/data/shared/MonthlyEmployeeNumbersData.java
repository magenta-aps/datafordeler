package dk.magenta.datafordeler.cvr.data.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class for monthly employees.
 */
@MappedSuperclass
public abstract class MonthlyEmployeeNumbersData extends EmployeeNumbersData implements Comparable<MonthlyEmployeeNumbersData> {

    public static final String DB_FIELD_YEAR = "year";
    public static final String IO_FIELD_YEAR = "aar";

    @Column(name = DB_FIELD_YEAR)
    @JsonProperty(value = IO_FIELD_YEAR)
    @XmlElement(name = IO_FIELD_YEAR)
    private int year;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    //-----------------------------------------------

    public static final String DB_FIELD_MONTH = "month";
    public static final String IO_FIELD_MONTH = "maaned";

    @Column(name = DB_FIELD_MONTH)
    @JsonProperty(value = IO_FIELD_MONTH)
    @XmlElement(name = IO_FIELD_MONTH)
    private int month;

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    //-----------------------------------------------


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put(DB_FIELD_YEAR, this.year);
        map.put(DB_FIELD_MONTH, this.month);
        return map;
    }

    /**
     * Return a map of attributes, including those from the superclass
     * @return
     */
    @JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>(super.databaseFields());
        map.put(DB_FIELD_YEAR, this.year);
        map.put(DB_FIELD_MONTH, this.month);
        return map;
    }

    @Override
    public int compareTo(MonthlyEmployeeNumbersData o) {
        if (o == null) return 1;
        if (this.year == o.year) {
            if (this.month == o.month) return 0;
            return this.month < o.month ? -1 : 1;
        }
        return this.year < o.year ? -1 : 1;
    }
}
