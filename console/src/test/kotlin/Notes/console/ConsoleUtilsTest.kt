/*
 * This Kotlin source file was generated by the Gradle "init" task.
 */
package Notes.console

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertEquals

class ConsoleUtilsTest {
    @Test fun testGetMessage() {
        assertEquals("Hello CS 346!", ConsoleUtils.getMessage())
    }
    @Test fun testPrintMessage() {
        ConsoleUtils.printMessage()
    }
}
