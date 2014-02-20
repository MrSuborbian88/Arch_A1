package edu.cmu.a1.filter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;

public class TablePrinterSink extends FilterFrameworkExtended {

	private FileOutputStream fileOutputStream;
	public TablePrinterSink(RecordDefinition recordDefinition,	FileOutputStream fileOutputStream) {
		// TODO Auto-generated constructor stub
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

		Calendar TimeStamp = Calendar.getInstance();
		SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss:SSS");

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

		System.out.print( "\n" + this.getName() + "::Sink Reading ");
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

					/*
					Integer idcode = readInt(portID);
					for(Integer code : codes) {
						if(idcode == code) {
							fileOutputStream.write(idcode);
							Class<?> type = this.recordDefinition.getFieldType(code);
							if(type == Integer.TYPE) { 
								fileOutputStream.write(readInt(portID));
								//writeField(portID,readInt(portID));
							}
							else if(type == Long.TYPE) {
								for (byte datum : ByteBuffer.allocate(8).putLong(readLong(portID)).array()) {
									fileOutputStream.write(datum);
									//writeField(portID,readLong(portID));
								}
							}
							else if(type == Double.TYPE) {
								for (byte datum : ByteBuffer.allocate(8).putDouble(readDouble(portID)).array()) {
									fileOutputStream.write(datum);
									//writeField(portID,readDouble(portID));
								}
							}
						}
						fileOutputStream.flush();
					}
				*/
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
					ClosePorts();
					System.out.print( "\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesread );
					break;

				} // catch
				catch (IOException e) {
					ClosePorts();
					System.out.print( "\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesread );
					break;
				}

			} // while

	} // run
	
	private void writeIntegerToFile(Integer value) throws IOException {
		for (byte datum : ByteBuffer.allocate(4).putInt(value).array())
			fileOutputStream.write(datum);	}
	private void writeLongToFile(Long value) throws IOException {
		for (byte datum : ByteBuffer.allocate(8).putLong(value).array())
			fileOutputStream.write(datum);
	}
	private void writeDoubleToFile(Double value) throws IOException {
		for (byte datum : ByteBuffer.allocate(8).putDouble(value).array())
			fileOutputStream.write(datum);
	}
	
	private void writeRecordToFile(Record record) throws IOException  {
		
		for(Integer fieldID : record.getCodes())
		{
			try {
				Class<?> type = this.recordDefinition.getFieldType(fieldID);
				Object value = record.getValueByCode(fieldID);
				
				writeIntegerToFile(fieldID);
				if(type == Integer.TYPE)
					writeIntegerToFile((Integer) value);
				else if(type == Long.TYPE)
					writeLongToFile((Long) value);
				else if(type == Double.TYPE)
					writeDoubleToFile((Double) value);
				else //Default behavior?
					writeDoubleToFile((Double) value);
			} catch (IllegalArgumentException e) {
				//Value not found in Record object, don't write
			}
			
		}

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
