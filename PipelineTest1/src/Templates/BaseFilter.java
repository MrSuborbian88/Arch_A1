package Templates;

import java.nio.ByteBuffer;

public class BaseFilter extends FilterFramework
{
	// these should be properties with getters and setters, blah	
	protected long measurement;				// This is the word used to store all measurements - conversions are illustrated.
	protected int id;							// This is the measurement id	
	
	byte[] idBytes = new byte[4];
	byte[] measurementBytes = new byte[8];
	
	int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
	int IdLength = 4;				// This is the length of IDs in the byte stream

	byte databyte = 0;				// This is the data byte read from the stream
	int bytesread = 0;				// This is the number of bytes read from the stream
			
	int i;		
	
	boolean endOfFileReached = false;
	
	@Override public void run()
    {
							// This is a loop counter

				
		/*************************************************************
		*	First we announce to the world that we are alive...
		**************************************************************/

		System.out.print( "\n" + this.getName() + "::Altitude Reading ");

		while (endOfFileReached == false)
		{
			GetNextIdAndMeasurementFromInputStream();

			/****************************************************************************
			// Do inner work polymorphically
			****************************************************************************/
			DoInnerWork();

			
			SendIdAndMeasurementToOutputStream();
			

			System.out.print( "\n" );			

		} // while
		
		DoFinalWork();

   } // run
	
	protected void GetNextIdAndMeasurementFromInputStream()
	{
		try
		{
			/***************************************************************************
			// We know that the first data coming to this filter is going to be an ID and
			// that it is IdLength long. So we first decommutate the ID bytes.
			****************************************************************************/
	
			id = 0;
	
			for (i=0; i<IdLength; i++ )
			{
				databyte = ReadFilterInputPort();	// This is where we read the byte from the stream...
	
				idBytes[i] = databyte;
				
				id = id | (databyte & 0xFF);		// We append the byte on to ID...
	
				if (i != IdLength-1)				// If this is not the last byte, then slide the
				{									// previously appended byte to the left by one byte
					id = id << 8;					// to make room for the next byte we append to the ID
	
				} // if
	
				bytesread++;						// Increment the byte count
	
			} // for
	
			/****************************************************************************
			// Here we read measurements. All measurement data is read as a stream of bytes
			// and stored as a long value. This permits us to do bitwise manipulation that
			// is neccesary to convert the byte stream into data words. Note that bitwise
			// manipulation is not permitted on any kind of floating point types in Java.
			// If the id = 0 then this is a time value and is therefore a long value - no
			// problem. However, if the id is something other than 0, then the bits in the
			// long value is really of type double and we need to convert the value using
			// Double.longBitsToDouble(long val) to do the conversion which is illustrated.
			// below.
			*****************************************************************************/
	
			measurement = 0;
	
			for (i=0; i<MeasurementLength; i++ )
			{
				databyte = ReadFilterInputPort();
				
				measurementBytes[i] = databyte;
				
				measurement = measurement | (databyte & 0xFF);	// We append the byte on to measurement...
	
				if (i != MeasurementLength-1)					// If this is not the last byte, then slide the
				{												// previously appended byte to the left by one byte
					measurement = measurement << 8;				// to make room for the next byte we append to the
																// measurement
				} // if
	
				bytesread++;									// Increment the byte count
	
			} // if
					
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
			
			endOfFileReached = true;
		} // catch
	}
	
	public double GetMeasurementAsDouble()
	{
		return Double.longBitsToDouble(measurement);
	}
	
	
	protected void DoInnerWork()
	{
	}
	
	protected void SendIdAndMeasurementToOutputStream()
	{
				
		for(int x = 0; x < 4; x++)
		{
			this.WriteFilterOutputPort(idBytes[x]);
		}
		
		for(int x = 0; x < 8; x++)
		{
			this.WriteFilterOutputPort(measurementBytes[x]);
		}		
		
	}
	
	protected void UpdateMeasurementBytesFromDouble(double value)
	{		
		byte[] doubleBytes = new byte[8];
		ByteBuffer.wrap(doubleBytes).putDouble(value);
		
		for(int x = 0; x < 8; x++)
		{
			measurementBytes[x] = doubleBytes[x];
		}			
		
	}

	protected void DoFinalWork()
	{
		// this happens when the end of file is reached
	}

}
