package edu.cmu.a1.filter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;
/******************************************************************************************************************
 * File: TablePrinterSink.java
 * 
 * 
 * Description:
 *
 * Writes the record objects from the input streams to a text file.
 *
 ******************************************************************************************************************/

public class TablePrinterSink extends FilterFrameworkExtended {

	private FileOutputStream fileOutputStream;

	private Calendar TimeStamp = null;
	//	SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss:SSS");
	private SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss:SSS");

	private String headerString;
	//Order in which to write the fields to file
	private int[] fieldOrder;

	public TablePrinterSink(RecordDefinition recordDefinition,	FileOutputStream fileOutputStream, int[] fieldOrder) {
		super(recordDefinition);
		this.fileOutputStream = fileOutputStream;
		this.fieldOrder = fieldOrder;

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
		 *	Print the header to the output file
		 **************************************************************/
		try {
			
			String header = "";
			for(Integer fieldID : fieldOrder)
			{
			
				if (fieldID == 0) {
					header += "Time:                        "; }
				
				if (fieldID == 1) {
					header += "Velocity (kts):         "; }
				
				if (fieldID == 2) {
					header += "Altitude (m):       "; }
				
				if (fieldID == 3) {
					header += "Pressure (psi):     "; }
				
				if (fieldID == 4) {
					header += "Temperature (C):   "; }
				
				if (fieldID == 5) {
					header += "Attitude (deg):      "; }
			
			}
			fileOutputStream.write(header.getBytes());
			fileOutputStream.write("\n".getBytes());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}



		Integer[] codes = this.recordDefinition.getFieldCodes();
		while (this.inputsMap.size() > 0)
			for (Integer portID : inputsMap.keySet())

			{
				try
				{
					/***************************************************************************
				// We know that the first data coming to this filter is going to be an ID and
				// that it is IdLength long. So we first decommutate the ID bytes.
					 ****************************************************************************/

					Record r = readNextRecord(portID);


					//				System.out.println(r);
					writeRecordToFile(r);
					fileOutputStream.flush();
					//				if(this.inputsMap.get(portID).available() == 0)
					//					ClosePort(portID);
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




	private void writeDoubleToFile(Double value, String significantDigits) throws IOException {
		String valueString = String.format(significantDigits, value);
		fileOutputStream.write(valueString.getBytes());	
		fileOutputStream.write("\t \t \t \t".getBytes());	}

	private void writeStringToFile(String value) throws IOException {
		fileOutputStream.write(value.getBytes());
		fileOutputStream.write("\t".getBytes());	}

	private void writeRecordToFile(Record record) throws IOException  {
		
		for(Integer fieldID : fieldOrder)
		{
			try {
				Class<?> type = this.recordDefinition.getFieldType(fieldID);
				Object value = record.getValueByCode(fieldID);


				if (fieldID == 0) // Time
				{ String timeStamp = TimeStampFormat.format(value);
				timeStamp += "\t";
				writeStringToFile(timeStamp);	
				}


				if (fieldID == 1) { // Velocity 
					writeDoubleToFile((Double) value, "%04.5f");
				}

				if (fieldID == 2) { // Altitude

					String altitudeString = String.format("%04.5f", value);
					for (Integer ids : fieldOrder) {
						if (ids.equals(7)) {

							Object isExtrapolated = record.getValueByCode(7);
							Class<?> altitudeType = record.getTypeByCode(7);
							if (altitudeType == Double.TYPE || isExtrapolated instanceof Double) {
								Double intWild = (Double) isExtrapolated;
								if (intWild == 1) {
									altitudeString += "*";
								}
							}
							else if (altitudeType == Boolean.TYPE || isExtrapolated instanceof Boolean){
								Boolean bExtrapolated = (Boolean) isExtrapolated;
								if (bExtrapolated) {
									altitudeString += "*";
								}
							}
						}
					}
					altitudeString += "\t \t";
					writeStringToFile(altitudeString);
				}

				/**********************************************************************************************
				 * We search to see if there are wild pressure points, and if so we replace them with asterisks
				 **********************************************************************************************/
				if (fieldID == 3) { // Pressure
					String pressure_string = String.format("%04.5f", value);
					for (Integer ids : fieldOrder) {
						if (ids.equals(6)) {

							Object isWild = record.getValueByCode(6);
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
					pressure_string += "\t \t";
					writeStringToFile(pressure_string);
				}

				if (fieldID == 4) { // Temperature
					
				    DecimalFormat decim = new DecimalFormat("000.00000");
				    Double paddedValue = Double.parseDouble(decim.format(value));
				    String paddedValueString = paddedValue.toString();
				    paddedValueString += "\t \t  ";
				    writeStringToFile(paddedValueString);

				}

				if (fieldID ==5) { // Attitude
					writeDoubleToFile((Double) value, "%04.5f");
				}

			}
			catch (IllegalArgumentException e) {
				//Value not found in Record object, don't write
			}

		}
		fileOutputStream.write("\n".getBytes());

	}

}
