package edu.cmu.a1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import edu.cmu.a1.base.RecordDefinition;
import edu.cmu.a1.filter.AltitudeFilter;
import edu.cmu.a1.filter.FieldFilter;
import edu.cmu.a1.filter.FileSource;
import edu.cmu.a1.filter.PressureFilter;
import edu.cmu.a1.filter.TablePrinterSink;
import edu.cmu.a1.filter.TimeMerge;

public class PlumberSystemC {

	private static final Integer OUTPUT = 1;
	private static final Integer INPUT = 2;
	private static final Integer WILD_OUTPUT1 = 007;
	private static final Integer WILD_OUTPUT2 = 006;

	public PlumberSystemC() {
	}
	public static void SystemC() throws FileNotFoundException {

		RecordDefinition recordDef = new RecordDefinition();

		recordDef.addFieldDefinition(000, Long.TYPE, "Time");
		recordDef.addFieldDefinition(001, Double.TYPE, "Velocity");
		recordDef.addFieldDefinition(002, Double.TYPE, "Altitude");
		recordDef.addFieldDefinition(003, Double.TYPE, "Pressure");
		recordDef.addFieldDefinition(004, Double.TYPE, "Temperature");
		recordDef.addFieldDefinition(005, Double.TYPE, "Attitude");

		// This will require special behavior in the TablePrinter
		recordDef.addFieldDefinition(006, Boolean.TYPE, "ExtrapolatedPressure");
		recordDef.addFieldDefinition(007, Boolean.TYPE, "ExtrapolatedAltitude");


		FileSource firstSourceFilter = new FileSource(recordDef, "resources"+File.separator+"SubSetA-2009.dat");
		FileSource secondSourceFilter = new FileSource(recordDef, "resources"+File.separator+"SubSetB-2009.dat");
//		FileSource secondSourceFilter = new FileSource(recordDef, "resources"+File.separator+"FlightData.dat");

		TimeMerge mergeFilter = new TimeMerge(recordDef, 000);
		AltitudeFilter altitudeFilter = new AltitudeFilter(recordDef, 002, 007);
		PressureFilter pressureFilter = new PressureFilter (recordDef, 003, 006);

		FileOutputStream primaryFileOutputStream = new FileOutputStream("resources"+File.separator+"outc.dat");
		FileOutputStream wildPressureFileOutputStream = new FileOutputStream("resources"+File.separator+"PressureWildPoints.dat");
		FileOutputStream wildAltitudeFileOutputStream = new FileOutputStream("resources"+File.separator+"LessThan10K.dat");
		FieldFilter fieldFilter = new FieldFilter(recordDef, new Integer[] {000, 004, 002, 003});

		
		TablePrinterSink sinkPrimary = new TablePrinterSink(recordDef, primaryFileOutputStream);
		TablePrinterSink sinkWildPressure = new TablePrinterSink(recordDef, wildPressureFileOutputStream);
		TablePrinterSink sinkWildAltitude = new TablePrinterSink(recordDef, wildAltitudeFileOutputStream);

		mergeFilter.Connect(firstSourceFilter, 3, INPUT);
		mergeFilter.Connect(secondSourceFilter, 4, INPUT);

		altitudeFilter.Connect(mergeFilter, OUTPUT, INPUT);
		sinkWildAltitude.Connect(altitudeFilter, WILD_OUTPUT1, INPUT);

		pressureFilter.Connect(altitudeFilter, OUTPUT, INPUT);
		sinkWildPressure.Connect(pressureFilter, WILD_OUTPUT2, INPUT);
		
		fieldFilter.Connect(pressureFilter, OUTPUT, INPUT);
		sinkPrimary.Connect(fieldFilter, OUTPUT, INPUT);
		
		firstSourceFilter.start();
		secondSourceFilter.start();
		mergeFilter.start();
		altitudeFilter.start();
		pressureFilter.start();
		fieldFilter.start();
		sinkPrimary.start();
		sinkWildPressure.start();
		sinkWildAltitude.start();
		
	}

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		SystemC();

	}

}
