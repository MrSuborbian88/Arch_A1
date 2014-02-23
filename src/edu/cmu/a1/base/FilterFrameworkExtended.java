package edu.cmu.a1.base;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

/******************************************************************************************************************
 * File:FilterFrameworkExtended.java
 * Course: 17655
 * Project: Assignment 1
 * Copyright: Copyright (c) 2013 Carnegie Mellon University
 * Versions:
 *	1.0 February 2013 - Initial version
 *
 * Description:
 *
 * This extension to the filter framework base class adds some convenience parsing 
 * routines to simplify the parsing.
 *
 ******************************************************************************************************************/


public class FilterFrameworkExtended extends FilterFramework
{

	protected RecordDefinition recordDefinition;

	protected HashMap<Integer, PipedInputStream> inputsMap;
	protected HashMap<Integer, PipedOutputStream> outputsMap;


	/***************************************************************************
	 * CONCRETE METHOD:: FilterFrameworkExtended
	 * Arguments:
	 * 	RecordDefinition - this defines the format of a record (field ids, types, names).
	 * Returns: void
	 *
	 *
	 ****************************************************************************/
	public FilterFrameworkExtended(RecordDefinition recordDefinition) {
		if(recordDefinition == null)
			throw new NullPointerException();
		this.recordDefinition=recordDefinition;
		this.inputsMap = new HashMap<Integer, PipedInputStream>();
		this.outputsMap = new HashMap<Integer, PipedOutputStream>();
	}
	
	//Private method to write a value to an output port
	private void writeField(Integer portID, Integer value) {
		if(outputsMap.containsKey(portID))
		{
			PipedOutputStream output = outputsMap.get(portID);
			for (byte datum : ByteBuffer.allocate(4).putInt(value).array()) {
				WriteFilterOutputPort(datum,output);
			}
		}
		else {
			throw new IllegalArgumentException("FieldID not connected.");
		}
	}
	//Private method to write a value to an output port
	private void writeField(Integer portID, Long value) {
		if(outputsMap.containsKey(portID))
		{
			PipedOutputStream output = outputsMap.get(portID);
			for (byte datum : ByteBuffer.allocate(8).putLong(value).array()) {
				WriteFilterOutputPort(datum,output);
			}
		}
		else {
			throw new IllegalArgumentException("FieldID not connected.");
		}
	}
	//Private method to write a value to an output port
	private void writeField(Integer portID, Double value) {
		if(outputsMap.containsKey(portID))
		{
			PipedOutputStream output = outputsMap.get(portID);
			for (byte datum : ByteBuffer.allocate(8).putDouble(value).array()) {
				WriteFilterOutputPort(datum,output);
			}
		}
		else {
			throw new IllegalArgumentException("FieldID not connected.");
		}
	}
	protected void WriteFilterOutputPort(byte datum, PipedOutputStream OutputWritePort)
	{
		try
		{
			OutputWritePort.write((int) datum );
			OutputWritePort.flush();

		} // try

		catch( Exception Error )
		{
			System.out.println("\n" + this.getName() + " Pipe write error::" + Error );

		} // catch

		return;

	} // WriteFilterPort
	
	/***************************************************************************
	 * This routine reads bytes from the current stream and assembles a new
	 * record (based on RecordDefinition).  The record data object can be used to format the output.
	 * @return: The next record or exception if there are no more records.
	 ****************************************************************************/
	private boolean first = true;
	public Record readNextRecord(Integer portID) throws IOException, EndOfStreamException {
		Record record = new Record(this.recordDefinition);


		int fieldID = -1;
		//Search for first 0
		if(first) {
			while(fieldID != 0)
				fieldID = readInt(portID);
			first = false;
		}
		else
			//Assume we stopped reading 
			fieldID = 0;
		do 
		{
			try {
				//			this.recordDefinition.getFieldType(fieldID);
				if(this.recordDefinition.hasFieldCode(fieldID))
				{
					Class<?> type = this.recordDefinition.getFieldType(fieldID);
					if(type == Integer.TYPE)
						record.setValueByCode(fieldID, readInt(portID));
					else if(type == Long.TYPE)
						record.setValueByCode(fieldID, readLong(portID));
					else if(type == Double.TYPE)
						record.setValueByCode(fieldID, readDouble(portID));
					else //Default behavior?
						record.setValueByCode(fieldID, readDouble(portID));
				}
				//Mark reset not supported by PipedInputStream
				//inputsMap.get(portID).mark(1024);
				fieldID = readInt(portID);
				if(fieldID == -1)
				{
					ClosePort(portID);
					break;
				}
			} catch(Exception e) {
				ClosePort(portID);
				return record;
			}
		} while(fieldID != 0);
		/*
		try {
		inputsMap.get(portID).reset();
		} catch(IOException e) {
			System.out.println(e.getMessage());
			//Could not reset...
		}
		 */
		return record;
	}
	/***************************************************************************
	 * This routine disassembles a record to the output stream.
	 * 
	 *  Arguments:
	 * 	portID - This output steam identifier to write the record to
	 *  record - A record object to write to the output stream
	 * @return: The next record or exception if there are no more records.
	 ****************************************************************************/

	public void writeRecord(Integer portID, Record record)  {

		for(Integer fieldID : record.getCodes())
		{

			try {
				Class<?> type = this.recordDefinition.getFieldType(fieldID);
				Object value = record.getValueByCode(fieldID);

				writeField(portID,fieldID);
				if(type == Integer.TYPE)
					writeField(portID,(Integer) value);
				else if(type == Long.TYPE)
					writeField(portID,(Long) value);
				else if(type == Double.TYPE)
					writeField(portID,(Double) value);
				else //Default behavior?
					writeField(portID,(Double) value);
			} catch (IllegalArgumentException e) {
				//Value not found in Record object, don't write
			}
		}

	}


	protected byte ReadFilterInputPort(Integer PortID) throws EndOfStreamException
	{
		if(!inputsMap.containsKey(PortID))
			throw new IllegalArgumentException("FieldID not connected.");

		PipedInputStream InputReadPort = inputsMap.get(PortID);
		byte datum = 0;

		/***********************************************************************
		 * If at least one byte of data is available on the input
		 * pipe we can read it. We read and write one byte to and from ports.
		 ***********************************************************************/
		try
		{
			datum = (byte)InputReadPort.read();
			return datum;

		} // try

		catch( Exception Error )
		{
//			System.out.println( "\n" + this.getName() + " Pipe read error::" + Error );
//			return datum;
			throw new EndOfStreamException();
		} // catch

	} // ReadFilterPort


	// Internal routine to read an integer from the stream	
	private Integer readInt(Integer PortID) throws EndOfStreamException
	{
		byte databyte = 0;
		int result = 0;

		for (int i=0; i < 4; ++i )
		{
			databyte = ReadFilterInputPort(PortID);

			result = result | (databyte & 0xFF);

			// Shift the whole thing to the left once if we aren't at the end
			if (i != 3)
			{
				result = result << 8;
			}
		}

		return result;
	}


	// Internal routine to read a long from the stream
	private long readLong(Integer PortID) throws EndOfStreamException
	{
		byte databyte = 0;
		long result = 0;

		for (int i=0; i < 8; ++i )
		{
			databyte = ReadFilterInputPort(PortID);

			result = result | (databyte & 0xFF);

			// Shift the whole thing to the left once if we aren't at the end
			if (i != 7)
			{
				result = result << 8;
			}
		}

		return result;
	}

	// Internal routine to read a double from the stream
	private double readDouble(Integer PortID) throws EndOfStreamException
	{
		byte databyte = 0;
		long measurement = 0;

		for (int i=0; i < 8; ++i )
		{
			databyte = ReadFilterInputPort(PortID);

			measurement = measurement | (databyte & 0xFF);

			// Shift the whole thing to the left once if we aren't at the end
			if (i != 7)
			{
				measurement = measurement << 8;
			}
		}

		return Double.longBitsToDouble(measurement);
	}

	/***************************************************************************
	 * CONCRETE METHOD:: Connect
	 * Purpose: This method connects filters to each other. All connections are
	 * through the inputport of each filter. That is each filter's inputport (local) is
	 * connected to another filter's output port (remote) through this method.
	 *
	 * Arguments:
	 * 	FilterFramework - this is the filter that this filter will connect to.
	 *  remote - this is the numeric reference to this filter's PipedInputStream
	 *  local - this is the numeric reference to the connected filter's PipedOutputStream
	 * Returns: void
	 *
	 ****************************************************************************/

	public void Connect( FilterFrameworkExtended Filter , Integer remote, Integer local)
	{

		try
		{
			// Connect this filter's input to the upstream pipe's output stream
			PipedInputStream thisfilter = new PipedInputStream();
			PipedOutputStream thatfilter = new PipedOutputStream();
			thisfilter.connect( thatfilter );
			//			InputFilter = Filter;

			inputsMap.put(local, thisfilter);
			//			outputsMap.put(remote, Filter.OutputWritePort);
			Filter.outputsMap.put(remote, thatfilter);
		} // try

		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " FilterFramework error connecting::"+ Error );

		} // catch
	} // Connect
	/***************************************************************************
	 * CONCRETE METHOD:: ClosePorts
	 * Purpose: Closes and removes all Input and Output Streams
	 *
	 ****************************************************************************/
	protected void ClosePorts()
	{
		try
		{
			for(PipedInputStream in : inputsMap.values())
			{
				in.close();
			}
			inputsMap.clear();
			for(PipedOutputStream out : outputsMap.values())
				out.close();
			outputsMap.clear();

		}
		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " ClosePorts error::" + Error );

		} // catch

	} // ClosePorts
	
	/***************************************************************************
	 * CONCRETE METHOD:: ClosePort
	 * Purpose: This method closes and removes any input or output stream referenced
	 * with the portID 
	 *
	 * Arguments:
	 * 	portID - Any input or output with this portID reference will be closed & removed.
	 * Returns: void
	 *
	 ****************************************************************************/
	protected void ClosePort(Integer portID)
	{
		try
		{
			if(this.inputsMap.containsKey(portID))
			{
				this.inputsMap.get(portID).close();

				this.inputsMap.remove(portID);
			}
		}
		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " ClosePorts error::" + Error );

		} // catch
		try
		{
			if(this.outputsMap.containsKey(portID))
			{
				this.outputsMap.get(portID).close();
				this.outputsMap.remove(portID);
			}

		}
		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " ClosePorts error::" + Error );

		} // catch

	} // ClosePorts

} // FilterFramework class