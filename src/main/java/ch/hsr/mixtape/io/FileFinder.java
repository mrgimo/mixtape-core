package ch.hsr.mixtape.io;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class FileFinder {

	private final Collection<File> roots;
	private final FileFilter filter;

	private final Comparator<File> pathComparator = new Comparator<File>() {

		public int compare(File fileA, File fileB) {
			return fileA.getAbsolutePath().compareTo(fileB.getAbsolutePath());
		}

	};

	public FileFinder(Collection<File> roots, FileFilter filter) {
		this.roots = roots;
		this.filter = filter;
	}

	public List<File> find() {
		Set<File> foundFiles = Sets.newTreeSet(pathComparator);
		Set<File> directories = Sets.newTreeSet(pathComparator);

		for (File root : roots) {
			if (filter.accept(root))
				foundFiles.add(root);
			else if (root.isDirectory())
				directories.add(root);
		}

		return Lists.newArrayList(find(foundFiles, directories));
	}

	private Set<File> find(Set<File> foundFiles, Set<File> directories) {
		if (directories.isEmpty())
			return foundFiles;

		Set<File> subdirectories = Sets.newTreeSet(pathComparator);
		for (File directory : directories) {
			subdirectories.addAll(filterSubdirectories(directory));
			foundFiles.addAll(filterFiles(directory));
		}

		return find(foundFiles, subdirectories);
	}

	private List<File> filterFiles(File directory) {
		return Arrays.asList(directory.listFiles(filter));
	}

	private List<File> filterSubdirectories(File directory) {
		return Arrays.asList(directory.listFiles(new FileFilter() {

			public boolean accept(File file) {
				return file.isDirectory();
			}

		}));
	}

}