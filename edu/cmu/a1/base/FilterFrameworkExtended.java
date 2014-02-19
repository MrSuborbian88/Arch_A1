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



	public FilterFrameworkExtended(RecordDefinition recordDefinition) {
		if(recordDefinition == null)
			throw new NullPointerException();
		this.recordDefinition=recordDefinition;
		this.inputsMap = new HashMap<Integer, PipedInputStream>();
		this.outputsMap = new HashMap<Integer, PipedOutputStream>();
	}
	public void writeField(Integer portID, Integer value) {
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
	public void writeField(Integer portID, Long value) {
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
	public void writeField(Integer portID, Double value) {
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
	public void WriteFilterOutputPort(byte datum, PipedOutputStream OutputWritePort)
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

	public Record readNextRecord(Integer portID) throws IOException, EndOfStreamException {
		Record record = new Record(this.recordDefinition);

		int fieldID = -1;
		while(fieldID != 0)
			fieldID = readInt(portID);
		do 
		{

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
			inputsMap.get(portID).mark(4);
			fieldID = readInt(portID);			
		} while(fieldID != 0);
		//
		try {
		inputsMap.get(portID).reset();
		} catch(IOException e) {
			//Could not reset...
		}
		return record;
	}
	public void writeRecord(Integer portID, Record record)  {
		
		for(Integer fieldID : record.getCodes())
		{
			Class<?> type = this.recordDefinition.getFieldType(fieldID);
			writeField(portID,fieldID);
			if(type == Integer.TYPE)
				writeField(portID,(Integer) record.getValueByCode(fieldID));
			else if(type == Long.TYPE)
				writeField(portID,(Long) record.getValueByCode(fieldID));
			else if(type == Double.TYPE)
				writeField(portID,(Double) record.getValueByCode(fieldID));
			else //Default behavior?
				writeField(portID,(Double) record.getValueByCode(fieldID));
		}

	}

	/***************************************************************************
	 * This routine reads bytes from the current stream and assembles a new
	 * record.  The record data object can be used to format the output.
	 * @return: The next record or null if no more records.
	 ****************************************************************************/

	private FlightDataRecord readNextFlightRecord(Integer PortID)
	{
		//		if(!inputsMap.containsKey(PortID))
		//			throw new IllegalArgumentException("FieldID not connected.");
		//		FilterFramework field = inputsMap.get(PortID);
		//		if (field.endOfStream)
		//		{
		//			return null;
		//		}

		// Make a flight data record
		FlightDataRecord record = new FlightDataRecord();

		try 
		{
			// If we have a next time object then use it, because it is the 
			// one we parsed (cached) from the end of the last readNextRecord which
			// indicated a new record.  See case 0 below for how/when we cache it.
			if (nextTime_ != null)
			{
				record.setTime(nextTime_);
				nextTime_ = null;
			}
			else
			{
				// This is the first time, prime the pump by reading the time record.
				int id = readInt(PortID);
				// TODO:  Check the id for validity
				record.setTime(readLong(PortID));
			}

			// Read records until we find the next time stamp or get an end of stream
			while (true)
			{
				int id = readInt(PortID);
				switch (id)
				{
				case 0: // Time 
					// This means we are at the next record and we are done.
					// Since we can't push it back on the stream, cache it off
					// until the next read.
					nextTime_ = readLong(PortID);
					return record;

				case 1: // Velocity
					record.setVelocity(readDouble(PortID));
					break;

				case 2: // Altitude
					record.setAltitude(readDouble(PortID));
					break;

				case 3: // Pressure
					record.setPressure(readDouble(PortID));
					break;

				case 4: // Temperature
					record.setTemperature(readDouble(PortID));
					break;

				case 5: // Attitude
					record.setAttitude(readDouble(PortID));
					break;
				}
			}
		}
		catch (EndOfStreamException e)
		{
			// We get this at the end, when we are looking for more data.
			endOfStream_ = true;

			// Send back what we have.
			return record;
		}
	}

	protected byte ReadFilterInputPort(Integer PortID) throws EndOfStreamException
	{
		if(!inputsMap.containsKey(PortID))
			throw new IllegalArgumentException("FieldID not connected.");

		PipedInputStream InputReadPort = inputsMap.get(PortID);
		byte datum = 0;

		/***********************************************************************
		 * Since delays are possible on upstream filters, we first wait until
		 * there is data available on the input port. We check,... if no data is
		 * available on the input port we wait for a quarter of a second and check
		 * again. Note there is no timeout enforced here at all and if upstream
		 * filters are deadlocked, then this can result in infinite waits in this
		 * loop. It is necessary to check to see if we are at the end of stream
		 * in the wait loop because it is possible that the upstream filter completes
		 * while we are waiting. If this happens and we do not check for the end of
		 * stream, then we could wait forever on an upstream pipe that is long gone.
		 * Unfortunately Java pipes do not throw exceptions when the input pipe is
		 * broken. So what we do here is to see if the upstream filter is alive.
		 * if it is, we assume the pipe is still open and sending data. If the
		 * filter is not alive, then we assume the end of stream has been reached.
		 ***********************************************************************/
		/*
		try
		{
			while (InputReadPort.available()==0 )
			{
				if (EndOfInputStream())
				{
					throw new EndOfStreamException("End of input stream reached");

				} //if

				sleep(250);

			} // while

		} // try

		catch( EndOfStreamException Error )
		{
			throw Error;

		} // catch

		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " Error in read port wait loop::" + Error );

		} // catch
		 */
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
			System.out.println( "\n" + this.getName() + " Pipe read error::" + Error );
			return datum;

		} // catch

	} // ReadFilterPort


	// Internal routine to read an integer from the stream	
	public Integer readInt(Integer PortID) throws EndOfStreamException
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
	protected long readLong(Integer PortID) throws EndOfStreamException
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
	protected double readDouble(Integer PortID) throws EndOfStreamException
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

	// This is where we cache the time from the beginning of the next record
	// when we are parsing.
	protected Long nextTime_ = null;

	// Once we hit the end of the stream, we set to true so the next 
	// read knows to return null.
	protected boolean endOfStream_ = false;
	/***************************************************************************
	 * CONCRETE METHOD:: Connect
	 * Purpose: This method connects filters to each other. All connections are
	 * through the inputport of each filter. That is each filter's inputport is
	 * connected to another filter's output port through this method.
	 *
	 * Arguments:
	 * 	FilterFramework - this is the filter that this filter will connect to.
	 *
	 * Returns: void
	 *
	 * Exceptions: IOException
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
	protected void ClosePorts()
	{
		try
		{
			for(PipedInputStream in : inputsMap.values())
				in.close();
			for(PipedOutputStream out : outputsMap.values())
				out.close();

		}
		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " ClosePorts error::" + Error );

		} // catch

	} // ClosePorts

} // FilterFramework class