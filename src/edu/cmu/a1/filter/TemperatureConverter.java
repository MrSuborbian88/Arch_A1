package edu.cmu.a1.filter;

import java.io.IOException;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;
/******************************************************************************************************************
 * File: TemperatureConverter.java
 * 
 * 
 * Description:
 *
 * Converts the temperature field from celsius to fahrenheit
 *
 ******************************************************************************************************************/

public class TemperatureConverter extends FilterFrameworkExtended{
	/***************************************************************************
	 *  Arguments:
	 *  recordDefinition - The format of record messages	
	 *  FieldID - The Field ID for temperature
	 * 
	 ****************************************************************************/

	public TemperatureConverter(RecordDefinition recordDefinition, Integer FieldID) {
		super(recordDefinition);
		this.TEMPERATURE_FIELDID = FieldID;
	}
	private Integer TEMPERATURE_FIELDID = 4;
	private void DoInnerWork(Record r )
	{
		try {
			double fahrenheit = 0;
			double celsius = 0;

			fahrenheit = (Double) r.getValueByCode(TEMPERATURE_FIELDID); 
			celsius = ConvertFahrenheitWhichIsADumbScaleAnywayToCelsiusWhichIsOnlySlightlyBetterWhenYouReallyShouldAllBeUsingKelvinBecauseISaySo(fahrenheit);

			r.setValueByCode(TEMPERATURE_FIELDID, celsius);
//			System.out.println("F:" + fahrenheit);
//			System.out.println("C:" + celsius);
		} catch (IllegalArgumentException e) {
			//No Temperature
		}
	}

	private double ConvertFahrenheitWhichIsADumbScaleAnywayToCelsiusWhichIsOnlySlightlyBetterWhenYouReallyShouldAllBeUsingKelvinBecauseISaySo(double fahrenheit)
	{
		return ((fahrenheit - 32) * 5 ) / 9;
	}

	public void run()
	{

		while (this.inputsMap.size() > 0)
		{
			/*************************************************************
			 *	Here we read a byte and write a byte
			 *************************************************************/
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
