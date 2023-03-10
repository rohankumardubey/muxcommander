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

package com.mucommander.ui.main.tabs;

import com.mucommander.ui.action.impl.CloseDuplicateTabsAction;
import com.mucommander.ui.action.impl.CloseOtherTabsAction;
import com.mucommander.ui.action.impl.CloseTabAction;
import com.mucommander.ui.action.impl.MoveTabToOtherPanelAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.popup.MuActionsPopupMenu;

/**
* Contextual popup menu invoked by {@link FileTableTabbedPane} when right-clicking on a tab's title.
* 
* @author Arik Hadas
*/
class FileTableTabPopupMenu extends MuActionsPopupMenu {

	public FileTableTabPopupMenu(MainFrame mainFrame) {
		super(mainFrame);
		
//		addAction("duplicate");
		addAction(CloseTabAction.Descriptor.ACTION_ID);
		addAction(CloseDuplicateTabsAction.Descriptor.ACTION_ID);
		addAction(CloseOtherTabsAction.Descriptor.ACTION_ID);
		addAction(MoveTabToOtherPanelAction.Descriptor.ACTION_ID);
//		addAction("lock");
		add(new Separator());
//		addAction("Move To Other Panel");
//		addAction("Clone To Other Panel");
	}
}
