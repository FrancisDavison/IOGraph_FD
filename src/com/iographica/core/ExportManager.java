package com.iographica.core;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.iographica.events.IEventDispatcher;
import com.iographica.events.IEventHandler;
import com.iographica.events.IOEvent;


public class ExportManager implements IEventDispatcher {

	private ArrayList<IEventHandler> _eventHandlers;

	public void export(BufferedImage img) {
		BufferedImage canvas = null;
		if (Data.useScreenshot) {
			try {
				canvas = ImageIO.read(new File(Data.BACKGROUND_FILE_NAME));
			} catch (IOException e) {
			}
		}
		if (canvas == null) {
			canvas = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = canvas.createGraphics();
			if (Data.prefs.getBoolean(Data.USE_COLOR_SCHEME, false)) {
				g.setColor(Color.BLACK);
			} else {
				g.setColor(Color.WHITE);
			}
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
		}
		canvas.createGraphics().drawImage(img, 0, 0, null);

		FileDialog fd = new FileDialog(Data.mainFrame);
		fd.setMode(FileDialog.SAVE);
		String s = Data.period.replace(":", "-");
		String n = "IOGraphica - " + Data.time + (Data.usePeriod ? " (" + s.substring(0, 1).toLowerCase() + s.substring(1) + ")" : "") + ".png";
		fd.setFile(n);
		fd.setVisible(true);
		if (fd.getDirectory() == null) {
			dispatchEvent(Data.IMAGE_SAVED);
			return;
		}

		File file = new File(fd.getDirectory(), fd.getFile());
		try {
			ImageIO.write(canvas, "png", file);
		} catch (IOException err) {
			dispatchEvent(Data.IMAGE_SAVED);
			return;
		}
		canvas.flush();
		canvas = null;
		System.gc();
		
		dispatchEvent(Data.IMAGE_SAVED);
	}

	public void addEventHandler(IEventHandler handler) {
		if (_eventHandlers == null) {
			_eventHandlers = new ArrayList<IEventHandler>();
		}
		_eventHandlers.add(handler);
	}

	private void dispatchEvent(int type) {
		if (_eventHandlers != null) {
			final IOEvent event = new IOEvent(type, this);
			for (IEventHandler handler : _eventHandlers) {
				handler.onEvent(event);
			}
		}
	}
}
