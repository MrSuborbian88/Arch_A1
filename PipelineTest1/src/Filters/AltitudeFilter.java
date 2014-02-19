package Filters;

import Templates.BaseFilter;

public class AltitudeFilter extends BaseFilter
{
	@Override public void DoInnerWork()
	{
		if ( this.id == 2 )
		{
			double feet = 0;
			double meters = 0;
			
			feet = Double.longBitsToDouble(measurement);
			meters = ConvertFeetWhichIsLeftoverImperialNonsenseToTheAwesomeMetricSystemMeters(feet);
			
			this.UpdateMeasurementBytesFromDouble(meters);
		} // if
		
		
	}
		
	private double ConvertFeetWhichIsLeftoverImperialNonsenseToTheAwesomeMetricSystemMeters(double feet)
	{
		return feet / 3.2808;
	}
}
