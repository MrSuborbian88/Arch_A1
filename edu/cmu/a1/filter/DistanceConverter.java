package edu.cmu.a1.filter;

import java.io.IOException;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;
import edu.cmu.a1.base.FilterFramework.EndOfStreamException;

public class DistanceConverter extends FilterFrameworkExtended {

	public DistanceConverter(RecordDefinition recordDefinition, Integer FieldID) {
		super(recordDefinition);
		//TODO
	}
	private static final Integer ALTITUDE_FIELDID = 2;

	public void DoInnerWork(Record r )
	{
		try {
			double feet = 0;
			double meters = 0;
			
			feet = (Double) r.getValueByCode(ALTITUDE_FIELDID);
			meters = ConvertFeetWhichIsLeftoverImperialNonsenseToTheAwesomeMetricSystemMeters(feet);
			
			r.setValueByCode(ALTITUDE_FIELDID, meters);
			System.out.println("F:" + feet);
			System.out.println("M:" + meters);
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
