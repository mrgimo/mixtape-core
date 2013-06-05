package ch.hsr.mixtape.model;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.io.FileUtils;

public class SystemStatus {

	private DecimalFormat df;

	public SystemStatus() {
		df = (DecimalFormat) NumberFormat.getInstance();
		df.setMaximumFractionDigits(2);
	}

	/**
	 * Get the number of available cores for the JVM.
	 */
	public int getNumberOfCores() {
		return Runtime.getRuntime().availableProcessors();
	}

	/**
	 * Get the reserved memory for the system in megabytes.
	 * 
	 * @return Returns the available memory for the JVM. Depending on the size
	 *         the unit MB (Megabytes) or GB (Gigabytes) is added.
	 */
	public String getAvailableMemory() {
		long memory = Runtime.getRuntime().maxMemory();
		if (memory > 1073741824)
			return df.format(memory / 1073741824F) + " GB";
		return df.format(memory / 1048576F) + " MB";
	}

	/**
	 * Get the current system load in percent.
	 */
	public String getCurrentSystemLoad() {
		double load = ManagementFactory.getOperatingSystemMXBean()
				.getSystemLoadAverage();
		return (String) (load < 0 ? "n/v" : load);
		// // TODO
		// try {
		// long load = 0;
		// Cpu[] cpus = sigar.getCpuList();
		// for (Cpu cpu: cpus) {
		// load += cpu.getTotal();
		// }
		// } catch (SigarException e) {
		// e.printStackTrace();
		// }
		// return ManagementFactory.getOperatingSystemMXBean()
		// .getSystemLoadAverage();
	}

	/**
	 * Get the size in megabytes of the current database.
	 * 
	 * @return Returns the database size. Depending on the size the unit MB
	 *         (Megabytes) or GB (Gigabytes) is added.
	 */
	public String getDatabaseSize() {
		try {
			File file = new File(System.getenv("mixtapeData") + "mixtapeDB");
			long size = FileUtils.sizeOfDirectory(file);
			if (size > 1073741824)
				return df.format(size / 1073741824F) + " GB";
			return df.format(size / 1048576F) + " MB";
		} catch (Exception e) {
			return "n/v";
		}
	}

	/**
	 * Get the number of songs in database.
	 */
	public String getTotalNumberOfSongs() {
		return df.format(32105);
	}

	/**
	 * Get number of analyzed songs in database.
	 */
	public String getNumberOfAnalyzedSongs() {
		return df.format(32002);
	}

	/**
	 * Get number of songs in database waiting for analysis.
	 */
	public String getNumberOfPendingSongs() {
		return df.format(103);
	}

}
