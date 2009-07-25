/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.ui.main.toolbar;

import com.mucommander.ui.action.impl.*;

import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * This class is responsible to handle the attributes of ToolBars - their actions and separators.
 * Every ToolBar should get its attributes from this class, and register in it for receiving attributes modifications.
 * 
 * @author Arik Hadas
 */
public class ToolBarAttributes {
	
	/** Command bar actions: Class instances or null to signify a separator */
    private static String actionIds[];
    
    private static boolean useDefaultActions = true;
    
    /** Contains all registered toolbar-attributes listeners, stored as weak references */
    private final static WeakHashMap listeners = new WeakHashMap();
    
    /** Default command bar actions: Class instances or null to signify a separator */
    private final static String[] DEFAULT_TOOLBAR_ACTIONS = new String[] {
            NewWindowAction.Descriptor.ACTION_ID,
            GoBackAction.Descriptor.ACTION_ID,
            GoForwardAction.Descriptor.ACTION_ID,
            GoToParentAction.Descriptor.ACTION_ID,
            null,
            GoToHomeAction.Descriptor.ACTION_ID,
            null,
            StopAction.Descriptor.ACTION_ID,
            null,
            MarkGroupAction.Descriptor.ACTION_ID,
            UnmarkGroupAction.Descriptor.ACTION_ID,
            null,
            SwapFoldersAction.Descriptor.ACTION_ID,
            SetSameFolderAction.Descriptor.ACTION_ID,
            null,
            PackAction.Descriptor.ACTION_ID,
            UnpackAction.Descriptor.ACTION_ID,
            null,
            AddBookmarkAction.Descriptor.ACTION_ID,
            EditBookmarksAction.Descriptor.ACTION_ID,
            EditCredentialsAction.Descriptor.ACTION_ID,
            null,
            ConnectToServerAction.Descriptor.ACTION_ID,
            ShowServerConnectionsAction.Descriptor.ACTION_ID,
            RunCommandAction.Descriptor.ACTION_ID,
            EmailAction.Descriptor.ACTION_ID,
            null,
            RevealInDesktopAction.Descriptor.ACTION_ID,
            ShowFilePropertiesAction.Descriptor.ACTION_ID,
            null,
            ShowPreferencesAction.Descriptor.ACTION_ID
    };

    /**
     * Removes leading and trailing separators (<code>null</code> elements) from the given action Class array, and
     * returns the trimmed action array.
     *
     * @param actions the action Class array to trim.
     * @return the trimmed action Class array, free of leading and trailing separators.
     */
    private static String[] trimActionsArray(String[] actions) {
        int start = 0;
        int end = actions.length;

        while(start<end && actions[start]==null)
            start++;

        if(start==end)
            return new String[]{};

        while(end>start && actions[end-1]==null)
            end--;

        int newLen = end-start;
        String newActions[] = new String[newLen];
        System.arraycopy(actions, start, newActions, 0, newLen);

        return newActions;
    }

    /**
     * Sets the toolbar actions to the given action classes. <code>null</code> elements are used to insert a separator
     * between buttons.
     *
     * @param actions the new toolbar actions classes
     */
    public static void setActions(String[] actions) {
        ToolBarAttributes.actionIds = trimActionsArray(actions);
    	useDefaultActions = false;
    	fireActionsChanged();
    }
    
    /**
     * Returns the actions classes that constitue the toolbar. <code>null</code> elements are used to insert a separator
     * between buttons.
     *
     * @return the actions classes that constitue the toolbar.
     */
    public static String[] getActions() {
    	return useDefaultActions ? DEFAULT_TOOLBAR_ACTIONS : actionIds;
    }
    
    // - Listeners -------------------------------------------------------------
    // -------------------------------------------------------------------------
    public static void addToolBarAttributesListener(ToolBarAttributesListener listener) {
    	synchronized(listeners) {listeners.put(listener, null);}
    }
    
    public static void removeToolBarAttributesListener(ToolBarAttributesListener listener) {
    	synchronized(listeners) {listeners.remove(listener);}
    }
    
    public static void fireActionsChanged() {
    	synchronized(listeners) {
            // Iterate on all listeners
            Iterator iterator = listeners.keySet().iterator();
            while(iterator.hasNext())
                ((ToolBarAttributesListener)iterator.next()).ToolBarActionsChanged();
        }
    }
}
