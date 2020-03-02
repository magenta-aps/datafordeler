package dk.magenta.datafordeler.cvr.data.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class for quarterly employees.
 */
@MappedSuperclass
public abstract class QuarterlyEmployeeNumbersData extends EmployeeNumbersData implements Comparable<QuarterlyEmployeeNumbersData> {

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

    //-------------------------------------------------------

    public static final String DB_FIELD_QUARTER = "quarter";
    public static final String IO_FIELD_QUARTER = "kvartal";

    @Column(name = DB_FIELD_QUARTER)
    @JsonProperty(value = IO_FIELD_QUARTER)
    @XmlElement(name = IO_FIELD_QUARTER)
    private int quarter;

    public int getQuarter() {
        return quarter;
    }

    public void setQuarter(int quarter) {
        this.quarter = quarter;
    }

    //-------------------------------------------------------


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put(DB_FIELD_YEAR, this.year);
        map.put(DB_FIELD_QUARTER, this.quarter);
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
        map.put(DB_FIELD_QUARTER, this.quarter);
        return map;
    }

    @Override
    public int compareTo(QuarterlyEmployeeNumbersData o) {
        if (o == null) return 1;
        if (this.year == o.year) {
            if (this.quarter == o.quarter) return 0;
            return this.quarter < o.quarter ? -1 : 1;
        }
        return this.year < o.year ? -1 : 1;
    }
}
