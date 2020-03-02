package dk.magenta.datafordeler.cvr.data.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cvr.data.DetailData;

import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract class for employee numbers.
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class EmployeeNumbersData extends DetailData {

    public static final String DB_FIELD_EMPLOYEES_LOW = "employeesLow";

    @JsonIgnore
    @XmlTransient
    @Column(name = DB_FIELD_EMPLOYEES_LOW)
    private Integer employeesLow;

    public int getEmployeesLow() {
        return employeesLow;
    }

    public void setEmployeesLow(Integer employeesLow) throws ParseException {
        if (employeesLow != null) {
            if (this.employeesHigh != null) {
                this.checkBounds(employeesLow, this.employeesHigh);
            }
            this.employeesLow = employeesLow;
        }
    }

    //-------------------------------------------

    public static final String DB_FIELD_EMPLOYEES_HIGH = "employeesHigh";

    @JsonIgnore
    @XmlTransient
    @Column(name = DB_FIELD_EMPLOYEES_HIGH)
    private Integer employeesHigh;

    public int getEmployeesHigh() {
        return employeesHigh;
    }

    public void setEmployeesHigh(Integer employeesHigh) throws ParseException {
        if (employeesHigh != null) {
            if (this.employeesLow != null) {
                this.checkBounds(this.employeesLow, employeesHigh);
            }
            this.employeesHigh = employeesHigh;
        }
    }

    //-------------------------------------------

    public static final String IO_FIELD_EMPLOYEES = "intervalAntalAnsatte";

    @JsonProperty(value = IO_FIELD_EMPLOYEES)
    @XmlElement(name = IO_FIELD_EMPLOYEES)
    public String getEmployeesInterval() {
        return this.formatInterval(this.employeesLow, this.employeesHigh);
    }

    //-------------------------------------------

    public static final String DB_FIELD_FULLTIME_LOW = "fullTimeEquivalentLow";

    @JsonIgnore
    @XmlTransient
    @Column(name = DB_FIELD_FULLTIME_LOW)
    private Integer fullTimeEquivalentLow;

    public int getFullTimeEquivalentLow() {
        return fullTimeEquivalentLow;
    }

    public void setFullTimeEquivalentLow(Integer fullTimeEquivalentLow) throws ParseException {
        if (fullTimeEquivalentLow != null) {
            if (this.fullTimeEquivalentHigh != null) {
                this.checkBounds(fullTimeEquivalentLow, this.fullTimeEquivalentHigh);
            }
            this.fullTimeEquivalentLow = fullTimeEquivalentLow;
        }
    }

    //-------------------------------------------

    public static final String DB_FIELD_FULLTIME_HIGH = "fullTimeEquivalentHigh";

    @JsonIgnore
    @XmlTransient
    @Column(name = DB_FIELD_FULLTIME_HIGH)
    private Integer fullTimeEquivalentHigh;

    public int getFullTimeEquivalentHigh() {
        return fullTimeEquivalentHigh;
    }

    public void setFullTimeEquivalentHigh(Integer fullTimeEquivalentHigh) throws ParseException {
        if (fullTimeEquivalentHigh != null) {
            if (this.fullTimeEquivalentLow != null) {
                this.checkBounds(this.fullTimeEquivalentLow, fullTimeEquivalentHigh);
            }
            this.fullTimeEquivalentHigh = fullTimeEquivalentHigh;
        }
    }

    //-------------------------------------------

    public static final String IO_FIELD_FULLTIME = "intervalAntalÅrsværk";

    @JsonProperty(value = IO_FIELD_FULLTIME)
    @XmlElement(name = IO_FIELD_FULLTIME)
    public String getFullTimeEquivalentInterval() {
        return this.formatInterval(this.fullTimeEquivalentLow, this.fullTimeEquivalentHigh);
    }

    //-------------------------------------------

    public static final String DB_FIELD_INCL_OWNERS_LOW = "includingOwnersLow";

    @JsonIgnore
    @XmlTransient
    @Column(name = DB_FIELD_INCL_OWNERS_LOW)
    private Integer includingOwnersLow;

    public int getIncludingOwnersLow() {
        return includingOwnersLow;
    }

    public void setIncludingOwnersLow(Integer includingOwnersLow) throws ParseException {
        if (includingOwnersLow != null) {
            if (this.includingOwnersHigh != null) {
                this.checkBounds(includingOwnersLow, this.includingOwnersHigh);
            }
            this.includingOwnersLow = includingOwnersLow;
        }
    }

    //-------------------------------------------

    public static final String DB_FIELD_INCL_OWNERS_HIGH = "includingOwnersHigh";

    @JsonIgnore
    @XmlTransient
    @Column(name = DB_FIELD_INCL_OWNERS_HIGH)
    private Integer includingOwnersHigh;

    public int getIncludingOwnersHigh() {
        return includingOwnersHigh;
    }

    public void setIncludingOwnersHigh(Integer includingOwnersHigh) throws ParseException {
        if (includingOwnersHigh != null) {
            if (this.includingOwnersLow != null) {
                this.checkBounds(this.includingOwnersLow, includingOwnersHigh);
            }
            this.includingOwnersHigh = includingOwnersHigh;
        }
    }

    //-------------------------------------------

    public static final String IO_FIELD_INCL_OWNERS = "intervalAntalInklusivEjere";

    @JsonProperty(value = IO_FIELD_INCL_OWNERS)
    @XmlElement(name = IO_FIELD_INCL_OWNERS)
    public String getIncludingOwnersInterval() {
        return this.formatInterval(this.includingOwnersLow, this.includingOwnersHigh);
    }

    //-------------------------------------------

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("employeesInterval", this.getEmployeesInterval());
        map.put("fullTimeEquivalentInterval", this.getFullTimeEquivalentInterval());
        map.put("includingOwnersInterval", this.getIncludingOwnersInterval());
        return map;
    }

    /**
     * Return a map of attributes, including those from the superclass
     * @return
     */
    @JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_EMPLOYEES_LOW, this.employeesLow);
        map.put(DB_FIELD_EMPLOYEES_HIGH, this.employeesHigh);
        map.put(DB_FIELD_FULLTIME_LOW, this.fullTimeEquivalentLow);
        map.put(DB_FIELD_FULLTIME_HIGH, this.fullTimeEquivalentHigh);
        map.put(DB_FIELD_INCL_OWNERS_LOW, this.includingOwnersLow);
        map.put(DB_FIELD_INCL_OWNERS_HIGH, this.includingOwnersHigh);
        return map;
    }



    protected void checkBounds(int low, int high) throws ParseException {
        if (high < low) {
            throw new ParseException("Upper bound must not be less than lower bound (low: "+low+", high: "+high+")");
        }
        if (low < 0) {
            throw new ParseException("Lower bound must not be less than zero (low: "+low+")");
        }
    }

    protected String formatInterval(Integer low, Integer high) {
        if (low == null || high == null) {
            return null;
        }
        if (low == high) {
            return Integer.toString(low);
        }
        return low + " - " + high;
    }

    private static Pattern intervalPattern = Pattern.compile("^(\\d+)\\s*-\\s*(\\d+)$");

    protected int parseLow(String interval) throws ParseException {
        try {
            return Integer.parseInt(interval);
        } catch (NumberFormatException e) {
            Matcher m = intervalPattern.matcher(interval);
            if (m.matches()) {
                return Integer.parseInt(m.group(1));
            }
            throw new ParseException("Cannot parse interval "+interval+", needing to match "+intervalPattern.toString());
        }
    }

    protected int parseHigh(String interval) throws ParseException {
        Matcher m = intervalPattern.matcher(interval);
        if (m.matches()) {
            return Integer.parseInt(m.group(2));
        }
        throw new ParseException("Cannot parse interval "+interval+", needing to match "+intervalPattern.toString());
    }

}
