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
		
		int[] primaryFieldOrder = {000, 004, 002, 003, 006};
		int[] wildFieldOrder = {000, 003};
		
		FileSource sourceFilter = new FileSource(recordDef, "resources"+File.separator+"FlightData.dat");
		TemperatureConverter FToC = new TemperatureConverter(recordDef, 004);
		DistanceConverter ftTom = new DistanceConverter (recordDef, 002);
		PressureFilter pressureFilter = new PressureFilter (recordDef, 003, 006);
		
		FieldFilter wildPressureFilter = new FieldFilter(recordDef, new Integer[] {000, 003, 006});
		
		FieldFilter fieldFilter = new FieldFilter(recordDef, new Integer[] {000, 004, 002, 003, 006});
		FileOutputStream primaryFileOutputStream = new FileOutputStream(SystemOutputFilepath);
		TablePrinterSink sinkPrimary = new TablePrinterSink(recordDef, primaryFileOutputStream, primaryFieldOrder);
		FileOutputStream wildFileOutputStream = new FileOutputStream(WildFile);
		TablePrinterSink sinkWild = new TablePrinterSink(recordDef, wildFileOutputStream, wildFieldOrder);

		FToC.Connect(sourceFilter, OUTPUT, INPUT);
		ftTom.Connect(FToC, OUTPUT, INPUT);
		pressureFilter.Connect(ftTom, OUTPUT, INPUT);
		
		wildPressureFilter.Connect(pressureFilter, WILD_OUTPUT, INPUT);
		
		fieldFilter.Connect(pressureFilter, OUTPUT, INPUT);
		sinkPrimary.Connect(fieldFilter, OUTPUT, INPUT);

		sinkWild.Connect(wildPressureFilter, WILD_OUTPUT, INPUT);
		
//		sinkWild.Connect(pressureFilter, WILD_OUTPUT, INPUT);	
	
		sourceFilter.start();
		FToC.start();
		ftTom.start();
		pressureFilter.start();
		wildPressureFilter.start();
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
		
		String outfile1 = "OutputB.dat";
		String outfile2 = "WildPoints.dat";
				
		if(args.length > 0)
			infile = (args[0]);
		
		System.out.println("Starting System B with: " + infile);
		SystemB(infile, outfile1, outfile2);
		System.out.println("System B finished. Result files: " + outfile1 + " " + outfile2);
	}

}
