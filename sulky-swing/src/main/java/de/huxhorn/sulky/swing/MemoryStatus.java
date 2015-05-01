/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2015 Joern Huxhorn
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2007-2015 Joern Huxhorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.huxhorn.sulky.swing;

import de.huxhorn.sulky.formatting.HumanReadable;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;

import javax.swing.border.Border;

public class MemoryStatus
	extends JComponent
{
	private static final long serialVersionUID = -7977658722158059284L;

	private static final int GRADIENT_PIXELS = 3;
	private final Logger logger = LoggerFactory.getLogger(MemoryStatus.class);

	private Runtime runtime;
	private MemoryInfo memoryInfo;
	private boolean paused;
	private boolean usingTotal;
	private boolean usingBinaryUnits;

	private BufferedImage offscreenImage;
	private static final Color USED_COLOR = new Color(20, 255, 20); // 20 instead of 0 so brighter works properly
	private static final Color TOTAL_COLOR = new Color(255, 255, 20, 192); // 20 instead of 0 so brighter works properly

	public MemoryStatus()
	{
		runtime = Runtime.getRuntime();
		paused = true;
		JLabel fontLabel = new JLabel("8,888.88 XXX");
		setFont(fontLabel.getFont());
		//setMemoryInfo(new MemoryInfo(runtime));
		updateMemoryBar();
		addMouseListener(new GcMouseListener());
		if(logger.isDebugEnabled()) logger.debug("Font: {}", getFont());
		//initUi();
		Thread t = new Thread(new PollRunnable(), "MemoryStatus-Poller");
		t.setDaemon(true);
		t.start();
	}

	public boolean isUsingBinaryUnits()
	{
		return usingBinaryUnits;
	}

	public void setUsingBinaryUnits(boolean usingBinaryUnits)
	{
		this.usingBinaryUnits = usingBinaryUnits;
	}

	@Override
	public void setFont(Font font)
	{
		super.setFont(font);
		calculatePreferredSize();
	}

	@Override
	public void setBorder(Border border)
	{
		super.setBorder(border);
		calculatePreferredSize();
	}

	private void calculatePreferredSize()
	{
		JLabel label = new JLabel("8,888.88 XXX");
		label.setFont(getFont());
		label.setBorder(getBorder());
		Dimension size = label.getPreferredSize();
		size.height += 2 * GRADIENT_PIXELS;
		setPreferredSize(size);
	}

	public boolean isUsingTotal()
	{
		return usingTotal;
	}

	public void setUsingTotal(boolean usingTotal)
	{
		this.usingTotal = usingTotal;
	}

	public synchronized boolean isPaused()
	{
		return paused;
	}

	/**
	 * Calls the UI delegate's paint method, if the UI delegate
	 * is non-<code>null</code>.  We pass the delegate a copy of the
	 * <code>Graphics</code> object to protect the rest of the
	 * paint code from irrevocable changes
	 * (for example, <code>Graphics.translate</code>).
	 *
	 * If you override this in a subclass you should not make permanent
	 * changes to the passed in <code>Graphics</code>. For example, you
	 * should not alter the clip <code>Rectangle</code> or modify the
	 * transform. If you need to do these operations you may find it
	 * easier to create a new <code>Graphics</code> from the passed in
	 * <code>Graphics</code> and manipulate it. Further, if you do not
	 * invoker super's implementation you must honor the opaque property,
	 * that is
	 * if this component is opaque, you must completely fill in the background
	 * in a non-opaque color. If you do not honor the opaque property you
	 * will likely see visual artifacts.
	 *
	 * The passed in <code>Graphics</code> object might
	 * have a transform other than the identify transform
	 * installed on it.  In this case, you might get
	 * unexpected results if you cumulatively apply
	 * another transform.
	 *
	 * @param g the <code>Graphics</code> object to protect
	 * @see #paint
	 * @see javax.swing.plaf.ComponentUI
	 */
	@Override
	protected void paintComponent(Graphics g)
	{
		Insets insets = getInsets();
		Dimension size = getSize();
		if(isOpaque())
		{
			g.setColor(getBackground());
			g.fillRect(0, 0, size.width, size.height);
		}
		Rectangle paintingBounds = new Rectangle();
		paintingBounds.x = insets.left;
		paintingBounds.y = insets.top;
		paintingBounds.width = size.width - insets.left - insets.right;
		paintingBounds.height = size.height - insets.top - insets.bottom;
		if(offscreenImage == null
			|| offscreenImage.getWidth() != paintingBounds.width
			|| offscreenImage.getHeight() != paintingBounds.height)
		{
			if(offscreenImage != null)
			{
				offscreenImage.flush();
				offscreenImage = null;
			}
			if(paintingBounds.width > 0 && paintingBounds.height > 0)
			{
				GraphicsConfiguration gc = getGraphicsConfiguration();
				offscreenImage = gc
					.createCompatibleImage(paintingBounds.width, paintingBounds.height, Transparency.TRANSLUCENT);
				if(logger.isDebugEnabled()) logger.debug("Created offscreen-image...");
			}
		}
		if(offscreenImage != null)
		{
			Graphics gr = offscreenImage.getGraphics();
			paintMemoryStatus(gr, paintingBounds);
			gr.dispose();
			g.drawImage(offscreenImage, paintingBounds.x, paintingBounds.y, null);
		}
	}


	private void paintMemoryStatus(Graphics g, Rectangle paintingBounds)
	{
		MemoryInfo info = this.memoryInfo;
		Graphics2D g2 = (Graphics2D) g;
		//g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setComposite(AlphaComposite.Clear);
		g2.fillRect(0, 0, paintingBounds.width, paintingBounds.height);
		g2.setComposite(AlphaComposite.SrcOver);
		if(info != null)
		{
			if(!usingTotal)
			{
				double usedFraction = (((double) info.getUsed() / (double) info.getMax()));
				double totalFraction = (((double) info.getTotal() / (double) info.getMax()));
				int usedWidth = (int) (paintingBounds.width * usedFraction + 0.5);
				int totalWidth = (int) (paintingBounds.width * totalFraction + 0.5);

				drawBar(g2, 0, usedWidth, paintingBounds.height, USED_COLOR);

				//g.setColor(new Color(255, 255, 0,192));
				//g.fillRect(usedWidth, 0, totalWidth-usedWidth, paintingBounds.height);
				drawBar(g2, usedWidth, totalWidth, paintingBounds.height, TOTAL_COLOR);
			}
			else
			{
				double usedFraction = (((double) info.getUsed() / (double) info.getTotal()));
				int usedWidth = (int) (paintingBounds.width * usedFraction + 0.5);

				drawBar(g2, 0, usedWidth, paintingBounds.height, USED_COLOR);
			}
			// text
			{
				String text = HumanReadable.getHumanReadableSize(info.getUsed(), usingBinaryUnits, true) + "B";
				FontRenderContext frc = g2.getFontRenderContext();

				TextLayout tl = new TextLayout(text, getFont(), frc);
				Shape s = tl.getOutline(null);
				Rectangle textBounds = s.getBounds();
				if(logger.isDebugEnabled()) logger.debug("textBounds: {}", textBounds);
				textBounds.x = (textBounds.x * -1) + (paintingBounds.width - textBounds.width) / 2;
				textBounds.y = (textBounds.y * -1) + (paintingBounds.height - textBounds.height) / 2;
				g.translate(textBounds.x, textBounds.y);
				if(logger.isDebugEnabled()) logger.debug("corrected textBounds: {}", textBounds);
				//FontMetrics fm = g.getFontMetrics();
				//Rectangle2D lm = fm.getStringBounds(text, g);
				//int textX=(int) (paintingBounds.width-lm.getWidth());
				//int textBase=halfHeight-fm.getHeight()/2;
				g.setColor(Color.WHITE);
				GraphicsUtilities.drawHighlight(g2, s, GRADIENT_PIXELS, 0.2f);
				g.setColor(Color.BLACK);
				//g.drawString(text,textX,textBase);
				g2.fill(s);
			}


		}

	}

	private void drawBar(Graphics2D g2, int startX, int endX, int height, Color c)
	{
		int halfHeight = height / 2;
		int gradientHeight = Math.min(GRADIENT_PIXELS, halfHeight);

		if(2 * gradientHeight < height)
		{
			g2.setColor(c);
			g2.fillRect(startX, gradientHeight, endX, height - 2 * gradientHeight);
		}

		GradientPaint p;
		Color brighter = c.brighter().brighter();
		Color darker = c.darker().darker();
		int colorAlpha = c.getAlpha();
		if(colorAlpha < 255)
		{
			brighter = new Color(brighter.getRed(), brighter.getGreen(), brighter.getBlue(), colorAlpha);
			darker = new Color(darker.getRed(), darker.getGreen(), darker.getBlue(), colorAlpha);
			if(logger.isDebugEnabled()) logger.debug("Corrected alpha-values.");
		}
		if(logger.isDebugEnabled())
		{
			logger.debug("original: {}\nbrighter: {}\ndarker: {}", new Object[]{c, brighter, darker});
		}
		p = new GradientPaint(0, 0, brighter, 0, gradientHeight, c);
		g2.setPaint(p);
		g2.fillRect(startX, 0, endX, gradientHeight);

		p = new GradientPaint(0, height - gradientHeight, c, 0, height, darker);
		g2.setPaint(p);
		g2.fillRect(startX, height - gradientHeight, endX, gradientHeight);


	}


	public synchronized void setPaused(boolean paused)
	{
		this.paused = paused;
		notifyAll();
	}

/*
	private void initUi()
	{
		memory=new JProgressBar(JProgressBar.HORIZONTAL,0,100);
		memory.addMouseListener(new GcMouseListener());
		memory.setStringPainted(true);
		add(memory);
		updateMemoryBar();
	}
*/

	private static class MemoryInfo
	{
		private long total;
		private long used;
		private long max;

		public MemoryInfo(Runtime runtime)
		{
			total = runtime.totalMemory();
			used = total - runtime.freeMemory();
			max = runtime.maxMemory();
		}

		public long getTotal()
		{
			return total;
		}

		public long getUsed()
		{
			return used;
		}

		public long getMax()
		{
			return max;
		}
	}

	private void updateMemoryBar()
	{
		this.memoryInfo = new MemoryInfo(runtime);

		// Tooltip
		{
			StringBuilder msg = new StringBuilder();
			msg.append("<html>");
			msg.append("Used memory: ");
			msg.append(HumanReadable.getHumanReadableSize(memoryInfo.getUsed(), usingBinaryUnits, false))
				.append("bytes");
			msg.append("<br>");
			msg.append("Total memory: ");
			msg.append(HumanReadable.getHumanReadableSize(memoryInfo.getTotal(), usingBinaryUnits, false))
				.append("bytes");
			msg.append("<br>");
			msg.append("Maximum memory: ");
			msg.append(HumanReadable.getHumanReadableSize(memoryInfo.getMax(), usingBinaryUnits, false))
				.append("bytes");
			msg.append("<br><br>");
			msg.append("Double-click to garbage-collect.");
			msg.append("</html>");
			setToolTipText(msg.toString());
		}
		repaint();
	}

	public void addNotify()
	{
		super.addNotify();
		setPaused(false);
	}

	public void removeNotify()
	{
		super.removeNotify();
		setPaused(true);
	}

	class PollRunnable
		implements Runnable
	{
		Runnable updateRunnable;
		private long frequency = 5000;

		public PollRunnable()
		{
			updateRunnable = new UpdateRunnable();
		}

		public void run()
		{
			for(; ;)
			{
				synchronized(MemoryStatus.this)
				{
					while(isPaused())
					{
						try
						{
							MemoryStatus.this.wait();
						}
						catch(InterruptedException e)
						{
							if(logger.isDebugEnabled()) logger.debug("Interrupted...", e);
							return;
						}
					}
				}
				EventQueue.invokeLater(updateRunnable);
				try
				{
					Thread.sleep(frequency);
				}
				catch(InterruptedException e)
				{
					if(logger.isDebugEnabled()) logger.debug("Interrupted...", e);
					return;
				}
			}
		}
	}

	class UpdateRunnable
		implements Runnable
	{
		public void run()
		{
			updateMemoryBar();
		}
	}

    //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="DM_GC",justification="")
	class GcMouseListener
		extends MouseAdapter
	{
		public void mouseClicked(MouseEvent evt)
		{
			if(evt.getClickCount() >= 2 && evt.getButton() == MouseEvent.BUTTON1)
			{
                // this is not a bug! - Performance - Explicit garbage collection; extremely dubious except in benchmarking code
				System.gc(); //NOSONAR
				if(logger.isInfoEnabled()) logger.info("Executed garbage-collection.");
				updateMemoryBar();
			}
		}
	}

	/*
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame("Test");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				Container cp = frame.getContentPane();
				cp.setLayout(new GridLayout(1, 1));
				MemoryStatus status = new MemoryStatus();
				status.setUsingTotal(false);
				status.setUsingBinaryUnits(true);
				status.setBorder(new EmptyBorder(5, 5, 5, 5));
				status.setBackground(Color.MAGENTA);
				status.setOpaque(true);
				cp.add(status);
				frame.pack();
				frame.setVisible(true);

			}
		});
	}
	*/
}
