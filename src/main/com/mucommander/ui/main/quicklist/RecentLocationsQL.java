/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.main.quicklist;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ShowRecentLocationsQLAction;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.quicklist.QuickListWithIcons;

import javax.swing.*;
import java.util.LinkedList;

/**
 * This quick list shows recently accessed locations.
 * 
 * @author Arik Hadas
 */
public class RecentLocationsQL extends QuickListWithIcons<AbstractFile> implements LocationListener {
	
	private static int MAX_ELEMENTS = 15;
	private LinkedList<AbstractFile> linkedList;

	public RecentLocationsQL() {
		super(ActionProperties.getActionLabel(ShowRecentLocationsQLAction.Descriptor.ACTION_ID), Translator.get("recent_locations_quick_list.empty_message"));
		
		linkedList = new LinkedList<AbstractFile>();
	}

	@Override
    protected void acceptListItem(AbstractFile item) {
		folderPanel.tryChangeCurrentFolder(item);
	}

	@Override
    public AbstractFile[] getData() {
		LinkedList<AbstractFile> list = (LinkedList<AbstractFile>)linkedList.clone();

		if (!list.remove(folderPanel.getCurrentFolder()))
			list.removeLast();
		
		return list.toArray(new AbstractFile[0]);
	}

	@Override
    protected Icon itemToIcon(AbstractFile item) {
		return getIconOfFile(item);
	}
	
	/*******************
	 * LocationListener
	 *******************/
	
	@Override
	public void locationChanged(LocationEvent locationEvent) {
		AbstractFile currentFolder = locationEvent.getFolderPanel().getCurrentFolder();
			
		if (!linkedList.remove(currentFolder) && linkedList.size() > MAX_ELEMENTS)
			linkedList.removeLast();
		linkedList.addFirst(currentFolder);
	}

	@Override
	public void locationCancelled(LocationEvent locationEvent) {}
	
	@Override
	public void locationChanging(LocationEvent locationEvent) {}

	@Override
	public void locationFailed(LocationEvent locationEvent) {}
}
