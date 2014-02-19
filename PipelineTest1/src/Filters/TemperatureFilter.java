package Filters;


import Templates.BaseFilter;

public class TemperatureFilter extends BaseFilter
{
	@Override public void DoInnerWork()
	{
		if (this.id == 4)
		{		
			double fahrenheit = 0;
			double celsius = 0;
			
			fahrenheit = Double.longBitsToDouble(measurement);
			celsius = ConvertFahrenheitWhichIsADumbScaleAnywayToCelsiusWhichIsOnlySlightlyBetterWhenYouReallyShouldAllBeUsingKelvinBecauseISaySo(fahrenheit);
			
			this.UpdateMeasurementBytesFromDouble(celsius);
		}
	}
	
	private double ConvertFahrenheitWhichIsADumbScaleAnywayToCelsiusWhichIsOnlySlightlyBetterWhenYouReallyShouldAllBeUsingKelvinBecauseISaySo(double fahrenheit)
	{
		return ((fahrenheit - 32) * 5 ) / 9;
	}
}
