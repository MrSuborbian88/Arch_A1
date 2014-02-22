package edu.cmu.a1.filter;

import java.io.IOException;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;

public class AltitudeFilter extends FilterFrameworkExtended {

	private Integer ALTITUDE_ID = 2;
	private Integer WILD_ID = 7;
	private Double previous_value = -1.0;
	private Integer wildPort;

	private Double WILDPOINT = 1.0;
	private Double UNWILDPOINT = 0.0;

	public AltitudeFilter(RecordDefinition recordDefinition, Integer FieldID, Integer WildIndicatorPortId)
	{
		super(recordDefinition);
		this.ALTITUDE_ID=FieldID;
		this.WILD_ID=WildIndicatorPortId;
		this.wildPort=WildIndicatorPortId;
	}
	public boolean DoInnerWork(Record record )
	{
		try {

			Double current_value = (Double) record.getValueByCode(ALTITUDE_ID);
			current_value = (Double) record.getValueByCode(002);

			if(current_value >= 10000)
			{
				record.setValueByCode(WILD_ID, UNWILDPOINT);
				return true;
			}

		} catch (IllegalArgumentException e) {
			//No Altitude
		}
		record.setValueByCode(WILD_ID, WILDPOINT);
		return false;
	}
	public void run()
	{


		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		byte databyte = 0;					// The byte of data read from the file

		// Next we write a message to the terminal to let the world know we are alive...

		//	System.out.print( "\n" + this.getName() + "::Middle Reading ");

		while (this.inputsMap.size() > 0)
		{
			/*************************************************************
			 *	Here we read a byte and write a byte
			 *************************************************************/
			for(Integer portID : this.inputsMap.keySet()) {
				try
				{
					Record record = readNextRecord(portID);
					boolean greaterthan10k = DoInnerWork(record);
					if(greaterthan10k)
					{
						for(Integer outID : this.outputsMap.keySet())
							if(outID != this.wildPort)
								writeRecord(outID,record);
					}
					else
					{
						writeRecord(this.wildPort,record);
					}

				} // try

				catch (EndOfStreamException e)
				{
					ClosePort(portID);
					//					System.out.print( "\n" + this.getName() + "::Middle Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
					break;

				} // catch
				catch (IOException e) {					// TODO Auto-generated catch block
					ClosePort(portID);
				}
			}

		} // while

	} // run	
}
