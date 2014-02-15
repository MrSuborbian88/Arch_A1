package edu.cmu.a1.base;
import edu.cmu.a1.base.FilterFramework;

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
	public FilterFrameworkExtended(RecordDefinition recordDefinition) {
		//TODO:
	}
	public void writeField(Integer FieldID, Integer value) {
		//TODO:
	}
	public void writeField(Integer FieldID, Long value) {
		//TODO:
	}
	public void writeField(Integer FieldID, Double value) {
		//TODO:
	}
	
	/***************************************************************************
	* This routine reads bytes from the current stream and assembles a new
	* record.  The record data object can be used to format the output.
	* @return: The next record or null if no more records.
	****************************************************************************/
	protected FlightDataRecord readNextRecord()
	{
		if (endOfStream_)
		{
			return null;
		}
	
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
				int id = readInt();
				// TODO:  Check the id for validity
				record.setTime(readLong());
			}
			
			// Read records until we find the next time stamp or get an end of stream
			while (true)
			{
				int id = readInt();
				switch (id)
				{
					case 0: // Time 
						// This means we are at the next record and we are done.
						// Since we can't push it back on the stream, cache it off
						// until the next read.
						nextTime_ = readLong();
						return record;
						
					case 1: // Velocity
						record.setVelocity(readDouble());
						break;
						
					case 2: // Altitude
						record.setAltitude(readDouble());
						break;

					case 3: // Pressure
						record.setPressure(readDouble());
						break;

					case 4: // Temperature
						record.setTemperature(readDouble());
						break;

					case 5: // Attitude
						record.setAttitude(readDouble());
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
	
    // Internal routine to read an integer from the stream
	protected int readInt() throws EndOfStreamException
	{
		byte databyte = 0;
		int result = 0;

		for (int i=0; i < 4; ++i )
		{
			databyte = ReadFilterInputPort();
	
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
	protected long readLong() throws EndOfStreamException
	{
		byte databyte = 0;
		long result = 0;

		for (int i=0; i < 8; ++i )
		{
			databyte = ReadFilterInputPort();
	
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
	protected double readDouble() throws EndOfStreamException
	{
		byte databyte = 0;
		long measurement = 0;

		for (int i=0; i < 8; ++i )
		{
			databyte = ReadFilterInputPort();
	
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

	public void Connect( FilterFramework Filter , Integer remote, Integer local)
	{
		//TODO
		/*
		try
		{
			// Connect this filter's input to the upstream pipe's output stream

			InputReadPort.connect( Filter.OutputWritePort );
			InputFilter = Filter;

		} // try

		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " FilterFramework error connecting::"+ Error );

		} // catch
		*/
	} // Connect
	
} // FilterFramework class