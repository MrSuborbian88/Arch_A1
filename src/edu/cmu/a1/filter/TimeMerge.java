package edu.cmu.a1.filter;

import java.util.HashMap;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;
/******************************************************************************************************************
 * File: TimeMerge.java
 * 
 * 
 * Description:
 *
 * Combines multiple input streams, ordering them chronologically by a record's time field
 *
 ******************************************************************************************************************/

public class TimeMerge extends FilterFrameworkExtended {

	private Integer TIME_FIELDID;
	private HashMap<Integer,Record> recordMap;
	/***************************************************************************
	 *  Arguments:
	 *  recordDefinition - The format of record messages	
	 *  FieldID - The Field ID for the timestamp
	 * 
	 ****************************************************************************/
	public TimeMerge(RecordDefinition recordDefinition, Integer FieldID) {
		super(recordDefinition);
		TIME_FIELDID = FieldID;
		this.recordMap = new HashMap<Integer,Record>();
	}
	
	//Read a record from each input stream that does not have a record in our map
	private void UpdateMap() {
		Integer[] keys = this.inputsMap.keySet().toArray(new Integer [this.inputsMap.size()]);
		for(Integer portID : keys) {
			//Find input streams that don't have a record in our local map
			if(!this.recordMap.containsKey(portID)) {
				//Read records until one has a time field
				while(true) {
					try {
						Record record = readNextRecord(portID);
						this.recordMap.put(portID,record);
						try {
							record.getValueByCode(TIME_FIELDID);
							break;
						} catch(IllegalArgumentException e ) {
							//Not in this record
						}
					} catch (Exception e){
						ClosePort(portID);
						break;
					}
				}
			}
		}

	}
	//Find the record with the lowest time value and remove/return it
	private Record FindAndRemoveLowestRecord() {
		long lowest_record = -1;
		Integer lowest_record_id = -1;
		for(Integer id : this.recordMap.keySet()) {
			long current = (Long) this.recordMap.get(id).getValueByCode(TIME_FIELDID);
			if(lowest_record_id == -1 || current < lowest_record)
			{
				lowest_record_id = id;
				lowest_record = current;
			}
		}
		if(lowest_record_id != -1)
		{
			Record record = this.recordMap.get(lowest_record_id);
			this.recordMap.remove(lowest_record_id);
			return record;
		}
		else
			return null;
	}
	public void run()
	{
		while (this.inputsMap.size() > 0 || recordMap.size() > 0)
		{
			//Get a record from each stream (that doesn't have a record stored)
			UpdateMap();
			//Find (and remove) the record with the lowest time
			Record record = FindAndRemoveLowestRecord();
			//Write it out
			if(record != null)
				for(Integer portID : this.outputsMap.keySet()) {
					writeRecord(portID,record);
				}

		} // while

	} // run

}
