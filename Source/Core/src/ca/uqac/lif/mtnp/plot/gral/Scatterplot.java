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
package ca.uqac.lif.mtnp.plot.gral;

import ca.uqac.lif.mtnp.plot.TwoDimensionalPlot;
import ca.uqac.lif.mtnp.table.HardTable;
import ca.uqac.lif.mtnp.table.Table;
import ca.uqac.lif.mtnp.table.TableTransformation;
import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.graphics.Insets2D;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.axes.LogarithmicRenderer2D;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;

/**
 * Scatterplot with default settings. Given a table, this class will draw
 * an x-y scatterplot with the first column as the values for the "x" axis,
 * and every remaining column as a distinct data series plotted on the "y"
 * axis.
 *   
 * @author Sylvain Hallé
 */
public class Scatterplot extends GralPlot implements ca.uqac.lif.mtnp.plot.Scatterplot
{
	/**
	 * The caption for the "x" axis
	 */
	protected String m_captionX = "";
	
	/**
	 * The caption for the "y" axis
	 */
	protected String m_captionY = "";
	
	/**
	 * Whether to use a logarithmic scale for the X axis
	 */
	protected boolean m_logScaleX = false;
	
	/**
	 * Whether to use a logarithmic scale for the Y axis
	 */
	protected boolean m_logScaleY = false;
	
	/**
	 * Whether to draw each data series with lines between each data point
	 */
	protected boolean m_withLines = true;
	
	/**
	 * Whether to draw each data series with marks for each data point
	 */
	protected boolean m_withPoints = true;
	
	/**
	 * Creates an empty scatterplot with default settings
	 */
	public Scatterplot()
	{
		super();
	}

	/**
	 * Creates a new scatterplot with default settings
	 * @param t The table
	 */
	public Scatterplot(Table t)
	{
		super(t);
	}
	
	/**
	 * Creates a new scatterplot with default settings
	 * @param t The table
	 * @param transformation A table transformation
	 */
	public Scatterplot(Table t, TableTransformation transformation)
	{
		super(t, transformation);
	}
	
	@Override
	public Scatterplot withLines(boolean b)
	{
		m_withLines = b;
		return this;
	}
	
	@Override
	public Scatterplot withLines()
	{
		m_withLines = true;
		return this;
	}
	
	@Override
	public Scatterplot withPoints(boolean b)
	{
		m_withPoints = b;
		return this;
	}
	
	@Override
	public Scatterplot withPoints()
	{
		m_withPoints = true;
		return this;
	}


	@Override
	public TwoDimensionalPlot setCaption(Axis axis, String caption)
	{
		if (axis == Axis.X)
		{
			m_captionX = caption;
		}
		else
		{
			m_captionY = caption;
		}
		return this;
	}
	
	@Override
	public de.erichseifert.gral.plots.Plot getPlot(HardTable source)
	{
		//GralDataTable gdt = GralDataTable.toGral(source);
		int num_cols = source.getColumnCount();
		DataSeries[] series = new DataSeries[num_cols - 1];
		String col_0 = source.getColumnName(0);
		for (int col = 1; col < num_cols; col++)
		{
			series[col - 1] = GralDataTable.getCleanedDataSeries(col_0, source.getColumnName(col), source);
		}
		XYPlot plot = new XYPlot(series);
		for (int col = 1; col < num_cols; col++)
		{
			if (m_withPoints)
			{
				PointRenderer pr = new DefaultPointRenderer2D();
				plot.setPointRenderers(series[col - 1], pr);
				for (PointRenderer r : plot.getPointRenderers(series[col - 1]))
				{
					r.setColor(m_palette.getPaint(col - 1));
				}
			}
			if (m_withLines)
			{
				LineRenderer lr = new DefaultLineRenderer2D();
				plot.setLineRenderers(series[col - 1], lr);
				for (LineRenderer r : plot.getLineRenderers(series[col - 1]))
				{
					r.setColor(m_palette.getPaint(col - 1));
				}
			}
		}
		plot.setInsets(new Insets2D.Double(20d, 60d, 60d, 40d));
		plot.getTitle().setText(getTitle());
		if (series.length > 1)
		{
			// Put legend only if more than one data series
			plot.setLegendVisible(true);
		}
		if (m_logScaleX)
		{
			AxisRenderer rendererX = new LogarithmicRenderer2D();
			plot.setAxisRenderer(XYPlot.AXIS_X, rendererX);
		}
		if (m_logScaleY)
		{
			AxisRenderer rendererY = new LogarithmicRenderer2D();
			plot.setAxisRenderer(XYPlot.AXIS_Y, rendererY);
		}
		if (!m_captionX.isEmpty())
		{
			plot.getAxisRenderer(XYPlot.AXIS_X).getLabel().setText(m_captionX);
		}
		else
		{
			plot.getAxisRenderer(XYPlot.AXIS_X).getLabel().setText(source.getColumnName(0));
		}
		plot.getAxisRenderer(XYPlot.AXIS_Y).getLabel().setText(m_captionY);
		customize(plot);
		return plot;
	}
	
	@Override
	public Scatterplot setLogscale(Axis axis)
	{
		if (axis == Axis.X)
		{
			m_logScaleX = true;
		}
		else
		{
			m_logScaleY = true;
		}
		return this;
	}
}
