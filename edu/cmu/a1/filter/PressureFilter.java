package edu.cmu.a1.filter;

import java.io.IOException;
import java.util.ArrayList;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;

public class PressureFilter extends FilterFrameworkExtended {
	private Integer PRESSURE_ID = 3;
	private Integer WILD_ID = 6;
	private Double previous_value = -1.0;
	private ArrayList<Record> backlog;
	private boolean writeQueue = false;
	private boolean first = true;
	private Integer wildPort;
	private Integer WildIndicatorFieldId;
	
	private Double WILDPOINT = 1.0;
	private Double UNWILDPOINT = 0.0;

	public PressureFilter(RecordDefinition recordDefinition, Integer FieldID, Integer WildIndicatorPortId )
	{
		super(recordDefinition);
		this.PRESSURE_ID=FieldID;
		this.wildPort=WildIndicatorPortId;
		this.WILD_ID = WildIndicatorPortId;

		this.backlog = new ArrayList<Record>();
	}
	public void DoInnerWork(Record record )
	{
		try {
			
			double current_value = (Double) record.getValueByCode(PRESSURE_ID);
			if(current_value < 0)
			{
				writeQueue = false;
				record.setValueByCode(WILD_ID, WILDPOINT);
				writeRecord(this.wildPort,record);

			} else if(previous_value >= 0 && Math.abs(current_value-previous_value) > 10)
			{
				writeQueue = false;
				record.setValueByCode(WILD_ID, WILDPOINT);
				writeRecord(this.wildPort,record);
			} else {
				record.setValueByCode(WILD_ID, UNWILDPOINT);
				if(this.backlog.size() > 0) {
					//First value invalid, fill in rest
					if(previous_value < 0) 
					{
						for(int i = 0; i < backlog.size(); i++) {
							Record r = backlog.get(i);
							r.setValueByCode(PRESSURE_ID, current_value);
							backlog.set(i, r);
						}	

					}
					//Interpolate between points
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


		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		byte databyte = 0;					// The byte of data read from the file

		// Next we write a message to the terminal to let the world know we are alive...

//		System.out.print( "\n" + this.getName() + "::Middle Reading ");

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
