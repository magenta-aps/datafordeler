package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * Query object specifying a search, with basic filter parameters
 * Subclasses should specify further searchable parameters, annotated with @QueryField.
 * SOAP clients should pass a serialized instance of a Query class to the SOAP interface
 */
public abstract class BaseQuery {

    public static final String[] PARAM_PAGE = new String[] {"side", "page"};
    public static final String[] PARAM_PAGESIZE = new String[] {"sidestoerrelse", "pageSize"};
    public static final String[] PARAM_REGISTRATION_FROM_BEFORE = new String[] {"registreringFraFør", "registrationFromBefore"};
    public static final String[] PARAM_REGISTRATION_FROM_AFTER = new String[] {"registreringFraEfter", "registrationFromAfter", "registreringFra", "registrationFrom"};
    public static final String[] PARAM_REGISTRATION_TO_BEFORE = new String[] {"registreringTilFør", "registrationToBefore", "registreringTil", "registrationTo"};
    public static final String[] PARAM_REGISTRATION_TO_AFTER = new String[] {"registreringTilEfter", "registrationToAfter"};
    public static final String[] PARAM_EFFECT_FROM_BEFORE = new String[] {"virkningFraFør", "effectFromBefore"};
    public static final String[] PARAM_EFFECT_FROM_AFTER = new String[] {"virkningFraEfter", "effectFromAfter"};
    public static final String[] PARAM_EFFECT_TO_BEFORE = new String[] {"virkningTilFør", "effectToBefore"};
    public static final String[] PARAM_EFFECT_TO_AFTER = new String[] {"virkningTilEfter", "effectToAfter"};
    public static final String[] PARAM_RECORD_AFTER = new String[] { "opdateretEfter", "recordAfter" };

    public static final String[] PARAM_OUTPUT_WRAPPING = new String[] {"format", "fmt"};
    public static final Map<String, OutputWrapper.Mode> PARAM_OUTPUT_WRAPPING_VALUEMAP = new HashMap<>();
    static {
        PARAM_OUTPUT_WRAPPING_VALUEMAP.put("rvd", OutputWrapper.Mode.RVD);
        PARAM_OUTPUT_WRAPPING_VALUEMAP.put("rdv", OutputWrapper.Mode.RDV);
        PARAM_OUTPUT_WRAPPING_VALUEMAP.put("drv", OutputWrapper.Mode.DRV);
        PARAM_OUTPUT_WRAPPING_VALUEMAP.put("legacy", OutputWrapper.Mode.LEGACY);
    }

    private static Logger log = LogManager.getLogger(BaseQuery.class.getCanonicalName());

    @QueryField(queryNames = {"side", "page"}, type = QueryField.FieldType.INT)
    protected int page = 1;

    @QueryField(queryNames = {"sidestoerrelse", "pageSize"}, type = QueryField.FieldType.INT)
    protected int pageSize = 10;

    @QueryField(queryNames = {"registreringFraFør", "registrationFromBefore"}, type = QueryField.FieldType.STRING)
    protected OffsetDateTime registrationFromBefore = null;

    @QueryField(queryNames = {"registreringFraEfter", "registrationFromAfter"}, type = QueryField.FieldType.STRING)
    protected OffsetDateTime registrationFromAfter = null;

    @QueryField(queryNames = {"registreringTilFør", "registrationToBefore"}, type = QueryField.FieldType.STRING)
    protected OffsetDateTime registrationToBefore = null;

    @QueryField(queryNames = {"registreringTilEfter", "registrationToAfter"}, type = QueryField.FieldType.STRING)
    protected OffsetDateTime registrationToAfter = null;

    @QueryField(queryNames = {"virkningFraFør", "effectFromBefore"}, type = QueryField.FieldType.STRING)
    protected OffsetDateTime effectFromBefore = null;

    @QueryField(queryNames = {"virkningFraEfter", "effectFromAfter"}, type = QueryField.FieldType.STRING)
    protected OffsetDateTime effectFromAfter = null;

    @QueryField(queryNames = {"virkningTilFør", "effectToBefore"}, type = QueryField.FieldType.STRING)
    protected OffsetDateTime effectToBefore  = null;

    @QueryField(queryNames = {"virkningTilEfter", "effectToAfter"}, type = QueryField.FieldType.STRING)
    protected OffsetDateTime effectToAfter = null;

    @QueryField(queryNames = { "opdateretEfter", "recordAfter" }, type = QueryField.FieldType.STRING)
    protected OffsetDateTime recordAfter = null;

    @QueryField(queryName = "UUID", type = QueryField.FieldType.STRING)
    protected List<UUID> uuid = new ArrayList<>();

    private List<String> kommunekodeRestriction = new ArrayList<>();

    private OutputWrapper.Mode mode = null;

    public BaseQuery() {
    }

    /**
     * Create a basic Query, filtering on page and pageSize (akin to offset & limit)
     * @param page
     * @param pageSize
     */
    public BaseQuery(int page, int pageSize) {
        this.setPage(page);
        this.setPageSize(pageSize);
    }

    /**
     * Create a basic Query, filtering on page and pageSize (akin to offset & limit), as well as output filtering
     * (Found entities will only include registrations that fall within the registrationTime limits)
     * @param page
     * @param pageSize
     */
    public BaseQuery(int page, int pageSize, OffsetDateTime registrationFromAfter, OffsetDateTime registrationFromBefore, OffsetDateTime registrationToAfter, OffsetDateTime registrationToBefore) {
        this(page, pageSize);
        this.registrationFromAfter = registrationFromAfter;
        this.registrationFromBefore = registrationFromBefore;
        this.registrationToAfter = registrationToAfter;
        this.registrationToBefore = registrationToBefore;
    }
    public BaseQuery(int page, int pageSize, OffsetDateTime registrationFromAfter, OffsetDateTime registrationToBefore) {
        this(page, pageSize, registrationFromAfter, null, null, registrationToBefore);
    }

    /**
     * Create a basic Query, filtering on page and pageSize (akin to offset & limit). This is the String parameter version; the parameters will be parsed as integers.
     * @param page
     * @param pageSize
     */
    public BaseQuery(String page, String pageSize) {
        this(intFromString(page, 0), intFromString(pageSize, 10), null, null);
    }

    /**
     * Create a basic Query, filtering on page and pageSize (akin to offset & limit), as well as output filtering
     * (Found entities will only include registrations that fall within the registrationTime limits)
     * This is the String parameter version; the parameters will be parsed as integers and OffsetDateTimes
     * @param page
     * @param pageSize
     */
    public BaseQuery(String page, String pageSize, String registrationFromAfter, String registrationToBefore) {
        this(page, pageSize, registrationFromAfter, null, null, registrationToBefore);
    }
    public BaseQuery(String page, String pageSize, String registrationFromAfter, String registrationFromBefore, String registrationToAfter, String registrationToBefore) {
        this(
                intFromString(page, 0),
                intFromString(pageSize, 10),
                parseDateTime(registrationFromAfter),
                parseDateTime(registrationFromBefore),
                parseDateTime(registrationToAfter),
                parseDateTime(registrationToBefore)
        );
    }


    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be at least 1");
        }
        this.pageSize = pageSize;
    }

    public void setPageSize(String pageSize) {
        if (pageSize != null) {
            this.pageSize = Integer.parseInt(pageSize);
        }
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        if (page < 1) {
            throw new IllegalArgumentException("page must be at least 1");
        }
        this.page = page;
    }

    public void setPage(String page) {
        if (page != null) {
            this.page = Integer.parseInt(page);
        }
    }

    public int getOffset() {
        return (this.page - 1) * this.pageSize;
    }

    public int getCount() {
        return this.pageSize;
    }

    public OffsetDateTime getRegistrationFromBefore() {
        return this.registrationFromBefore;
    }

    public void setRegistrationFromBefore(OffsetDateTime registrationFromBefore) {
        this.setRegistrationFromBefore(registrationFromBefore, null);
    }

    public void setRegistrationFromBefore(OffsetDateTime registrationFromBefore, OffsetDateTime fallback) {
        this.registrationFromBefore = registrationFromBefore;
        if (registrationFromBefore == null && fallback != null) {
            this.registrationFromBefore = fallback;
        }
    }

    public void setRegistrationFromBefore(String registrationFromBefore) {
        this.setRegistrationFromBefore(registrationFromBefore, null);
    }

    public void setRegistrationFromBefore(String registrationFromBefore, OffsetDateTime fallback) throws DateTimeParseException {
        this.setRegistrationFromBefore(parseDateTime(registrationFromBefore), fallback);
    }





    public OffsetDateTime getRegistrationFrom() {
        return this.registrationFromBefore;
    }

    public OffsetDateTime getRegistrationFromAfter() {
        return this.registrationFromAfter;
    }

    public void setRegistrationFromAfter(OffsetDateTime registrationFromAfter) {
        this.setRegistrationFromAfter(registrationFromAfter, null);
    }

    public void setRegistrationFromAfter(OffsetDateTime registrationFromAfter, OffsetDateTime fallback) {
        this.registrationFromAfter = registrationFromAfter;
        if (registrationFromAfter == null && fallback != null) {
            this.registrationFromAfter = fallback;
        }
    }

    public void setRegistrationFromAfter(String registrationFromAfter) {
        this.setRegistrationFromAfter(registrationFromAfter, null);
    }

    public void setRegistrationFromAfter(String registrationFromAfter, OffsetDateTime fallback) throws DateTimeParseException {
        this.setRegistrationFromAfter(parseDateTime(registrationFromAfter), fallback);
    }

    public void setRegistrationFrom(OffsetDateTime registrationFrom) {
        this.setRegistrationFromAfter(registrationFrom);
    }

    public void setRegistrationFrom(OffsetDateTime registrationFrom, OffsetDateTime fallback) {
        this.setRegistrationFromAfter(registrationFrom, fallback);
    }

    public void setRegistrationFrom(String registrationFromAfter) {
        this.setRegistrationFromAfter(registrationFromAfter);
    }

    public void setRegistrationFrom(String registrationFromAfter, OffsetDateTime fallback) throws DateTimeParseException {
        this.setRegistrationFromAfter(registrationFromAfter, fallback);
    }




    public OffsetDateTime getRegistrationToAfter() {
        return this.registrationToAfter;
    }

    public void setRegistrationToAfter(OffsetDateTime registrationToAfter) {
        this.setRegistrationToAfter(registrationToAfter, null);
    }

    public void setRegistrationToAfter(OffsetDateTime registrationToAfter, OffsetDateTime fallback) {
        this.registrationToAfter = registrationToAfter;
        if (registrationToAfter == null && fallback != null) {
            this.registrationToAfter = fallback;
        }
    }

    public void setRegistrationToAfter(String registrationToAfter) {
        this.setRegistrationToAfter(registrationToAfter, null);
    }

    public void setRegistrationToAfter(String registrationToAfter, OffsetDateTime fallback) throws DateTimeParseException {
        this.setRegistrationToAfter(parseDateTime(registrationToAfter), fallback);
    }


    public OffsetDateTime getRegistrationToBefore() {
        return this.registrationToBefore;
    }

    public void setRegistrationToBefore(OffsetDateTime registrationToBefore) {
        this.setRegistrationToBefore(registrationToBefore, null);
    }

    public void setRegistrationToBefore(OffsetDateTime registrationToBefore, OffsetDateTime fallback) {
        this.registrationToBefore = registrationToBefore;
        if (registrationToBefore == null && fallback != null) {
            this.registrationToBefore = fallback;
        }
    }

    public void setRegistrationToBefore(String registrationToBefore) {
        this.setRegistrationToBefore(registrationToBefore, null);
    }

    public void setRegistrationToBefore(String registrationToBefore, OffsetDateTime fallback) throws DateTimeParseException {
        this.setRegistrationToBefore(parseDateTime(registrationToBefore), fallback);
    }



    public OffsetDateTime getRegistrationTo() {
        return this.getRegistrationToBefore();
    }

    public void setRegistrationTo(OffsetDateTime registrationTo) {
        this.setRegistrationToBefore(registrationTo);
    }

    public void setRegistrationTo(OffsetDateTime registrationTo, OffsetDateTime fallback) {
        this.setRegistrationToBefore(registrationTo, fallback);
    }

    public void setRegistrationTo(String registrationTo) {
        this.setRegistrationToBefore(registrationTo);
    }

    public void setRegistrationTo(String registrationTo, OffsetDateTime fallback) throws DateTimeParseException {
        this.setRegistrationToBefore(registrationTo, fallback);
    }



    public OffsetDateTime getEffectFrom() {
        return this.effectFromBefore;
    }

    public OffsetDateTime getEffectFromBefore() {
        return this.effectFromBefore;
    }

    public void setEffectFromBefore(OffsetDateTime effectFromBefore) {
        this.setEffectFromBefore(effectFromBefore, null);
    }

    public void setEffectFromBefore(OffsetDateTime effectFromBefore, OffsetDateTime fallback) {
        this.effectFromBefore = effectFromBefore;
        if (effectFromBefore == null && fallback != null) {
            this.effectFromBefore = fallback;
        }
    }

    public void setEffectFromBefore(String effectFromBefore) {
        this.setEffectFromBefore(effectFromBefore, null);
    }

    public void setEffectFromBefore(String effectFromBefore, OffsetDateTime fallback) {
        this.setEffectFromBefore(parseDateTime(effectFromBefore), fallback);
    }


    public OffsetDateTime getEffectFromAfter() {
        return this.effectFromAfter;
    }

    public void setEffectFromAfter(OffsetDateTime effectFromAfter) {
        this.setEffectFromAfter(effectFromAfter, null);
    }

    public void setEffectFromAfter(OffsetDateTime effectFromAfter, OffsetDateTime fallback) {
        this.effectFromAfter = effectFromAfter;
        if (effectFromAfter == null && fallback != null) {
            this.effectFromAfter = fallback;
        }
    }

    public void setEffectFromAfter(String effectFromAfter) {
        this.setEffectFromAfter(effectFromAfter, null);
    }

    public void setEffectFromAfter(String effectFromAfter, OffsetDateTime fallback) {
        this.setEffectFromAfter(parseDateTime(effectFromAfter), fallback);
    }




    public OffsetDateTime getEffectToBefore() {
        return this.effectToBefore;
    }

    public void setEffectToBefore(OffsetDateTime effectToBefore) {
        this.setEffectToBefore(effectToBefore, null);
    }

    public void setEffectToBefore(OffsetDateTime effectToBefore, OffsetDateTime fallback) {
        this.effectToBefore = effectToBefore;
        if (effectToBefore == null && fallback != null) {
            this.effectToBefore = fallback;
        }
    }

    public void setEffectToBefore(String effectToBefore) {
        this.setEffectToBefore(effectToBefore, null);
    }

    public void setEffectToBefore(String effectToBefore, OffsetDateTime fallback) {
        this.setEffectToBefore(parseDateTime(effectToBefore), fallback);
    }



    public OffsetDateTime getEffectToAfter() {
        return this.effectToAfter;
    }

    public void setEffectToAfter(OffsetDateTime effectToAfter) {
        this.setEffectToAfter(effectToAfter, null);
    }

    public void setEffectToAfter(OffsetDateTime effectToAfter, OffsetDateTime fallback) {
        this.effectToAfter = effectToAfter;
        if (effectToAfter == null && fallback != null) {
            this.effectToAfter = fallback;
        }
    }

    public void setEffectToAfter(String effectToAfter) {
        this.setEffectToAfter(effectToAfter, null);
    }

    public void setEffectToAfter(String effectToAfter, OffsetDateTime fallback) {
        this.setEffectToAfter(parseDateTime(effectToAfter), fallback);
    }








    public void setEffectFrom(OffsetDateTime EffectFrom) {
        this.setEffectFromAfter(EffectFrom);
    }

    public void setEffectFrom(OffsetDateTime EffectFrom, OffsetDateTime fallback) {
        this.setEffectFromAfter(EffectFrom, fallback);
    }

    public void setEffectFrom(String EffectFromAfter) {
        this.setEffectFromAfter(EffectFromAfter);
    }

    public void setEffectFrom(String EffectFromAfter, OffsetDateTime fallback) throws DateTimeParseException {
        this.setEffectFromAfter(EffectFromAfter, fallback);
    }

    public OffsetDateTime getEffectTo() {
        return this.getEffectToAfter();
    }

    public void setEffectTo(OffsetDateTime EffectTo) {
        this.setEffectToAfter(EffectTo);
    }

    public void setEffectTo(OffsetDateTime EffectTo, OffsetDateTime fallback) {
        this.setEffectToAfter(EffectTo, fallback);
    }

    public void setEffectTo(String EffectTo) {
        this.setEffectToAfter(EffectTo);
    }

    public void setEffectTo(String EffectTo, OffsetDateTime fallback) throws DateTimeParseException {
        this.setEffectToAfter(EffectTo, fallback);
    }









    public OffsetDateTime getRecordAfter() {
        return this.recordAfter;
    }

    public void setRecordAfter(OffsetDateTime recordAfter) {
        this.recordAfter = recordAfter;
    }

    public void setRecordAfter(String recordAfter) throws DateTimeParseException {
        this.recordAfter = parseDateTime(recordAfter);
        if (recordAfter != null) {
            this.increaseDataParamCount();
        }
    }

    public void setUuid(Collection<UUID> uuid) {
        this.uuid = new ArrayList<>(uuid);
    }

    public List<UUID> getUuid() {
        return this.uuid;
    }

    public void addUUID(String uuid) throws InvalidClientInputException {
        if (uuid != null){
            try {
                this.uuid.add(UUID.fromString(uuid));
            } catch (IllegalArgumentException e) {
                throw new InvalidClientInputException("Invalid uuid "+uuid, e);
            }
        }
    }


    public void addKommunekodeRestriction(String kommunekode) {
        this.kommunekodeRestriction.add(kommunekode);
    }
    public List<String> getKommunekodeRestriction() {
        return this.kommunekodeRestriction;
    }


    public abstract Map<String, Object> getSearchParameters();

    /**
     * Obtain a LookupDefinition object that describes the query in that form.
     * This means a definition where keys are set to the full lookup path for
     * the attribute in question, and values are set from the query.
     * @return
     */
    public abstract BaseLookupDefinition getLookupDefinition();

    public OutputWrapper.Mode getMode() {
        return this.mode;
    }

    public OutputWrapper.Mode getMode(OutputWrapper.Mode fallback) {
        return this.mode != null ? this.mode : fallback;
    }

    /**
     * Parse a ParameterMap from a http request and insert values in this Query object
     * @param parameterMap
     */
    public void fillFromParameters(ParameterMap parameterMap, boolean limitsOnly) throws InvalidClientInputException {
        this.setPage(parameterMap.getFirstOf(PARAM_PAGE));
        this.setPageSize(parameterMap.getFirstOf(PARAM_PAGESIZE));
        try {
            OffsetDateTime now = OffsetDateTime.now();
            this.setRegistrationFromBefore(parameterMap.getFirstOf(PARAM_REGISTRATION_FROM_BEFORE)); // If not set, use current time
            this.setRegistrationFromAfter(parameterMap.getFirstOf(PARAM_REGISTRATION_FROM_AFTER));
            this.setRegistrationToBefore(parameterMap.getFirstOf(PARAM_REGISTRATION_TO_BEFORE));
            this.setRegistrationToAfter(parameterMap.getFirstOf(PARAM_REGISTRATION_TO_AFTER));
            this.setEffectFromBefore(parameterMap.getFirstOf(PARAM_EFFECT_FROM_BEFORE));
            this.setEffectFromAfter(parameterMap.getFirstOf(PARAM_EFFECT_FROM_AFTER));
            this.setEffectToBefore(parameterMap.getFirstOf(PARAM_EFFECT_TO_BEFORE));
            this.setEffectToAfter(parameterMap.getFirstOf(PARAM_EFFECT_TO_AFTER));
            this.setRecordAfter(parameterMap.getFirstOf(PARAM_RECORD_AFTER));
        } catch (DateTimeParseException e) {
            throw new InvalidClientInputException(e.getMessage());
        }
        if (!limitsOnly) {
            this.setFromParameters(parameterMap);
            if (this.getDataParamCount() == 0) {
                throw new InvalidClientInputException("Missing query parameters");
            }
        }
        String modeString = parameterMap.getFirstOf(PARAM_OUTPUT_WRAPPING);
        if (modeString != null) {
            this.mode = PARAM_OUTPUT_WRAPPING_VALUEMAP.get(modeString);
        }
    }

    private int dataParamCount = 0;

    protected void increaseDataParamCount() {
        this.dataParamCount++;
    }

    protected int getDataParamCount() {
        return this.dataParamCount;
    }

    public abstract void setFromParameters(ParameterMap parameterMap) throws InvalidClientInputException;

    /**
     * Convenience method for parsing a String as an integer, without throwing a parseexception
     * @param s String holding integer to be parsed
     * @return Parse result, or null if unparseable
     */
    public static Integer intFromString(String s) {
        return BaseQuery.intFromString(s, null);
    }

    /**
     * Convenience method for parsing a String as an integer, without throwing a parseexception
     * @param s String holding integer to be parsed
     * @param def Fallback value if string is unparseable
     * @return Parse result, or def if unparseable
     */
    public static Integer intFromString(String s, Integer def) {
        if (s == null) {
            return def;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Convenience method for parsing a String as a boolean
     * @param s String holding boolean to be parsed ("1", "true", "yes", "0", "false", "no")
     * @return Parse result, or null if neither of the above are found
     */
    public static Boolean booleanFromString(String s) {
        return BaseQuery.booleanFromString(s, null);
    }

    /**
     * Convenience method for parsing a String as a boolean
     * @param s String holding boolean to be parsed ("1", "true", "yes", "0", "false", "no")
     * @param def Fallback value if string doesn't match
     * @return Parse result, or def if neither of the above are found
     */
    public static Boolean booleanFromString(String s, Boolean def) {
        if (s != null) {
            s = s.toLowerCase();
            if (s.equals("1") || s.equals("true") || s.equals("yes")) {
                return true;
            }
            if (s.equals("0") || s.equals("false") || s.equals("no")) {
                return false;
            }
        }
        return def;
    }


    private static DateTimeFormatter[] zonedDateTimeFormatters = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.RFC_1123_DATE_TIME,
    };

    private static DateTimeFormatter[] zonedDateFormatters = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_OFFSET_DATE,
    };

    private static DateTimeFormatter[] unzonedDateTimeFormatters = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
    };


    private static DateTimeFormatter[] unzonedDateFormatters = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_DATE,
            DateTimeFormatter.BASIC_ISO_DATE
    };

    private static int formatterCount = zonedDateTimeFormatters.length + zonedDateFormatters.length + unzonedDateTimeFormatters.length + unzonedDateFormatters.length;

    /**
     * Convenience method for parsing a String as an OffsetDateTime
     * A series of parsers will attempt to parse the input string, returning on the first success.
     * The Parsers, in order, are:
     *   DateTimeFormatter.ISO_OFFSET_DATE_TIME
     *   DateTimeFormatter.ISO_ZONED_DATE_TIME
     *   DateTimeFormatter.ISO_INSTANT
     *   DateTimeFormatter.RFC_1123_DATE_TIME
     *   DateTimeFormatter.ISO_OFFSET_DATE
     *   DateTimeFormatter.ISO_DATE_TIME
     *   DateTimeFormatter.ISO_LOCAL_DATE_TIME
     *   DateTimeFormatter.ISO_DATE
     *   DateTimeFormatter.BASIC_ISO_DATE
     * @param dateTime
     * @return Parsed OffsetDateTime, or null if input was null
     * @throws DateTimeParseException if no parser succeeded on a non-null input string
     */
    public static OffsetDateTime parseDateTime(String dateTime) throws DateTimeParseException {
        if (dateTime != null) {
            for (DateTimeFormatter formatter : zonedDateTimeFormatters) {
                try {
                    return OffsetDateTime.parse(dateTime, formatter);
                } catch (DateTimeParseException e) {
                    //I added logging of exceptions here as I expected exceptions to be an error, it seems like it is this way by design, and I disable the loggings again
                }
            }
            for (DateTimeFormatter formatter : zonedDateFormatters) {
                try {
                    TemporalAccessor accessor = formatter.parse(dateTime);
                    return OffsetDateTime.of(LocalDate.from(accessor), LocalTime.MIDNIGHT, ZoneOffset.from(accessor));
                } catch (DateTimeParseException e) {
                    //I added logging of exceptions here as I expected exceptions to be an error, it seems like it is this way by design, and I disable the loggings again
                }
            }
            for (DateTimeFormatter formatter : unzonedDateTimeFormatters) {
                try {
                    TemporalAccessor accessor = formatter.parse(dateTime);
                    return OffsetDateTime.of(LocalDateTime.from(accessor), ZoneOffset.UTC);
                } catch (DateTimeParseException e) {
                    //I added logging of exceptions here as I expected exceptions to be an error, it seems like it is this way by design, and I disable the loggings again
                }
            }
            for (DateTimeFormatter formatter : unzonedDateFormatters) {
                try {
                    TemporalAccessor accessor = formatter.parse(dateTime);
                    return OffsetDateTime.of(LocalDate.from(accessor), LocalTime.MIDNIGHT, ZoneOffset.UTC);
                } catch (DateTimeParseException e) {
                    //I added logging of exceptions here as I expected exceptions to be an error, it seems like it is this way by design, and I disable the loggings again
                }
            }
            throw new DateTimeParseException("Unable to parse date string \""+dateTime+"\", tried "+ formatterCount + " parsers of "+DateTimeFormatter.class.getCanonicalName(), dateTime, 0);
        }
        return null;
    }


    /**
     * Put Query parameters into the Hibernate session. Subclasses should override this and call this method, then
     * put their own Query-subclass-specific parameters in as well
     * @param session Hibernate session in use
     */
    public void applyFilters(Session session) {
        /*if (this.getRegistrationFromBefore() != null) {
            //this.applyFilter(session, Registration.FILTER_REGISTRATION_FROM, Registration.FILTERPARAM_REGISTRATION_FROM, this.getRegistrationFromBefore());
            System.out.println("Activating filter "+Monotemporal.FILTER_REGISTRATIONFROM_BEFORE+"  "+this.getRegistrationFromBefore());
            this.applyFilter(session, Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, Monotemporal.FILTERPARAM_REGISTRATIONFROM_BEFORE, this.getRegistrationFromBefore());
        }*/
        if (this.getRegistrationFromAfter() != null) {
            //this.applyFilter(session, Registration.FILTER_REGISTRATION_FROM, Registration.FILTERPARAM_REGISTRATION_FROM, this.getRegistrationFromBefore());
            log.debug("Activating filter "+Monotemporal.FILTER_REGISTRATIONFROM_AFTER+"  "+this.getRegistrationFromAfter());
            this.applyFilter(session, Monotemporal.FILTER_REGISTRATIONFROM_AFTER, Monotemporal.FILTERPARAM_REGISTRATIONFROM_AFTER, this.getRegistrationFromAfter());
        }
        if (this.getRegistrationToBefore() != null) {
            //this.applyFilter(session, Registration.FILTER_REGISTRATION_TO, Registration.FILTERPARAM_REGISTRATION_TO, this.getRegistrationToBefore());
            log.debug("Activating filter "+Monotemporal.FILTER_REGISTRATIONTO_BEFORE+"  "+this.getRegistrationToBefore());
            this.applyFilter(session, Monotemporal.FILTER_REGISTRATIONTO_BEFORE, Monotemporal.FILTERPARAM_REGISTRATIONTO_BEFORE, this.getRegistrationToBefore());
        }
        if (this.getRegistrationToAfter() != null) {
            //this.applyFilter(session, Registration.FILTER_REGISTRATION_TO, Registration.FILTERPARAM_REGISTRATION_TO, this.getRegistrationToBefore());
            log.debug("Activating filter "+Monotemporal.FILTER_REGISTRATIONTO_AFTER+"  "+this.getRegistrationToAfter());
            this.applyFilter(session, Monotemporal.FILTER_REGISTRATIONTO_AFTER, Monotemporal.FILTERPARAM_REGISTRATIONTO_AFTER, this.getRegistrationToAfter());
        }

        if (this.getEffectFromBefore() != null) {
            //this.applyFilter(session, Effect.FILTER_EFFECT_FROM, Effect.FILTERPARAM_EFFECT_FROM, this.getEffectFrom());
            log.debug("Activating filter "+Bitemporal.FILTER_EFFECTFROM_BEFORE+"  "+this.getEffectFromBefore());
            this.applyFilter(session, Bitemporal.FILTER_EFFECTFROM_BEFORE, Bitemporal.FILTERPARAM_EFFECTFROM_BEFORE, this.getEffectFromBefore());
        }
        if (this.getEffectFromAfter() != null) {
            //this.applyFilter(session, Effect.FILTER_EFFECT_FROM, Effect.FILTERPARAM_EFFECT_FROM, this.getEffectFrom());
            log.debug("Activating filter "+Bitemporal.FILTER_EFFECTFROM_AFTER+"  "+this.getEffectFromAfter());
            this.applyFilter(session, Bitemporal.FILTER_EFFECTFROM_AFTER, Bitemporal.FILTERPARAM_EFFECTFROM_AFTER, this.getEffectFromAfter());
        }
        if (this.getEffectToBefore() != null) {
            //this.applyFilter(session, Effect.FILTER_EFFECT_TO, Effect.FILTERPARAM_EFFECT_TO, this.getEffectTo());
            log.debug("Activating filter "+Bitemporal.FILTER_EFFECTTO_BEFORE+"  "+this.getEffectToBefore());
            this.applyFilter(session, Bitemporal.FILTER_EFFECTTO_BEFORE, Bitemporal.FILTERPARAM_EFFECTTO_BEFORE, this.getEffectToBefore());
        }
        if (this.getEffectToAfter() != null) {
            //this.applyFilter(session, Effect.FILTER_EFFECT_TO, Effect.FILTERPARAM_EFFECT_TO, this.getEffectTo());
            log.debug("Activating filter "+Bitemporal.FILTER_EFFECTTO_AFTER+"  "+this.getEffectToAfter());
            this.applyFilter(session, Bitemporal.FILTER_EFFECTTO_AFTER, Bitemporal.FILTERPARAM_EFFECTTO_AFTER, this.getEffectToAfter());
        }

        if (this.getRecordAfter() != null) {
            //this.applyFilter(session, DataItem.FILTER_RECORD_AFTER, DataItem.FILTERPARAM_RECORD_AFTER, this.getRecordAfter());
            System.out.println("Activating filter "+Nontemporal.FILTER_LASTUPDATED_AFTER+"  "+this.getRecordAfter());
            this.applyFilter(session, Nontemporal.FILTER_LASTUPDATED_AFTER, Nontemporal.FILTERPARAM_LASTUPDATED_AFTER, this.getRecordAfter());
        }
    }

    private void applyFilter(Session session, String filterName, String parameterName, Object parameterValue) {
        if (session.getSessionFactory().getDefinedFilterNames().contains(filterName)) {
            log.debug("enable filter");
            session.enableFilter(filterName).setParameter(
                    parameterName,
                    this.castFilterParam(parameterValue, filterName)
            );
        } else {
            log.debug("do not enable filter");
        }
    }

    protected Object castFilterParam(Object input, String filter) {
        return input;
    }
}
