package edu.cmu.a1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import edu.cmu.a1.base.RecordDefinition;
import edu.cmu.a1.filter.DistanceConverter;
import edu.cmu.a1.filter.FieldFilter;
import edu.cmu.a1.filter.FileSource;
import edu.cmu.a1.filter.PressureFilter;
import edu.cmu.a1.filter.TablePrinterSink;
import edu.cmu.a1.filter.TemperatureConverter;

public class PlumberSystemB {

	private static final Integer OUTPUT = 1;
	private static final Integer INPUT = 2;
	private static final Integer WILD_OUTPUT = 6;

	public PlumberSystemB() {
	}
	public static void SystemB(String InputFilepath, String SystemOutputFilepath, String WildFile) throws FileNotFoundException {
		RecordDefinition recordDef = new RecordDefinition();

		recordDef.addFieldDefinition(000, Long.TYPE, "Time");
		recordDef.addFieldDefinition(001, Double.TYPE, "Velocity");
		recordDef.addFieldDefinition(002, Double.TYPE, "Altitude");
		recordDef.addFieldDefinition(003, Double.TYPE, "Pressure");
		recordDef.addFieldDefinition(004, Double.TYPE, "Temperature");
		recordDef.addFieldDefinition(005, Double.TYPE, "Attitude");

		// This will require special behavior in the TablePrinter
		recordDef.addFieldDefinition(006, Double.TYPE, "WildPressure");
		
		// Headers for output file
		String primaryHeader = "Time:     Temperature (C):     Altitude (m):     Pressure (psi):";
		String wildHeader = "Time:     Pressure (psi):";

		FileSource sourceFilter = new FileSource(recordDef, "resources"+File.separator+"FlightData.dat");
		TemperatureConverter FToC = new TemperatureConverter(recordDef, 004);
		DistanceConverter ftTom = new DistanceConverter (recordDef, 002);
		PressureFilter pressureFilter = new PressureFilter (recordDef, 003, 006);
		FieldFilter fieldFilter = new FieldFilter(recordDef, new Integer[] {000, 004, 002, 003, 006});
		FileOutputStream primaryFileOutputStream = new FileOutputStream("resources"+File.separator+"OutputB.dat");
		TablePrinterSink sinkPrimary = new TablePrinterSink(recordDef, primaryFileOutputStream, primaryHeader);
		FileOutputStream wildFileOutputStream = new FileOutputStream(WildFile);
		TablePrinterSink sinkWild = new TablePrinterSink(recordDef, wildFileOutputStream, wildHeader);

		FToC.Connect(sourceFilter, OUTPUT, INPUT);
		ftTom.Connect(FToC, OUTPUT, INPUT);
		pressureFilter.Connect(ftTom, OUTPUT, INPUT);
		fieldFilter.Connect(pressureFilter, OUTPUT, INPUT);
		sinkPrimary.Connect(fieldFilter, OUTPUT, INPUT);

		sinkWild.Connect(pressureFilter, WILD_OUTPUT, INPUT);	
	
		sourceFilter.start();
		FToC.start();
		ftTom.start();
		pressureFilter.start();
		fieldFilter.start();
		sinkPrimary.start();
		sinkWild.start();
		
	}

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		String infile = "resources"+File.separator+"FlightData.dat";
		if(args.length > 0)
			infile = (args[0]);
		SystemB(infile,"resources"+File.separator+"outB.dat","resources"+File.separator+"WildPoints.dat");
	}

}
