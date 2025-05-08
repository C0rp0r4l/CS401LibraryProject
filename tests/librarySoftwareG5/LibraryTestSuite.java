package librarySoftwareG5; 

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;


@Suite
@SuiteDisplayName("Library Management System Test Suite")
@SelectClasses({
    MessageTest.class,
    ItemTest.class,
    MemberTest.class,
    StaffMemberTest.class,
    LocationTest.class,
    ItemListTest.class,
    MemberListTest.class,
})
public class LibraryTestSuite {
    // This class remains empty.
    // The annotations above define the suite.
    // When you run this class with a JUnit 5 runner (e.g., in Eclipse),
    // it will execute all the tests in the selected classes.
}
