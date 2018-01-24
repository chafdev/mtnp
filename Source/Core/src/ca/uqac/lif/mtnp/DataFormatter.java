/*
  MTNP: Manipulate Tables N'Plots
  Copyright (C) 2017 Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.mtnp;

import ca.uqac.lif.mtnp.table.PrimitiveValue;

/**
 * Provides various facilities for reading, casting and formatting values
 */
public class DataFormatter
{
	/**
	 * The OS-dependent carriage return symbol
	 */
	public static final String CRLF = System.getProperty("line.separator");
	
	/**
	 * Version string
	 */
	protected static final String s_versionString = FrontEnd.s_majorVersionNumber + "." + FrontEnd.s_minorVersionNumber + "." + FrontEnd.s_revisionVersionNumber;
	
	/**
	 * Gets the version string of this library
	 * @return The version string
	 */
	public static String getVersionString()
	{
		return s_versionString;
	}
	
	/**
	 * Attempts to convert an object into a number
	 * @param o The object to convert
	 * @return The object as a number; if the conversion cannot be
	 * done, the object is converted as a string using its {@code toString()}
	 * method
	 */
	public static Object cast(Object o)
	{
		if (o instanceof String)
		{
			try
			{
				int f = Integer.parseInt((String) o);
				return f;
			}
			catch (NumberFormatException e1)
			{
				try
				{
					float f = Float.parseFloat((String) o);
					return f;
				}
				catch (NumberFormatException e2)
				{
					return o;
				}
			}
		}
		return o;
	}
	
	/**
	 * Attempts to convert an object into a number
	 * @param o The object to convert
	 * @return The object as a string; if the object is null,
	 * the empty string is returned
	 */
	public static /*@NotNull*/ String asString(Object o)
	{
		if (o == null)
		{
			return "";
		}
		if (o instanceof String)
		{
			return (String) o;
		}
		return o.toString();
	}
	
	/**
	 * Attempts to convert an object into a float
	 * @param o The object
	 * @return A float, or null {@code o} is not a number
	 */
	public static final Float readFloat(Object o)
	{
		if (o == null)
		{
			return null;
		}
		if (o instanceof Number)
		{
			return ((Number) o).floatValue();
		}
		if (o instanceof PrimitiveValue)
		{
			PrimitiveValue pv = (PrimitiveValue) o;
			if (pv.isNumeric())
			{
				return pv.numberValue().floatValue();
			}
		}
		return null;
	}
	
	/**
	 * Rounds number num to n significant figures.
	 * Found from <a href="http://stackoverflow.com/a/1581007">StackOverflow</a>
	 * @param num The number
	 * @param n The number of significant figures
	 * @return The resulting number
	 */
	public static double roundToSignificantFigures(double num, int n) 
	{
	    if(num == 0) 
	    {
	        return 0;
	    }
	    final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
	    final int power = n - (int) d;
	    final double magnitude = Math.pow(10, power);
	    final long shifted = Math.round(num*magnitude);
	    return shifted/magnitude;
	}
	
	/**
	 * Checks if a given string contains a number
	 * @param s The string
	 * @return true if it contains a number, false otherwise
	 */
	public static boolean isNumeric(String s)
	{
		if (s == null)
		{
			return false;
		}
		try
		{
			Float.parseFloat(s);
			return true;
		}
		catch (NumberFormatException nfe)
		{
			return false;
		}
	}
}
