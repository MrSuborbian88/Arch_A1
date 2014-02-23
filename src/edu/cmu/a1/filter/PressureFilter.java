package edu.cmu.a1.filter;

import java.io.IOException;
import java.util.ArrayList;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;
/******************************************************************************************************************
 * File: PressureFilter.java
 * 
 * 
 * Description:
 *
 * Checks for valid pressure values and modifies any.
 * Writes any invalid values to a special "wild stream"
 *
 ******************************************************************************************************************/

public class PressureFilter extends FilterFrameworkExtended {
	private Integer PRESSURE_ID = 3;
	private Integer WILD_ID = 6;
	private Double previous_value = -1.0;
	private ArrayList<Record> backlog;
	private boolean writeQueue = false;
	private Integer wildPort;

	private Double WILDPOINT = 1.0;
	private Double UNWILDPOINT = 0.0;
	
	/***************************************************************************
	 *  Arguments:
	 *  recordDefinition - The format of record messages	
	 *  FieldID - The Field ID for Pressure
	 *  WildIndicatorPortId - The Field ID of the field in the record to mark 
	 *  						whether the value has been altered 
	 *  						 & the Output Stream reference port to write to 
	 * 
	 ****************************************************************************/
	public PressureFilter(RecordDefinition recordDefinition, Integer FieldID, Integer WildIndicatorPortId )
	{
		super(recordDefinition);
		this.PRESSURE_ID=FieldID;
		this.wildPort=WildIndicatorPortId;
		this.WILD_ID = WildIndicatorPortId;

		this.backlog = new ArrayList<Record>();
	}
	private void DoInnerWork(Record record )
	{
		try {

			double current_value = (Double) record.getValueByCode(PRESSURE_ID);
			
			//Invalid if negative, store in queue until next valid value is found
			if(current_value < 0)
			{
				writeQueue = false;
				record.setValueByCode(WILD_ID, WILDPOINT);
				//Write to the wild output stream
				writeRecord(this.wildPort,record);

			}
			//Invalid if pressure change is greater than 10, store in queue until next valid value is found 
			else if(previous_value >= 0 && Math.abs(current_value-previous_value) > 10)
			{
				writeQueue = false;
				record.setValueByCode(WILD_ID, WILDPOINT);
				//Write to the wild output stream
				writeRecord(this.wildPort,record);
			}
			//Valid value
			else {
				record.setValueByCode(WILD_ID, UNWILDPOINT);
				//If there were previous invalid 
				if(this.backlog.size() > 0) {
					//No valid input up to this point
					// fill in all values with this first valid value
					if(previous_value < 0) 
					{
						for(int i = 0; i < backlog.size(); i++) {
							Record r = backlog.get(i);
							r.setValueByCode(PRESSURE_ID, current_value);
							backlog.set(i, r);
						}	

					}
					//Invalids within the middle of the stream, interpolate between points and update values
					else {
						double step = (current_value - previous_value) / (backlog.size() + 1);

						for(int i = 0; i < backlog.size(); i++) {
							Record r = backlog.get(i);
							previous_value += step;
							r.setValueByCode(PRESSURE_ID, previous_value);
							backlog.set(i, r);
						}	
					}
				}
				writeQueue = true;
				previous_value = current_value;
			}
			backlog.add(record);
		} catch (IllegalArgumentException e) {
			//No Altitude
		}
	}
	public void run()
	{


		while (this.inputsMap.size() > 0)
		{
			/*************************************************************
			 *	Loop through inputs for records
			 *************************************************************/
			for(Integer portID : this.inputsMap.keySet()) {
				try
				{
					Record record = readNextRecord(portID);
					
					DoInnerWork(record);
					//Write all the records that are ready to be written
					if(writeQueue) {
						for(Integer outID : this.outputsMap.keySet())
							if(outID != this.wildPort)
								for(Record r : backlog)
									writeRecord(outID,r);
						backlog.clear();
					}
				} // try

				catch (EndOfStreamException e)
				{
					ClosePort(portID);
					break;

				} // catch
				catch (IOException e) {	
					ClosePort(portID);
				}
			}

		} // while

		//Write any remaining values with the last valid value (if there is one)
		if(backlog.size()  > 0 && previous_value >= 0) {
			for(Integer outID : this.outputsMap.keySet())
				if(outID != this.wildPort)
					for(Record r : backlog)
					{
						r.setValueByCode(PRESSURE_ID, previous_value);
						writeRecord(outID,r);
					}
			backlog.clear();	
		}


	} // run	

}
