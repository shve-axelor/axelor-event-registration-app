package com.axelor.eventregistration.service;

import com.axelor.data.csv.CSVImporter;
import com.axelor.event.registration.db.Discount;
import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.Period;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class EventRegistrationServiceImpl implements EventRegistrationService {

	@Override
	public void calculateAmount(Event event, EventRegistration eventregistration) {
		List<Discount> discountList = event.getDiscountList();
		Period endPeriod = Period.between(eventregistration.getRegistrationDate().toLocalDate(),
				event.getRegCloseDate());
		if (discountList != null && !discountList.isEmpty()) {
			Comparator<Discount> comparator = (Discount d1, Discount d2) -> d1.getBeforeDays()
					.compareTo(d2.getBeforeDays());
			discountList.sort(comparator.reversed());
			for (Discount discount : event.getDiscountList()) {
				if (endPeriod.getDays() >= discount.getBeforeDays()) {
					eventregistration.setAmount(event.getEventFees().subtract(discount.getDiscountAmount()));
					break;
				} else {
					eventregistration.setAmount(event.getEventFees());
				}
			}
		} else {
			eventregistration.setAmount(event.getEventFees());
		}
	}

	@Override
	public void importEventRegistration(MetaFile dataFile) {

		try {
			File tempDir = Files.createTempDir();
			File configXmlFile = this.getConfigXmlFile();
			File dataCsvFile = this.getDataCsvFile(dataFile, tempDir);

			importEventRegistrationData(configXmlFile.getAbsolutePath(), tempDir);
			this.deleteTempFiles(configXmlFile, dataCsvFile);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private File getConfigXmlFile() {
		File configFile = null;
		try {
			configFile = File.createTempFile("input-config", ".xml");

			InputStream bindFileInputStream = this.getClass()
					.getResourceAsStream("/import-configs/csv-multi-config.xml");

			if (bindFileInputStream == null) {
				throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "");
			}

			FileOutputStream outputStream = new FileOutputStream(configFile);

			IOUtils.copy(bindFileInputStream, outputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return configFile;
	}

	private File getDataCsvFile(MetaFile dataFile, File tempDir) {
		File csvFile = null;
		try {

			csvFile = new File(tempDir, "event_registration.csv");

			Files.copy(MetaFiles.getPath(dataFile).toFile(), csvFile);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return csvFile;
	}

	private void deleteTempFiles(File configXmlFile, File dataCsvFile) {
		try {
			if (configXmlFile.isDirectory() && dataCsvFile.isDirectory()) {
				FileUtils.deleteDirectory(configXmlFile);
				FileUtils.deleteDirectory(dataCsvFile);
			} else {
				configXmlFile.delete();
				dataCsvFile.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void importEventRegistrationData(String configXmlFile, File tempDir) {
		CSVImporter csvImporter = new CSVImporter(configXmlFile, tempDir.getAbsolutePath());
		csvImporter.run();
	}

	@Override
	public boolean checkEventCapacity(EventRegistration eventRegistration) {
		if (eventRegistration.getEvent().getCapacity() == eventRegistration.getEvent().getEventRegistrationList()
				.size()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean checkEventRegistrationDate(EventRegistration eventRegistration) {
		Period endPeriod = Period.between(eventRegistration.getRegistrationDate().toLocalDate(),
				eventRegistration.getEvent().getRegCloseDate());
		Period startPeriod = Period.between(eventRegistration.getEvent().getRegOpenDate(),
				eventRegistration.getRegistrationDate().toLocalDate());
		if (endPeriod.getDays() < 0 || startPeriod.getDays() < 0) {
			return true;
		}
		return false;
	}

}
