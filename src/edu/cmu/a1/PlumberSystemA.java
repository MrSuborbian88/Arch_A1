package edu.cmu.a1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import edu.cmu.a1.base.RecordDefinition;
import edu.cmu.a1.filter.DistanceConverter;
import edu.cmu.a1.filter.FieldFilter;
import edu.cmu.a1.filter.FileSource;
import edu.cmu.a1.filter.TablePrinterSink;
import edu.cmu.a1.filter.TemperatureConverter;

public class PlumberSystemA {

	private static final Integer OUTPUT = 2;
	private static final Integer INPUT = 1;

	public PlumberSystemA() {

	}
	
	public static void SystemA(String InputFilepath, String SystemOutputFilepath) throws FileNotFoundException {
		RecordDefinition recordDef = new RecordDefinition();

		recordDef.addFieldDefinition(000, Long.TYPE, "Time");
		recordDef.addFieldDefinition(001, Double.TYPE, "Velocity");
		recordDef.addFieldDefinition(002, Double.TYPE, "Altitude");
		recordDef.addFieldDefinition(003, Double.TYPE, "Pressure");
		recordDef.addFieldDefinition(004, Double.TYPE, "Temperature");
		recordDef.addFieldDefinition(005, Double.TYPE, "Attitude");

		String header = "Time:                        Temperature (C):     Altitude (m):     ";

		int[] fieldOrder = {000, 004, 002, 003, 001, 005};
		
		
		FileSource sourceFilter = new FileSource(recordDef, InputFilepath);
		TemperatureConverter FToC = new TemperatureConverter(recordDef, 004);
		DistanceConverter ftTom = new DistanceConverter (recordDef, 002);
		FieldFilter fieldFilter = new FieldFilter(recordDef, new Integer[] {000, 004, 002});
		FileOutputStream fileOutputStream = new FileOutputStream(SystemOutputFilepath);
		TablePrinterSink sinkFilter = new TablePrinterSink(recordDef, fileOutputStream, header, fieldOrder);

		FToC.Connect(sourceFilter, OUTPUT, INPUT);
		ftTom.Connect(FToC, OUTPUT, INPUT);
		fieldFilter.Connect(ftTom, OUTPUT, INPUT);
		sinkFilter.Connect(fieldFilter, OUTPUT, INPUT);

		sourceFilter.start();
		FToC.start();
		ftTom.start();
		fieldFilter.start();
		sinkFilter.start();
		
	}

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		String infile = "resources"+File.separator+"FlightData.dat";
		if(args.length > 0)
			infile = args[0];
		
		SystemA(infile,"resources"+File.separator+"OutputA.dat");
	}

}
