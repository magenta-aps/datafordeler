package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class QueryTest {

    private class QueryImpl extends Query<Entity> {

        public QueryImpl() {
            super();
        }

        public QueryImpl(int page, int pageSize) {
            super(page, pageSize);
        }

        @Override
        public Map<String, Object> getSearchParameters() {
            return new HashMap<>();
        }

        @Override
        public LookupDefinition getLookupDefinition() {
            LookupDefinition lookupDefinition = new LookupDefinition(this, DataItem.class);
            return lookupDefinition;
        }

        @Override
        protected boolean isEmpty() {
            return false;
        }

        @Override
        public void setFromParameters(ParameterMap parameters) {
        }

        @Override
        public String getEntityClassname() {
            return Entity.class.getCanonicalName();
        }

        @Override
        public String getEntityIdentifier() {
            return "entity";
        }

        @Override
        protected Map<String, String> joinHandles() {
            return Collections.emptyMap();
        }

        @Override
        protected void setupConditions() {

        }

        @Override
        public Class<Entity> getEntityClass() {
            return Entity.class;
        }

        @Override
        public Class getDataClass() {
            return DataItem.class;
        }
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testPagesize() throws Exception {
        Query query = new QueryImpl(1, 10);
        Assert.assertEquals(10, query.getPageSize());
        query.setPageSize(20);
        Assert.assertEquals(20, query.getPageSize());
        query.setPageSize("30");
        Assert.assertEquals(30, query.getPageSize());
        query.setPageSize(null);
        Assert.assertEquals(30, query.getPageSize());
    }

    @Test
    public void testPagesizeFail() throws Exception {
        exception.expect(IllegalArgumentException.class);
        Query query = new QueryImpl(1, 0);
    }

    @Test
    public void testPage() throws Exception {
        Query query = new QueryImpl(1, 10);
        Assert.assertEquals(1, query.getPage());
        query.setPage(2);
        Assert.assertEquals(2, query.getPage());
        query.setPage("3");
        Assert.assertEquals(3, query.getPage());
        query.setPage(null);
        Assert.assertEquals(3, query.getPage());
    }

    @Test
    public void testPageFail() throws Exception {
        exception.expect(IllegalArgumentException.class);
        Query query = new QueryImpl(-1, 1);
    }

    @Test
    public void testOffset() {
        Query query = new QueryImpl(2, 10);
        Assert.assertEquals(10, query.getOffset());
    }

    @Test
    public void testCount() {
        Query query = new QueryImpl(1, 10);
        Assert.assertEquals(10, query.getCount());
    }

    private HashMap<String, String> dateTimeTests = new HashMap<>();

    @Before
    public void populateDateTimeTests() {
        this.dateTimeTests.put("2017-05-05T13:30:00+01:00", "2017-05-05T13:30:00+01:00");
        this.dateTimeTests.put("2017-05-05+01:00", "2017-05-05T00:00:00+01:00");
        this.dateTimeTests.put("2017-05-05T14:07:30+01:00[Europe/Copenhagen]", "2017-05-05T14:07:30+01:00");
        this.dateTimeTests.put("2017-05-05T14:10:30Z", "2017-05-05T14:10:30+00:00");
        this.dateTimeTests.put("Fri, 5 May 2017 14:11:30 GMT", "2017-05-05T14:11:30+00:00");
        this.dateTimeTests.put("2017-05-05", "2017-05-05T00:00:00+00:00");
        this.dateTimeTests.put("20170505", "2017-05-05T00:00:00+00:00");
    }

    @Test
    public void testRegistrationFrom() throws Exception {
        Query query = new QueryImpl();
        OffsetDateTime time = OffsetDateTime.now();
        query.setRegistrationFromBefore(time);
        Assert.assertEquals(time, query.getRegistrationFromBefore());
        query.setRegistrationFromAfter(time);
        Assert.assertEquals(time, query.getRegistrationFromAfter());

        for (String testDateTime : this.dateTimeTests.keySet()) {
            query.setRegistrationFromBefore(testDateTime);
            Assert.assertEquals(OffsetDateTime.parse(this.dateTimeTests.get(testDateTime)), query.getRegistrationFromBefore());
            query.setRegistrationFromAfter(testDateTime);
            Assert.assertEquals(OffsetDateTime.parse(this.dateTimeTests.get(testDateTime)), query.getRegistrationFromAfter());
        }
    }

    @Test
    public void testRegistrationTo() throws Exception {
        Query query = new QueryImpl();
        OffsetDateTime time = OffsetDateTime.now();
        query.setRegistrationToBefore(time);
        Assert.assertEquals(time, query.getRegistrationToBefore());

        for (String testDateTime : this.dateTimeTests.keySet()) {
            query.setRegistrationToBefore(testDateTime);
            Assert.assertEquals(OffsetDateTime.parse(this.dateTimeTests.get(testDateTime)), query.getRegistrationToBefore());
        }
    }

    @Test
    public void testEffectFrom() throws Exception {
        Query query = new QueryImpl();
        OffsetDateTime time = OffsetDateTime.now();
        query.setEffectFromBefore(time);
        Assert.assertEquals(time, query.getEffectFromBefore());
        query.setEffectFromAfter(time);
        Assert.assertEquals(time, query.getEffectFromAfter());

        for (String testDateTime : this.dateTimeTests.keySet()) {
            query.setEffectFromBefore(testDateTime);
            Assert.assertEquals(OffsetDateTime.parse(this.dateTimeTests.get(testDateTime)), query.getEffectFromBefore());
            query.setEffectFromAfter(testDateTime);
            Assert.assertEquals(OffsetDateTime.parse(this.dateTimeTests.get(testDateTime)), query.getEffectFromAfter());
        }
    }

    @Test
    public void testEffectTo() throws Exception {
        Query query = new QueryImpl();
        OffsetDateTime time = OffsetDateTime.now();
        query.setEffectToBefore(time);
        Assert.assertEquals(time, query.getEffectToBefore());
        query.setEffectToAfter(time);
        Assert.assertEquals(time, query.getEffectToAfter());

        for (String testDateTime : this.dateTimeTests.keySet()) {
            query.setEffectToBefore(testDateTime);
            Assert.assertEquals(OffsetDateTime.parse(this.dateTimeTests.get(testDateTime)), query.getEffectToBefore());
            query.setEffectToAfter(testDateTime);
            Assert.assertEquals(OffsetDateTime.parse(this.dateTimeTests.get(testDateTime)), query.getEffectToAfter());
        }
    }

    @Test
    public void testGetSearchParameters() {
        Query query = new QueryImpl();
        Assert.assertNotNull(query.getSearchParameters());
        Assert.assertEquals(0, query.getSearchParameters().size());
    }
}
