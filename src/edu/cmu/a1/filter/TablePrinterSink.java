package edu.cmu.a1.filter;

//Begin CFP
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;
// This class is used to interpret time words
//End CFP

public class TablePrinterSink extends FilterFrameworkExtended {

	private FileOutputStream fileOutputStream;
	
	Calendar TimeStamp = null;
//	SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss:SSS");
	SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss:SSS");

	
	public TablePrinterSink(RecordDefinition recordDefinition,	FileOutputStream fileOutputStream) {
		super(recordDefinition);
		this.fileOutputStream = fileOutputStream;
		
		

	}

	public void run()
	{
		/************************************************************************************
		 *	TimeStamp is used to compute time using java.util's Calendar class.
		 * 	TimeStampFormat is used to format the time value so that it can be easily printed
		 *	to the terminal.
		 *************************************************************************************/

//		Calendar TimeStamp = Calendar.getInstance();
//		SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss:SSS");

		int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
		int IdLength = 4;				// This is the length of IDs in the byte stream

		byte databyte = 0;				// This is the data byte read from the stream
		int bytesread = 0;				// This is the number of bytes read from the stream

		long measurement;				// This is the word used to store all measurements - conversions are illustrated.
		int id;							// This is the measurement id
		int i;							// This is a loop counter

		/*************************************************************
		 *	First we announce to the world that we are alive...
		 **************************************************************/


		String header = "Time:            Temperature (C):   Altitude (m): \n";
		try {
			fileOutputStream.write(header.getBytes());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
				
				
		Integer[] codes = this.recordDefinition.getFieldCodes();
		for (Integer portID : inputsMap.keySet())
			while (true)
			{
				try
				{
					/***************************************************************************
				// We know that the first data coming to this filter is going to be an ID and
				// that it is IdLength long. So we first decommutate the ID bytes.
					 ****************************************************************************/

				Record r = readNextRecord(portID);
				
				
				System.out.println(r);
				writeRecordToFile(r);
				fileOutputStream.flush();
				} // try

				/*******************************************************************************
				 *	The EndOfStreamExeception below is thrown when you reach end of the input
				 *	stream (duh). At this point, the filter ports are closed and a message is
				 *	written letting the user know what is going on.
				 ********************************************************************************/

				catch (EndOfStreamException e)
				{
//					ClosePorts();
					System.out.print( "\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesread );
					break;

				} // catch
				catch (IOException e) {
//					ClosePorts();
					System.out.print( "\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesread );
					break;
				}

			} // while

	} // run
	

	
	private void writeIntegerToFile(Integer value) throws IOException {
		String valueString = value.toString();
			fileOutputStream.write(valueString.getBytes());
			fileOutputStream.write("\t".getBytes());	}
	
	private void writeLongToFile(Long value) throws IOException {
		String valueString = value.toString();
		fileOutputStream.write(valueString.getBytes());	
		fileOutputStream.write("\t".getBytes());	}
	
	private void writeDoubleToFile(Double value) throws IOException {
		String valueString = String.format("%.5f", value);
		fileOutputStream.write(valueString.getBytes());	
		fileOutputStream.write("\t".getBytes());	}
	
	private void writeStringToFile(String value) throws IOException {
		fileOutputStream.write(value.getBytes());
		fileOutputStream.write("\t".getBytes());	}
	
	private void writeRecordToFile(Record record) throws IOException  {
		
		for(Integer fieldID : record.getCodes())
		{
			try {
				Class<?> type = this.recordDefinition.getFieldType(fieldID);
				Object value = record.getValueByCode(fieldID);

				
				if (fieldID == 0)
				{ String timestamp = TimeStampFormat.format(value);
					writeStringToFile(timestamp);	
				}
				
				/**********************************************************************************************
				 * We search to see if there are wild pressure points, and if so we replace them with asterisks
				 **********************************************************************************************/
				else if (fieldID == 3) {
					String pressure_string = String.format("%.5f", value);
					for (Integer ids : record.getCodes()) {
					    if (ids.equals(6)) {
					    	
					    	Object isWild = record.getValueByCode(6);
//					    	String pressure_type = record.getTitleByCode(6);
					    	Class<?> pressure_type = record.getTypeByCode(6);
					    	
					    	if (pressure_type == Double.TYPE || isWild instanceof Double) {
					    		Double intWild = (Double) isWild;
					    		if (intWild == 1) {
							    	pressure_string += "*";
							    	}
					    	}
					    	else if (pressure_type == Boolean.TYPE || isWild instanceof Boolean){
					    		Boolean bWild = (Boolean) isWild;
					    		if (bWild) {
							    	pressure_string += "*";
							    	}
					    	}

					    }
					}
					writeStringToFile(pressure_string);
				}
				
				else if (fieldID == 6 || fieldID == 7) {
					continue;
				}
				
				else {
				if(type == Integer.TYPE)
					writeIntegerToFile((Integer) value);
				else if(type == Long.TYPE)
					writeLongToFile((Long) value);
				else if(type == Double.TYPE)
					writeDoubleToFile((Double) value);
				else //Default behavior?
					writeDoubleToFile((Double) value);
				}
				}
			catch (IllegalArgumentException e) {
				//Value not found in Record object, don't write
			}
			
		}
		fileOutputStream.write("\n".getBytes());

	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
