package ch.hsr.mixtape;

import static java.io.File.separator;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

public class FileFinderTest {

	private static final String EMPTY_DIRECTORY_PATH = "empty_directory";

	private static final String DIRECTORY_A_PATH = "test_directory_a";
	private static final String DIRECTORY_B_PATH = "test_directory_b";
	private static final String DIRECTORY_C_PATH = DIRECTORY_B_PATH + separator + "test_directory_c";

	private static final String FILE_A0_PATH = DIRECTORY_A_PATH + separator + "file_a0.test";
	private static final String FILE_A1_PATH = DIRECTORY_A_PATH + separator + "file_a1.test";
	private static final String FILE_A2_PATH = DIRECTORY_A_PATH + separator + "file_a2.test";

	private static final String FILE_B0_PATH = DIRECTORY_B_PATH + separator + "file_b0.test";
	private static final String FILE_B1_PATH = DIRECTORY_B_PATH + separator + "file_b1.test";
	private static final String FILE_B2_PATH = DIRECTORY_B_PATH + separator + "file_b2.test";

	private static final String FILE_C0_PATH = DIRECTORY_C_PATH + separator + "file_c0.test";
	private static final String FILE_C1_PATH = DIRECTORY_C_PATH + separator + "file_c1.test";
	private static final String FILE_C2_PATH = DIRECTORY_C_PATH + separator + "file_c2.test";

	@BeforeClass
	public static void setUp() throws IOException {
		makeDirectory(EMPTY_DIRECTORY_PATH);

		makeDirectory(DIRECTORY_A_PATH);
		makeDirectory(DIRECTORY_B_PATH);
		makeDirectory(DIRECTORY_C_PATH);

		makeFile(FILE_A0_PATH);
		makeFile(FILE_A1_PATH);
		makeFile(FILE_A2_PATH);

		makeFile(FILE_B0_PATH);
		makeFile(FILE_B1_PATH);
		makeFile(FILE_B2_PATH);

		makeFile(FILE_C0_PATH);
		makeFile(FILE_C1_PATH);
		makeFile(FILE_C2_PATH);
	}

	private static void makeDirectory(String path) {
		File directory = new File(path);

		directory.mkdirs();
		directory.deleteOnExit();
	}

	private static void makeFile(String path) throws IOException {
		File file = new File(path);

		file.createNewFile();
		file.deleteOnExit();
	}

	@Test
	public void testFindInEmptyDirectory() {
		File emptyDirectory = new File(EMPTY_DIRECTORY_PATH);

		FileFinder finder = new FileFinder(asList(emptyDirectory), createSimpleFilter());
		assertThat(finder.find(), is(empty()));
	}

	@Test
	public void testFindInDirectory() {
		File directoryA = new File(DIRECTORY_A_PATH);

		FileFinder finder = new FileFinder(asList(directoryA), createSimpleFilter());
		assertThat(finder.find(), containsInAnyOrder(
				new File(FILE_A0_PATH),
				new File(FILE_A1_PATH),
				new File(FILE_A2_PATH)));
	}

	@Test
	public void testFindRecursivelyInDirectory() {
		File directoryB = new File(DIRECTORY_B_PATH);

		FileFinder finder = new FileFinder(asList(directoryB), createSimpleFilter());
		assertThat(finder.find(), containsInAnyOrder(
				new File(FILE_B0_PATH),
				new File(FILE_B1_PATH),
				new File(FILE_B2_PATH),
				new File(FILE_C0_PATH),
				new File(FILE_C1_PATH),
				new File(FILE_C2_PATH)));
	}

	@Test
	public void testFindInMultipleDirectories() {
		Collection<File> directories = asList(
				new File(EMPTY_DIRECTORY_PATH),
				new File(DIRECTORY_A_PATH),
				new File(DIRECTORY_B_PATH),
				new File(DIRECTORY_C_PATH));

		FileFinder finder = new FileFinder(directories, createSimpleFilter());
		assertThat(finder.find(), containsInAnyOrder(
				new File(FILE_A0_PATH),
				new File(FILE_A1_PATH),
				new File(FILE_A2_PATH),
				new File(FILE_B0_PATH),
				new File(FILE_B1_PATH),
				new File(FILE_B2_PATH),
				new File(FILE_C0_PATH),
				new File(FILE_C1_PATH),
				new File(FILE_C2_PATH)));
	}

	private FileFilter createSimpleFilter() {
		return new FileFilter() {

			public boolean accept(File file) {
				return file.isFile();
			}

		};
	}

}