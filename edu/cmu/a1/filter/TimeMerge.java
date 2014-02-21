package edu.cmu.a1.filter;

import java.io.IOException;
import java.util.HashMap;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;

public class TimeMerge extends FilterFrameworkExtended {

	private Integer TIME_FIELDID;
	private HashMap<Integer,Record> recordMap;
	public TimeMerge(RecordDefinition recordDefinition, Integer FieldID) {
		super(recordDefinition);
		TIME_FIELDID = FieldID;
		this.recordMap = new HashMap<Integer,Record>();
	}
	private void UpdateMap() {
		for(Integer portID : this.inputsMap.keySet()) {
			try
			{
				if(!this.recordMap.containsKey(portID)) {
					//Read records until one has a time field
					while(true) {
						Record record = readNextRecord(portID);
						this.recordMap.put(portID,record);
						try {
							record.getValueByCode(TIME_FIELDID);
							break;
						} catch(IllegalArgumentException e ) {
							//Find
						}
					}
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

	}
	public Record FindAndRemoveLowestRecord() {
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
		while (true)
		{
			//Get a record from each stream (that doesn't have a record stored)
			UpdateMap();
			//Find (and remove) the record with the lowest time
			Record record = FindAndRemoveLowestRecord();
			//Write it out
			for(Integer portID : this.outputsMap.keySet()) {
					writeRecord(portID,record);
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
