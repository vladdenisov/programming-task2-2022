import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.IncorrectOptionValueCount
import com.github.ajalt.clikt.core.NoSuchOption
import com.github.ajalt.clikt.core.PrintHelpMessage
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class Tests {
    @Test
    fun humanReadableByteCountSI() {
        assertEquals("1.0 kB", humanReadableByteCountSI(1000))
        assertEquals("999 B", humanReadableByteCountSI(999))
        assertEquals("987.7 MB", humanReadableByteCountSI(987654321))
    }
    private fun prepare() {
        val testPath = Files.createDirectory(Paths.get("./build/tempTest"))

        Files.createFile(Paths.get("${testPath.toAbsolutePath()}/1.txt"))
        for (i in 1..100)
            File("./build/tempTest/1.txt").appendText("TEST EXAMPLE FILE")

        Files.createDirectory(Paths.get("${testPath.toAbsolutePath()}/test"))
    }

    private fun checkSize() {
        assertEquals("1.7 kB", LSFile("./build/tempTest/1.txt").displaySize(true))
        assertEquals("1700 B", LSFile("./build/tempTest/1.txt").displaySize(false))
    }

    private fun permissionsFallback() {
        assertEquals("-rw-", LSFile("./build/tempTest/1.txt").windowsPermissions(false))
        assertEquals("600", LSFile("./build/tempTest/1.txt").windowsPermissions(true))
    }

    private fun displayPermissions(){
        val permission = LSFile("./build/tempTest/1.txt").displayPermissions()
        assertContains(permission, "-rw-")
    }

    private fun remove() {
        Files.walk(Paths.get("./build/tempTest"))
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete)
    }


    @Test
    fun main() {
        prepare() // Create files for testing
        checkSize() // Check exact size of file
        permissionsFallback() // Check permissions (use only builtin kotlin functions)
        displayPermissions() // Check string permissions format
        remove() // Cleanup
    }
    @Test
    fun checkArgs() {
        assertFailsWith<NoSuchOption>{mainWithoutLibraryExceptionHandler(arrayOf("--p"))} // Incorrect option
        assertFailsWith<BadParameterValue>{mainWithoutLibraryExceptionHandler(arrayOf("-l", "oooo"))} // Incorrect path + correct option
        assertFailsWith<PrintHelpMessage>{mainWithoutLibraryExceptionHandler(arrayOf("--help"))} // Exception to print help message
        assertFailsWith<IncorrectOptionValueCount>{mainWithoutLibraryExceptionHandler(arrayOf("-o"))} // No path to output file
        assertFailsWith<BadParameterValue> {mainWithoutLibraryExceptionHandler(arrayOf("./testt"))} // No directory test
    }
    @Test
    // Incorrect output file path
    fun checkOutputFile() {
        val stream = ByteArrayOutputStream()
        val ps = PrintStream(stream)
        System.setOut(ps)
        mainWithoutLibraryExceptionHandler(arrayOf("-o ./testt"))
        val output = String(stream.toByteArray())
        assertContains(output, "No such file or directory")
    }
}