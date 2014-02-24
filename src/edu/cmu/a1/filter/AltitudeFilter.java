package edu.cmu.a1.filter;

import java.io.IOException;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;
/******************************************************************************************************************
 * File: AltitudeFilter.java
 * 
 * 
 * Description:
 *
 * Filters out altitudes below 10,000 to a special "wild stream"
 *
 ******************************************************************************************************************/

public class AltitudeFilter extends FilterFrameworkExtended {

	private Integer ALTITUDE_ID = 2;
	private Integer WILD_ID = 7;
	private Double previous_value = -1.0;
	private Integer wildPort;

	private Double WILDPOINT = 1.0;
	private Double UNWILDPOINT = 0.0;

	/***************************************************************************
	 *  Arguments:
	 *  recordDefinition - The format of record messages	
	 *  FieldID - The Field ID for altitude
	 *  WildIndicatorPortId - The Field ID of the field in the record to mark 
	 *  						whether the value meets the criteria (less than 10k)
	 *  						 & the Output Stream reference port to write to 
	 * 
	 ****************************************************************************/

	public AltitudeFilter(RecordDefinition recordDefinition, Integer FieldID, Integer WildIndicatorPortId)
	{
		super(recordDefinition);
		this.ALTITUDE_ID=FieldID;
		this.WILD_ID=WildIndicatorPortId;
		this.wildPort=WildIndicatorPortId;
	}
	private boolean DoInnerWork(Record record )
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
		while (this.inputsMap.size() > 0)
		{
			for(Integer portID : this.inputsMap.keySet()) {
				try
				{
					//Read in records
					Record record = readNextRecord(portID);
					boolean greaterthan10k = DoInnerWork(record);
					//>= 10k altitude, write to all output streams (except the wild stream)
					if(greaterthan10k)
					{
						for(Integer outID : this.outputsMap.keySet())
							if(outID != this.wildPort)
								writeRecord(outID,record);
					}
					//<10k altitude , write to the "wild" output stream
					else
					{
						writeRecord(this.wildPort,record);
					}

				} // try

				catch (EndOfStreamException e)
				{
					ClosePort(portID);

				} // catch
				catch (IOException e) {
					ClosePort(portID);
				}
			}

		} // while

	} // run	
}
