package edu.cmu.a1.filter;

import java.io.IOException;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;
/******************************************************************************************************************
 * File: DistanceConverter.java
 * 
 * 
 * Description:
 *
 * Converts altitude measure from meters to feet
 *
 ******************************************************************************************************************/

public class DistanceConverter extends FilterFrameworkExtended {

	/***************************************************************************
	 *  Arguments:
	 *  recordDefinition - The format of record messages	
	 *  FieldID - The Field ID for altitude
	 * 
	 ****************************************************************************/

	public DistanceConverter(RecordDefinition recordDefinition, Integer FieldID) {
		super(recordDefinition);
		this.ALTITUDE_FIELDID = FieldID;
	}
	private Integer ALTITUDE_FIELDID = 2;

	private void DoInnerWork(Record r )
	{
		try {
			double feet = 0;
			double meters = 0;
			
			feet = (Double) r.getValueByCode(ALTITUDE_FIELDID);
			meters = ConvertFeetWhichIsLeftoverImperialNonsenseToTheAwesomeMetricSystemMeters(feet);
			
			r.setValueByCode(ALTITUDE_FIELDID, meters);
		} catch (IllegalArgumentException e) {
			//No Temperature
		}
	}

	private static double ConvertFeetWhichIsLeftoverImperialNonsenseToTheAwesomeMetricSystemMeters(double feet)
	{
		return feet / 3.2808;
	}
	public void run()
	{

		while (this.inputsMap.size() > 0)
		{
			for(Integer portID : this.inputsMap.keySet()) {
				try
				{
					Record record = readNextRecord(portID);
					DoInnerWork(record);
					for(Integer outID : this.outputsMap.keySet())
							writeRecord(outID,record);
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
