package edu.cmu.a1.filter;

import java.io.IOException;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;

public class FieldFilter extends FilterFrameworkExtended{

	Integer [] FilterIDs;
	public FieldFilter(RecordDefinition recordDefinition, Integer [] FieldID) {
		super(recordDefinition);
		if(FieldID == null)
			throw new NullPointerException("Field ID Array is Null");
		FilterIDs = FieldID;
	}
	public Record DoInnerWork(Record r)
	{
		RecordDefinition filteredRecordDefinition = createFilteredRecordDefinition();
		Record filteredRecord = new Record(filteredRecordDefinition);
		for(Integer code : FilterIDs)
		try {
			filteredRecord.setValueByCode(code, r.getValueByCode(code));
		} catch (IllegalArgumentException e) {
			//No Temperature
		}
		
		return filteredRecord;
		
		
	}

	private RecordDefinition createFilteredRecordDefinition() {
		RecordDefinition recordDef = new RecordDefinition();
		for(Integer code : FilterIDs) {
			if(this.recordDefinition.hasFieldCode(code)) {
				recordDef.addFieldDefinition(code, this.recordDefinition.getFieldType(code), this.recordDefinition.getFieldTitle(code));
			}
		}
		return recordDef;
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
					record = DoInnerWork(record);
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

}
