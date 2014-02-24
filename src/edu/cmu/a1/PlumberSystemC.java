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
	public static void SystemC(String InputFilepath1,String InputFilepath2, String SystemOutputFilepath, String WildFile1, String WildFile2) throws FileNotFoundException {

		RecordDefinition recordDef = new RecordDefinition();

		recordDef.addFieldDefinition(000, Long.TYPE, "Time");
		recordDef.addFieldDefinition(001, Double.TYPE, "Velocity");
		recordDef.addFieldDefinition(002, Double.TYPE, "Altitude");
		recordDef.addFieldDefinition(003, Double.TYPE, "Pressure");
		recordDef.addFieldDefinition(004, Double.TYPE, "Temperature");
		recordDef.addFieldDefinition(005, Double.TYPE, "Attitude");

		// This will require special behavior in the TablePrinter
		recordDef.addFieldDefinition(006, Double.TYPE, "ExtrapolatedPressure");
		recordDef.addFieldDefinition(007, Double.TYPE, "ExtrapolatedAltitude");

		
		int[] primaryFieldOrder = {000, 004, 002, 003, 001, 005, 006, 007};
		int[] wildFieldOrder = {000, 003};
		int[] under10KFieldOrder = {000, 002};
		
		FileSource firstSourceFilter = new FileSource(recordDef, InputFilepath1);
		FileSource secondSourceFilter = new FileSource(recordDef, InputFilepath2);

		TimeMerge mergeFilter = new TimeMerge(recordDef, 000);
		AltitudeFilter altitudeFilter = new AltitudeFilter(recordDef, 002, 007);
		FieldFilter wildAltitudeFilter = new FieldFilter(recordDef, new Integer[] {000, 002, 007});
		
		PressureFilter pressureFilter = new PressureFilter (recordDef, 003, 006);
		FieldFilter wildPressureFilter = new FieldFilter(recordDef, new Integer[] {000, 003, 006});
		
		FileOutputStream primaryFileOutputStream = new FileOutputStream(SystemOutputFilepath);
		FileOutputStream wildPressureFileOutputStream = new FileOutputStream(WildFile1);
		FileOutputStream wildAltitudeFileOutputStream = new FileOutputStream(WildFile2);
		FieldFilter fieldFilter = new FieldFilter(recordDef, new Integer[] {000, 004, 002, 003, 005, 001, 006, 007});

		
		TablePrinterSink sinkPrimary = new TablePrinterSink(recordDef, primaryFileOutputStream, primaryFieldOrder);
		TablePrinterSink sinkWildPressure = new TablePrinterSink(recordDef, wildPressureFileOutputStream, wildFieldOrder);
		TablePrinterSink sinkWildAltitude = new TablePrinterSink(recordDef, wildAltitudeFileOutputStream, under10KFieldOrder);

		mergeFilter.Connect(firstSourceFilter, 3, 11);
		mergeFilter.Connect(secondSourceFilter, 4, 12);

		altitudeFilter.Connect(mergeFilter, 13, 14);
		wildAltitudeFilter.Connect(altitudeFilter, WILD_OUTPUT1, 32);
		sinkWildAltitude.Connect(wildAltitudeFilter, 31, 15);

		pressureFilter.Connect(altitudeFilter, 16, 17);
		wildPressureFilter.Connect(pressureFilter, WILD_OUTPUT2, 34);
		sinkWildPressure.Connect(wildPressureFilter, 33, 18);
		
		fieldFilter.Connect(pressureFilter, 22, 19);
		sinkPrimary.Connect(fieldFilter, 23, 24);
		
		firstSourceFilter.start();
		secondSourceFilter.start();
		mergeFilter.start();
		altitudeFilter.start();
		wildAltitudeFilter.start();
		pressureFilter.start();
		wildPressureFilter.start();
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
		String infile1 = "resources"+File.separator+"SubSetA-2009.dat";
		String infile2 = "resources"+File.separator+"SubSetB-2009.dat";
//		String infile2 = "resources"+File.separator+"FlightData.dat";
		
		String outfile1 = "OutputC.dat";
		String outfile2 = "PressureWildPoints.dat";
		String outfile3 = "LessThan10K.dat";
		
//		String outfile1 = "resources"+File.separator+"OutputC.dat";
//		String outfile2 = "resources"+File.separator+"PressureWildPoints.dat";
//		String outfile3 = "resources"+File.separator+"LessThan10K.dat";
		
		if(args.length > 0)
			infile1 = (args[0]);
		if(args.length > 1)
			infile2 = (args[1]);
		
		System.out.println("Starting System C with: " + infile1 + " " + infile2);
		SystemC(infile1,infile2,outfile1,outfile2,outfile3);
		System.out.println("System C finished. Result files: " + outfile1 + " " + outfile2 + " " + outfile3);
	}

}
