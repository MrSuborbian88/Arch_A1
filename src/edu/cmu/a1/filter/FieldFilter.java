package edu.cmu.a1.filter;

import java.io.IOException;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;

/******************************************************************************************************************
 * File: FieldFilter.java
 * 
 * 
 * Description:
 *
 * Filters out values from the record.
 *
 ******************************************************************************************************************/


public class FieldFilter extends FilterFrameworkExtended{

	private Integer [] FilterIDs;
	/***************************************************************************
	 *  Arguments:
	 *  recordDefinition - The format of record messages
	 *  FieldID - The Field IDs of the record to write to its output streams
	 * 
	 ****************************************************************************/
	public FieldFilter(RecordDefinition recordDefinition, Integer [] FieldID) {
		super(recordDefinition);
		if(FieldID == null)
			throw new NullPointerException("Field ID Array is Null");
		FilterIDs = FieldID;
	}
	private Record DoInnerWork(Record r)
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
		while (inputsMap.size() > 0)
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

				} // catch
				catch (IOException e) {					// TODO Auto-generated catch block
					ClosePort(portID);
				}
			}

		} // while

	} // run

}
