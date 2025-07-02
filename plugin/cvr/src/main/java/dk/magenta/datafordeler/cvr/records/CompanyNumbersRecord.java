package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract record for Company and CompanyUnit employee numbers.
 */
@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CompanyNumbersRecord extends CvrBitemporalDataRecord implements Cloneable {

    private static class Range {
        public int low;
        public int high;

        public Range(int low, int high) {
            this.low = low;
            this.high = high;
        }
    }

    public static final String DB_FIELD_EMPLOYEES_BASE = "employees";
    public static final String IO_FIELD_EMPLOYEES_BASE = "antalAnsatte";

    @Column(name = DB_FIELD_EMPLOYEES_BASE)
    @JsonProperty(value = IO_FIELD_EMPLOYEES_BASE)
    private Integer employees;

    public void setEmployees(Integer employees) {
        this.employees = employees;
    }

    public static final String DB_FIELD_EMPLOYEES_LOW = "employeeLow";
    public static final String IO_FIELD_EMPLOYEES_LOW = "antalAnsatteMin";

    @Column(name = DB_FIELD_EMPLOYEES_LOW)
    @JsonProperty(value = IO_FIELD_EMPLOYEES_LOW)
    private Integer employeeLow;

    public void setEmployeeLow(Integer employeeLow) {
        this.employeeLow = employeeLow;
    }

    public static final String DB_FIELD_EMPLOYEES_HIGH = "employeeHigh";
    public static final String IO_FIELD_EMPLOYEES_HIGH = "antalAnsatteMax";

    @Column(name = DB_FIELD_EMPLOYEES_HIGH)
    @JsonProperty(value = IO_FIELD_EMPLOYEES_HIGH)
    private Integer employeeHigh;

    public void setEmployeeHigh(Integer employeeHigh) {
        this.employeeHigh = employeeHigh;
    }

    @JsonProperty(value = "intervalKodeAntalAnsatte")
    public void setEmployeeRange(String range) {
        Range parsed = parseRange(range);
        if (parsed != null) {
            this.employeeLow = parsed.low;
            this.employeeHigh = parsed.high;
        }
    }

    @JsonProperty(value = "intervalKodeAntalAnsatte")
    public String getEmployeeRange() {
        if (this.employeeLow != null && this.employeeHigh != null) {
            return "ANTAL_" + this.employeeLow + "_" + this.employeeHigh;
        }
        return null;
    }

    public Integer getEmployeeLow() {
        return this.employeeLow;
    }

    public Integer getEmployeeHigh() {
        return this.employeeHigh;
    }


    @JsonProperty(value = "antalAarsvaerk")
    private Integer fulltimeEquivalent;

    public void setFulltimeEquivalent(Integer fulltimeEquivalent) {
        this.fulltimeEquivalent = fulltimeEquivalent;
    }

    @JsonProperty(value = "antalAarsvaerkMin")
    private Integer fulltimeEquivalentLow;

    public void setFulltimeEquivalentLow(Integer fulltimeEquivalentLow) {
        this.fulltimeEquivalentLow = fulltimeEquivalentLow;
    }

    @JsonProperty(value = "antalAarsvaerkMax")
    private Integer fulltimeEquivalentHigh;

    public void setFulltimeEquivalentHigh(Integer fulltimeEquivalentHigh) {
        this.fulltimeEquivalentHigh = fulltimeEquivalentHigh;
    }

    @JsonProperty(value = "intervalKodeAntalAarsvaerk")
    public void setFulltimeEquivalentRange(String range) {
        Range parsed = parseRange(range);
        if (parsed != null) {
            this.fulltimeEquivalentLow = parsed.low;
            this.fulltimeEquivalentHigh = parsed.high;
        }
    }

    @JsonProperty(value = "intervalKodeAntalAarsvaerk")
    public String getFulltimeEquivalentRange() {
        if (this.fulltimeEquivalentLow != null && this.fulltimeEquivalentHigh != null) {
            return "ANTAL_" + this.fulltimeEquivalentLow + "_" + this.fulltimeEquivalentHigh;
        }
        return null;
    }

    public Integer getFulltimeEquivalentLow() {
        return this.fulltimeEquivalentLow;
    }

    public Integer getFulltimeEquivalentHigh() {
        return this.fulltimeEquivalentHigh;
    }


    @JsonProperty(value = "antalInklusivEjere")
    private Integer includingOwners;

    public void setIncludingOwners(Integer includingOwners) {
        this.includingOwners = includingOwners;
    }

    @JsonProperty(value = "antalInklusivEjereMin")
    private Integer includingOwnersLow;

    public void setIncludingOwnersLow(Integer includingOwnersLow) {
        this.includingOwnersLow = includingOwnersLow;
    }

    @JsonProperty(value = "antalInklusivEjereMax")
    private Integer includingOwnersHigh;

    public void setIncludingOwnersHigh(Integer includingOwnersHigh) {
        this.includingOwnersHigh = includingOwnersHigh;
    }

    @JsonProperty(value = "intervalKodeAntalInklusivEjere")
    public void setIncludingOwnersRange(String range) {
        Range parsed = parseRange(range);
        if (parsed != null) {
            this.includingOwnersLow = parsed.low;
            this.includingOwnersHigh = parsed.high;
        }
    }

    @JsonProperty(value = "intervalKodeAntalInklusivEjere")
    public String getIncludingOwnersRange() {
        if (this.includingOwnersLow != null && this.includingOwnersHigh != null) {
            return "ANTAL_" + this.includingOwnersLow + "_" + this.includingOwnersHigh;
        }
        return null;
    }

    public Integer getIncludingOwnersLow() {
        return this.includingOwnersLow;
    }

    public Integer getIncludingOwnersHigh() {
        return this.includingOwnersHigh;
    }

    private static Pattern rangePattern = Pattern.compile("^ANTAL_(\\d+)_(\\d+)$");

    private static Range parseRange(String range) {
        if (range != null) {
            Matcher m = rangePattern.matcher(range);
            if (m.find()) {
                return new Range(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
            }
        }
        return null;
    }


    public LocalDate getValidFrom() {
        return null;
    }

    public LocalDate getValidTo() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CompanyNumbersRecord that = (CompanyNumbersRecord) o;
        return Objects.equals(employees, that.employees) &&
                Objects.equals(employeeLow, that.employeeLow) &&
                Objects.equals(employeeHigh, that.employeeHigh) &&
                Objects.equals(fulltimeEquivalent, that.fulltimeEquivalent) &&
                Objects.equals(fulltimeEquivalentLow, that.fulltimeEquivalentLow) &&
                Objects.equals(fulltimeEquivalentHigh, that.fulltimeEquivalentHigh) &&
                Objects.equals(includingOwners, that.includingOwners) &&
                Objects.equals(includingOwnersLow, that.includingOwnersLow) &&
                Objects.equals(includingOwnersHigh, that.includingOwnersHigh);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), employees, employeeLow, employeeHigh, fulltimeEquivalent, fulltimeEquivalentLow, fulltimeEquivalentHigh, includingOwners, includingOwnersLow, includingOwnersHigh);
    }



    /*@Override
    public boolean equalData(Object o) {
        if (!super.equalData(o)) return false;
        CompanyNumbersRecord that = (CompanyNumbersRecord) o;
        return Objects.equals(employees, that.employees) &&
                Objects.equals(employeeLow, that.employeeLow) &&
                Objects.equals(employeeHigh, that.employeeHigh) &&
                Objects.equals(fulltimeEquivalent, that.fulltimeEquivalent) &&
                Objects.equals(fulltimeEquivalentLow, that.fulltimeEquivalentLow) &&
                Objects.equals(fulltimeEquivalentHigh, that.fulltimeEquivalentHigh) &&
                Objects.equals(includingOwners, that.includingOwners) &&
                Objects.equals(includingOwnersLow, that.includingOwnersLow) &&
                Objects.equals(includingOwnersHigh, that.includingOwnersHigh);
    }*/

    @Override
    protected CompanyNumbersRecord clone() throws CloneNotSupportedException {
        CompanyNumbersRecord clone = (CompanyNumbersRecord) super.clone();
        clone.setEmployees(this.employees);
        clone.setEmployeeLow(this.employeeLow);
        clone.setEmployeeHigh(this.employeeHigh);
        clone.setFulltimeEquivalent(this.fulltimeEquivalent);
        clone.setFulltimeEquivalentLow(this.fulltimeEquivalentLow);
        clone.setFulltimeEquivalentHigh(this.fulltimeEquivalentHigh);
        clone.setIncludingOwners(this.includingOwners);
        clone.setIncludingOwnersLow(this.includingOwnersLow);
        clone.setIncludingOwnersHigh(this.includingOwnersHigh);
        return clone;
    }
}
