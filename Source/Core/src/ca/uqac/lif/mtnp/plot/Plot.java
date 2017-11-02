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
package ca.uqac.lif.mtnp.plot;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ca.uqac.lif.mtnp.table.Table;
import ca.uqac.lif.mtnp.table.TableFunctionNode;
import ca.uqac.lif.mtnp.table.TableTransformation;
import ca.uqac.lif.mtnp.table.TempTable;
import ca.uqac.lif.mtnp.util.FileHelper;
import ca.uqac.lif.petitpoucet.NodeFunction;

/**
 * A representation of data into a picture
 * @author Sylvain Hallé
 */
public abstract class Plot
{
	/**
	 * The image type used for displaying the plot
	 */
	public static enum ImageType {PNG, DUMB, PDF, CACA};

	/**
	 * An 8-color preset palette for qualitative data:
	 * <span style="color:#E41A1C">&#x25A0;</span>
	 * <span style="color:#377EB8">&#x25A0;</span>
	 * <span style="color:#4DAF4A">&#x25A0;</span>
	 * <span style="color:#984EA3">&#x25A0;</span>
	 * <span style="color:#FF7F00">&#x25A0;</span>
	 * <span style="color:#FFFF33">&#x25A0;</span>
	 * <span style="color:#A65628">&#x25A0;</span>
	 * <span style="color:#F781BF">&#x25A0;</span>
	 * <p>
	 * This palette corresponds to the preset <tt>Set1.ptl</tt> from
	 * <a href="https://github.com/aschn/gnuplot-colorbrewer">gnuplot-colorbrewer</a>.
	 * 
	 */
	public static final transient Palette QUALITATIVE_1;

	/**
	 * An 8-color preset palette for qualitative data:
	 * <span style="color:#66C2A5">&#x25A0;</span>
	 * <span style="color:#FC8D62">&#x25A0;</span>
	 * <span style="color:#8DA0CB">&#x25A0;</span>
	 * <span style="color:#E78AC3">&#x25A0;</span>
	 * <span style="color:#A6D854">&#x25A0;</span>
	 * <span style="color:#FFD92F">&#x25A0;</span>
	 * <span style="color:#E5C494">&#x25A0;</span>
	 * <span style="color:#B3B3B3">&#x25A0;</span>
	 * <p>
	 * This palette corresponds to the preset <tt>Set2.ptl</tt> from
	 * <a href="https://github.com/aschn/gnuplot-colorbrewer">gnuplot-colorbrewer</a>.
	 */
	public static final transient Palette QUALITATIVE_2; 

	/**
	 * An 8-color preset palette for qualitative data:
	 * <span style="color:#8DD3C7">&#x25A0;</span>
	 * <span style="color:#FFFFB3">&#x25A0;</span>
	 * <span style="color:#BEBADA">&#x25A0;</span>
	 * <span style="color:#FB8072">&#x25A0;</span>
	 * <span style="color:#80B1D3">&#x25A0;</span>
	 * <span style="color:#FDB462">&#x25A0;</span>
	 * <span style="color:#B3DE69">&#x25A0;</span>
	 * <span style="color:#FCCDE5">&#x25A0;</span>
	 * <p>
	 * This palette corresponds to the preset <tt>Set3.ptl</tt> from
	 * <a href="https://github.com/aschn/gnuplot-colorbrewer">gnuplot-colorbrewer</a>.
	 */
	public static final transient Palette QUALITATIVE_3;

	/**
	 * A 16-color preset palette for qualitative data, corresponding to the
	 * 16 EGA colors.
	 */
	public static final transient Palette EGA;

	static {
		// Setup of discrete palettes
		// Found from https://github.com/aschn/gnuplot-colorbrewer
		QUALITATIVE_1 = new DiscretePalette("#E41A1C", "#377EB8", "#4DAF4A", "#984EA3", "#FF7F00", "#FFFF33", "#A65628", "#F781BF");
		QUALITATIVE_2 = new DiscretePalette("#66C2A5", "#FC8D62", "#8DA0CB", "#E78AC3", "#A6D854", "#FFD92F", "#E5C494", "#B3B3B3");
		QUALITATIVE_3 = new DiscretePalette("#8DD3C7", "#FFFFB3", "#BEBADA", "#FB8072", "#80B1D3", "#FDB462", "#B3DE69", "#FCCDE5");
		EGA = new DiscretePalette("#5555FF", "#55FF55", "#55FFFF", "#FF5555", "#FF55FF", "#FFFF55", "#0000AA", "#00AA00", "#00AAAA", "#AA0000", "#AA00AA", "#AA5500", "#AAAAAA", "#555555", "#FFFFFF", "#000000");
	}

	/**
	 * The table this plot is based on
	 */
	protected Table m_table;

	/**
	 * The plot's title
	 */
	protected String m_title;

	/**
	 * The plot's ID
	 */
	protected int m_id;

	/**
	 * A counter for auto-incrementing plot IDs
	 */
	private static int s_idCounter = 1;

	/**
	 * A lock for accessing the counter
	 */
	private static Lock s_counterLock = new ReentrantLock();

	/**
	 * The palette used to draw the data series for this plot
	 */
	protected Palette m_palette;

	/**
	 * A table transformation to apply before plotting
	 */
	protected TableTransformation m_transformation = null;

	/**
	 * Whether the plot shows a key
	 */
	protected transient boolean m_hasKey = true;

	/**
	 * A table nickname. This can be used as a short "code" that refers
	 * to the table (rather than using its ID).
	 */
	protected String m_nickname = "";

	/**
	 * The bytes of a blank PNG image, used as a placeholder when no plot can
	 * be drawn
	 */
	public static final transient byte[] s_blankImagePng = FileHelper.internalFileToBytes(Plot.class, "blank.png");

	/**
	 * The bytes of a blank PDF image, used as a placeholder when no plot can
	 * be drawn
	 */
	public static final transient byte[] s_blankImagePdf = FileHelper.internalFileToBytes(Plot.class, "blank.pdf");

	/**
	 * Creates a new plot from a table
	 * @param table The table
	 * @param transformation A transformation to apply to the table before
	 *   plotting
	 */
	public Plot(Table table, String title, TableTransformation transformation)
	{
		super();
		setTable(table);
		s_counterLock.lock();
		m_id = s_idCounter++;
		s_counterLock.unlock();
		m_transformation = transformation;
		setPalette(EGA);
	}

	/**
	 * Creates a new plot from a table
	 * @param table The table
	 */
	public Plot(Table table)
	{
		this(table, "", null);
	}
	
	/**
	 * Creates an empty plot
	 */
	public Plot()
	{
		this(null, "", null);
	}

	/**
	 * Creates a new plot
	 * @param t The table from which the plot will fetch its data
	 * @param title A title given to the plot
	 */
	protected Plot(Table t, String title)
	{
		this(t);
		m_title = title;
	}

	/**
	 * Sets the table to be displayed by this plot
	 * @param t The table
	 * @return This plot
	 */
	public Plot setTable(Table t)
	{
		if (t != null)
		{
			m_table = t;
			m_title = t.getTitle();
			if (m_title.matches("Table \\d+"))
			{
				// Replace "Table n" by "Plot n" as the default name
				m_title = m_title.replace("Table", "Plot");
			}
		}
		return this;
	}

	/**
	 * Gets the plot's ID
	 * @return The ID
	 */
	public final int getId()
	{
		return m_id;
	}

	/**
	 * Resets the ID counter for plots
	 */
	public static void resetCounter()
	{
		s_counterLock.lock();
		s_idCounter = 1;
		s_counterLock.unlock();
	}

	/**
	 * Gets the plot's title
	 * @return The title
	 */
	public final String getTitle()
	{
		return m_title;
	}

	/**
	 * Sets the plot's title
	 * @param title The title
	 * @return This plot
	 */
	public final Plot setTitle(String title)
	{
		m_title = title;
		return this;
	}

	/**
	 * Sets the palette to display the graph
	 * @param p The palette. Set to <tt>null</tt> to use the default palette.
	 * @return This plot
	 */
	public final Plot setPalette(Palette p)
	{
		m_palette = p;
		return this;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null || ! (o instanceof Plot))
		{
			return false;
		}
		return m_id == ((Plot) o).m_id;
	}

	@Override
	public int hashCode()
	{
		return m_id;
	}

	/**
	 * Sets if this plot shows a key when it has multiple data series
	 * @param b Set to {@code true} to enable the key, {@code false}
	 * otherwise
	 * @return This plot
	 */
	public Plot setKey(boolean b)
	{
		m_hasKey = b;
		return this;
	}

	/**
	 * Determines if this plot shows a key when it has multiple data series
	 * @return {@code true} if the key is enabled, {@code false} otherwise
	 */
	public boolean hasKey()
	{
		return m_hasKey;
	}

	/**
	 * Gets an image from this plot
	 * @param type The image type to produce
	 * @param with_caption Set to false to remove the caption from the image
	 *   (even if a caption is defined for the plot)
	 * @return An array of bytes containing the image, or {@code null} if
	 *   the image cannot be produced
	 */
	public abstract byte[] getImage(ImageType type, boolean with_caption);

	/**
	 * Gets an image from this plot
	 * @param type The image type to produce
	 * @return An array of bytes containing the image, or {@code null} if
	 *   the image cannot be produced
	 */
	public final byte[] getImage(ImageType type)
	{
		return getImage(type, true);
	}

	/**
	 * Transforms a data table before being plotted. A plot can override this
	 * method to perform pre-processing of the table.
	 * @param table The original table
	 * @return The transformed table
	 */
	public TempTable processTable(TempTable table)
	{
		if (m_transformation == null)
		{
			return table;
		}
		return m_transformation.transform(table);
	}

	/**
	 * Gets a reference to the data table from which this plot is drawn.
	 * @return The table, or {@code null} if no table reference can be given
	 */
	public Table getTable()
	{
		return m_table;
	}

	/**
	 * Generates a suitable file extension for a given image type
	 * @param type The image type
	 * @return The extension
	 */
	public static final String getTypeExtension(ImageType type)
	{
		switch (type)
		{
		case CACA:
			return "txt";
		case DUMB:
			return "txt";
		case PDF:
			return "pdf";
		case PNG:
			return "png";
		default:
			return "";

		}
	}

	public NodeFunction getDependency()
	{
		return new TableFunctionNode(m_table, 0, 0);
	}

	/**
	 * Sets a nickname for this plot. 
	 * This can be used as a short "code" that refers
	 * to the plot (rather than using its ID).
	 * @param nickname The nickname
	 * @return This table
	 */
	public Plot setNickname(String nickname)
	{
		if (nickname == null)
		{
			m_nickname = "";
		}
		else
		{
			m_nickname = nickname;
		}
		return this;
	}

	/**
	 * Gets the plot's nickname
	 * @return The nickname
	 */
	public String getNickname()
	{
		return m_nickname;
	}
}
