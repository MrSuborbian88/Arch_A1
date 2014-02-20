package edu.cmu.a1.filter;

import java.util.Queue;

import com.sun.jmx.remote.internal.ArrayQueue;

import edu.cmu.a1.base.FilterFrameworkExtended;
import edu.cmu.a1.base.Record;
import edu.cmu.a1.base.RecordDefinition;

public class AltitudeFilter extends FilterFrameworkExtended {

	private Integer ALTITUDE_ID = 3;
	private Integer WILD_ID = 6;
	private Double previous_value = -1.0;
	private ArrayQueue<Record> backlog;
	private boolean writeQueue = false;
	private boolean first = true;
	public AltitudeFilter(RecordDefinition recordDefinition, Integer FieldID, Integer WildIndicatorFieldId)
	{
		super(recordDefinition);
		this.ALTITUDE_ID=FieldID;
		this.WILD_ID=WildIndicatorFieldId;
		this.backlog = new ArrayQueue<Record>(1024*32);
	}
	public void DoInnerWork(Record record )
	{
		try {
			
			double current_value = (Double) record.getValueByCode(ALTITUDE_ID);
			if(current_value < 0)
			{
				writeQueue = false;
				record.setValueByCode(WILD_ID, 1);
			} else if(previous_value >= 0 && Math.abs(current_value-previous_value) > 10)
			{
				writeQueue = false;
				record.setValueByCode(WILD_ID, 1);
			} else {
				record.setValueByCode(WILD_ID, 0);
				if(this.backlog.size() > 0) {
					double step = current_value - previous_value / (backlog.size() + 1);
					
					for(Record r : backlog) {
						previous_value += step;
						r.setValueByCode(ALTITUDE_ID, previous_value);
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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
