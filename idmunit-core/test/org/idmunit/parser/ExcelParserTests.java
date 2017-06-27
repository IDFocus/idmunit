package org.idmunit.parser;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.idmunit.ConnectorManager;
import org.idmunit.ExampleTest;
import org.idmunit.IdMUnitTestCase;
//import org.idmunit.TrivirADDomainDriverTests;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ExcelParserTests extends TestCase {

	public void testLoadingFromTestCase() throws Exception {
        System.setProperty("ConfigLocation", "./docs/IdMUnit-Reference-idmunit-config.xml");
        System.setProperty("ApplySubstitutionsToOperations", "false");
        System.setProperty("ApplySubstitutionsToData", "true");
		ExcelParser.createSuite(ExampleTest.class);
	}

    @SuppressWarnings("unchecked")
    public void testParser() throws Exception {
        System.setProperty("ConfigLocation", "./docs/IdMUnit-Reference-idmunit-config.xml");
        System.setProperty("ApplySubstitutionsToOperations", "false");
        System.setProperty("ApplySubstitutionsToData", "true");
        TestSuite suite = ((ConnectorManager)ExcelParser.loadSuite("docs/IdMUnitReference.xls", "testReference")).getSuite();
        assertEquals(24, suite.testCount());
        Enumeration<IdMUnitTestCase> steps = suite.tests();

        IdMUnitTestCase row8 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 8]", row8.getName());
        assertEquals(1, row8.countTestCases());
        assertEquals("comment", row8.getOperation());
        assertEquals("TEST: REFERENCE\nThis this provide example uses of current IdMUnit features and functionality", row8.getComment());
        assertEquals(0, row8.getAttributeMap().size());

        IdMUnitTestCase row9 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 9]", row9.getName());
        assertEquals(1, row9.countTestCases());
        assertEquals("comment", row9.getOperation());
        assertEquals("Basic Verbs", row9.getComment());
        assertEquals(0, row9.getAttributeMap().size());
        
        IdMUnitTestCase row10 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 10]", row10.getName());
        assertEquals(1, row10.countTestCases());
        assertEquals("addObject", row10.getOperation());
        assertEquals(0, row10.getRetryCount());
        assertEquals(0, row10.getWaitInterval());
        assertEquals("Create an object with addObject.  Be sure to include all required attributes such as naming and object class attributes.  Note that multiple attribute values may be added in addObject by delinating the values in a single cell with the pipe “|”", row10.getComment());
        assertEquals(false, row10.isFailureExpected());
        assertEquals(false, row10.isDisabled());
        assertEquals("IV", row10.getConfig().getName());
        assertEquals(22, row10.getAttributeMap().size());
        Map<String, Collection<String>> data10 = new HashMap<String, Collection<String>>();
        data10.put("dn", singleValue("cn=TIDMTST001,ou=users,o=myorg"));
        data10.put("cn", singleValue("TIDMTST001"));
        data10.put("givenName", singleValue("TST001"));
        data10.put("sn", singleValue("IDMTST001"));
        data10.put("initials", singleValue("F."));
        data10.put("fullName", singleValue("TST001 F. IDMTST001"));
        data10.put("ou", singleValue("myDept"));
        data10.put("company", singleValue("myCompany"));
        data10.put("description", singleValue("myDescription"));
        data10.put("facsimiletelephoneNumber", singleValue("111-111-1111"));
        data10.put("L", singleValue("myLocation"));
        data10.put("loginDisabled", singleValue("false"));
        data10.put("mobile", singleValue("111-111-1111"));
        data10.put("pager", singleValue("111-111-1111"));
        data10.put("postalCode", singleValue("11111"));
        data10.put("st", singleValue("DC"));
        data10.put("street", singleValue("001 Happy Lane"));
        data10.put("telephoneNumber", singleValue("111-111-1111"));
        data10.put("co", singleValue("United States"));
        data10.put("userPassword", singleValue("newUser#1"));
        data10.put("workforceID", singleValue("1111111"));
        List<String> values = new ArrayList<String>();
        values.add("inetOrgPerson");
        values.add("MyAuxClassUser");
        data10.put("objectClass", values);
        assertEquals(data10, row10.getAttributeMap());

        IdMUnitTestCase row11 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 11]", row11.getName());

        IdMUnitTestCase row12 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 12]", row12.getName());

        IdMUnitTestCase row13 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 13]", row13.getName());

        IdMUnitTestCase row14 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 14]", row14.getName());

        IdMUnitTestCase row15 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 15]", row15.getName());

        IdMUnitTestCase row16 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 16]", row16.getName());

        IdMUnitTestCase row17 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 17]", row17.getName());

        IdMUnitTestCase row18 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 18]", row18.getName());

        IdMUnitTestCase row19 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 19]", row19.getName());

        IdMUnitTestCase row20 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 20]", row20.getName());

        IdMUnitTestCase row21 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 21]", row21.getName());

        IdMUnitTestCase row22 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 22]", row22.getName());

        IdMUnitTestCase row23 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 23]", row23.getName());

        IdMUnitTestCase row24 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 24]", row24.getName());

        IdMUnitTestCase row25 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 25]", row25.getName());

        IdMUnitTestCase row26 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 26]", row26.getName());

        IdMUnitTestCase row27 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 27]", row27.getName());
        assertEquals(1, row27.countTestCases());
        assertEquals("addObject", row27.getOperation());
        assertEquals(0, row27.getRetryCount());
        assertEquals(0, row27.getWaitInterval());
        assertEquals("Use substitutions for data values that change between environments (Note that the substitution is defined for each connector in the idmunit-config.xml file and will differ per/profile so that the correct values will be used in development, test, and production).  Substitutions apply to all verbs.  In this case, the context %MYORG% will be converted to o=myorglab in the lab and o=myorgprod in production (see the corresponding IdMUnit-Reference-idmunit-config.xml).", row27.getComment());
        assertEquals(false, row27.isFailureExpected());
        assertEquals(false, row27.isDisabled());
        assertEquals("IV", row27.getConfig().getName());
        assertEquals(1, row27.getAttributeMap().size());
        Map<String, Collection<String>> data27 = new HashMap<String, Collection<String>>();
        data27.put("dn", singleValue("cn=TIDMTST001,ou=users,o=myorglab"));
        assertEquals(data27, row27.getAttributeMap());

        IdMUnitTestCase row28 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testReference[Excel row 28]", row28.getName());
        assertEquals(1, row28.countTestCases());
        assertEquals("validateObject", row28.getOperation());
        assertEquals(10, row28.getRetryCount());
        assertEquals(5000, row28.getWaitInterval());
        assertEquals("Use dynamic data injections for those substituions that need to be generated at run time (such as a date stamp).  In this case, the %TODAY%, %TODAY+30% and %TODAY-30% values will be replaced with today's date, the date 30 days from now, and the date 30 days ago.  ", row28.getComment());
        assertEquals(false, row28.isFailureExpected());
        assertEquals(false, row28.isDisabled());
        assertEquals("IV", row28.getConfig().getName());
        assertEquals(4, row28.getAttributeMap().size());
        Map<String, Collection<String>> data28 = new HashMap<String, Collection<String>>();
        data28.put("dn", singleValue("cn=TIDMTST001,ou=users,o=myorg"));
        Format formatter = new SimpleDateFormat("yyyyMMdd");
        Calendar today = new GregorianCalendar();
        data28.put("ou", singleValue(formatter.format(today.getTime())));
        Calendar plus30 = new GregorianCalendar();
        plus30.add(Calendar.DATE, 30);
        data28.put("company", singleValue("%TODAY+30%")); //formatter.format(plus30.getTime()));
        Calendar minus30 = new GregorianCalendar();
        minus30.add(Calendar.DATE, -30);
        data28.put("description", singleValue(formatter.format(minus30.getTime())));
        assertEquals(data28, row28.getAttributeMap());
    }

    @SuppressWarnings("unchecked")
    public void testMultipleHeaderRows() throws Exception {
        System.setProperty("ConfigLocation", "./docs/IdMUnit-Reference-idmunit-config.xml");
        System.setProperty("ApplySubstitutionsToOperations", "false");
        System.setProperty("ApplySubstitutionsToData", "true");
        TestSuite suite = ((ConnectorManager)ExcelParser.loadSuite("docs/IdMUnitReference.xls", "testHeaders")).getSuite();
        assertEquals(5, suite.testCount());
        Enumeration<IdMUnitTestCase> steps = suite.tests();

        IdMUnitTestCase row10 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testHeaders[Excel row 10]", row10.getName());
        assertEquals(1, row10.countTestCases());
        assertEquals("comment", row10.getOperation());
        assertEquals("TEST: REFERENCE\nThis this provide example uses of current IdMUnit features and functionality", row10.getComment());
        assertEquals(0, row10.getAttributeMap().size());

        IdMUnitTestCase row11 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testHeaders[Excel row 11]", row11.getName());
        assertEquals(1, row11.countTestCases());
        assertEquals("comment", row11.getOperation());
        assertEquals("Basic Verbs", row11.getComment());
        assertEquals(0, row11.getAttributeMap().size());
        
        IdMUnitTestCase row12 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testHeaders[Excel row 12]", row12.getName());
        assertEquals(1, row12.countTestCases());
        assertEquals("addObject", row12.getOperation());
        assertEquals(0, row12.getRetryCount());
        assertEquals(0, row12.getWaitInterval());
        assertEquals("Create an object with addObject.  Be sure to include all required attributes such as naming and object class attributes.  Note that multiple attribute values may be added in addObject by delinating the values in a single cell with the pipe “|”", row12.getComment());
        assertEquals(false, row12.isFailureExpected());
        assertEquals(false, row12.isDisabled());
        assertEquals("IV", row12.getConfig().getName());
        assertEquals(22, row12.getAttributeMap().size());
        Map<String, Collection<String>> data12 = new HashMap<String, Collection<String>>();
        data12.put("dn", singleValue("cn=TIDMTST001,ou=users,o=myorg"));
        data12.put("cn", singleValue("TIDMTST001"));
        data12.put("givenName", singleValue("TST001"));
        data12.put("sn", singleValue("IDMTST001"));
        data12.put("initials", singleValue("F."));
        data12.put("fullName", singleValue("TST001 F. IDMTST001"));
        data12.put("ou", singleValue("myDept"));
        data12.put("company", singleValue("myCompany"));
        data12.put("description", singleValue("myDescription"));
        data12.put("facsimiletelephoneNumber", singleValue("111-111-1111"));
        data12.put("L", singleValue("myLocation"));
        data12.put("loginDisabled", singleValue("false"));
        data12.put("mobile", singleValue("111-111-1111"));
        data12.put("pager", singleValue("111-111-1111"));
        data12.put("postalCode", singleValue("11111"));
        data12.put("st", singleValue("DC"));
        data12.put("street", singleValue("001 Happy Lane"));
        data12.put("telephoneNumber", singleValue("111-111-1111"));
        data12.put("co", singleValue("United States"));
        data12.put("userPassword", singleValue("newUser#1"));
        data12.put("workforceID", singleValue("1111111"));
        List<String> values = new ArrayList<String>();
        values.add("inetOrgPerson");
        values.add("MyAuxClassUser");
        data12.put("objectClass", values);
        assertEquals(data12, row12.getAttributeMap());

        IdMUnitTestCase row13 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testHeaders[Excel row 13]", row13.getName());

        IdMUnitTestCase row14 = (IdMUnitTestCase)steps.nextElement();
        assertEquals("testHeaders[Excel row 14]", row14.getName());
        assertEquals(1, row14.countTestCases());
        assertEquals("execSQL", row14.getOperation());
        assertEquals(0, row14.getRetryCount());
        assertEquals(0, row14.getWaitInterval());
        assertEquals("Execute an SQL statement against a JDBC interface.  The “sql” column is required.", row14.getComment());
        assertEquals(false, row14.isFailureExpected());
        assertEquals(false, row14.isDisabled());
        assertEquals("ORCL", row14.getConfig().getName());
        assertEquals(1, row14.getAttributeMap().size());
        Map<String, Collection<String>> data14 = new HashMap<String, Collection<String>>();
        data14.put("sql", singleValue("delete from idm.employee where workforceid=99999"));
        assertEquals(data14, row14.getAttributeMap());
    }
    
    private static Collection<String> singleValue(String value) {
    	List<String> values = new ArrayList<String>();
    	values.add(value);
    	return values;
    }
}
