package edu.cmu.a1.filter;

import java.io.IOException;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;

public class TemperatureConverter extends FilterFrameworkExtended{

	public TemperatureConverter(RecordDefinition recordDefinition, Integer FieldID) {
		super(recordDefinition);
		//TODO
	}
	private Integer TEMPERATURE_FIELDID = 4;
	public void DoInnerWork(Record r )
	{
		try {
			double fahrenheit = 0;
			double celsius = 0;

			fahrenheit = (Double) r.getValueByCode(TEMPERATURE_FIELDID); 
			celsius = ConvertFahrenheitWhichIsADumbScaleAnywayToCelsiusWhichIsOnlySlightlyBetterWhenYouReallyShouldAllBeUsingKelvinBecauseISaySo(fahrenheit);

			r.setValueByCode(TEMPERATURE_FIELDID, celsius);
			System.out.println("F:" + fahrenheit);
			System.out.println("C:" + celsius);
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


		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		byte databyte = 0;					// The byte of data read from the file

		// Next we write a message to the terminal to let the world know we are alive...

		System.out.print( "\n" + this.getName() + "::Middle Reading ");

		while (true)
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
					System.out.print( "\n" + this.getName() + "::Middle Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
					break;

				} // catch
				catch (IOException e) {					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} // while

	} // run
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
