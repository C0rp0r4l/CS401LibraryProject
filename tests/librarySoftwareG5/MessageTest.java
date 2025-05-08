package librarySoftwareG5;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void testMessageCreationAndGetters() {
        Message.ActionType expectedAction = Message.ActionType.LOGIN;
        String expectedData = "Test Data";
        Message message = new Message(expectedAction, expectedData);

        assertEquals(expectedAction, message.getAction(), "ActionType should match constructor argument.");
        assertEquals(expectedData, message.getData(), "Data should match constructor argument.");
        assertTrue(message.isSuccess(), "New message should be successful by default (no error message).");
        assertNull(message.getErrorMessage(), "New message should have null error message by default.");
    }

    @Test
    void testSetErrorMessage() {
        Message message = new Message(Message.ActionType.ACK, null);
        String errorMessage = "This is an error.";
        message.setErrorMessage(errorMessage);

        assertFalse(message.isSuccess(), "Message should not be successful after setting an error message.");
        assertEquals(errorMessage, message.getErrorMessage(), "Error message should match the set value.");
    }

    @Test
    void testIsSuccess() {
        Message successMessage = new Message(Message.ActionType.ACK, "Success data");
        assertTrue(successMessage.isSuccess(), "Message without error message set should be successful.");

        Message errorMessage = new Message(Message.ActionType.ERR, null);
        errorMessage.setErrorMessage("Failure");
        assertFalse(errorMessage.isSuccess(), "Message with error message set should not be successful.");
    }

    @Test
    void testActionTypeEnum() {
        // Simple test to ensure an action type exists
        assertNotNull(Message.ActionType.valueOf("ADD_MEMBER"));
        // Test a few action types
        assertEquals("LOGIN", Message.ActionType.LOGIN.name());
        assertEquals("MARK_MEMBER_PAST_DUE", Message.ActionType.MARK_MEMBER_PAST_DUE.name());
        assertEquals("MEMBER_ITEM_RETURNED", Message.ActionType.MEMBER_ITEM_RETURNED.name());
    }
}
