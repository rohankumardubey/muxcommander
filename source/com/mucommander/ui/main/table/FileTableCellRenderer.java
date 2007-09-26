/*

 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.ui.main.table;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.theme.*;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;


/**
 * The custom <code>TableCellRenderer</code> class used by {@link FileTable} to render all table cells.
 *
 * <p>Quote from Sun's Javadoc : The table class defines a single cell renderer and uses it as a 
 * as a rubber-stamp for rendering all cells in the table;  it renders the first cell,
 * changes the contents of that cell renderer, shifts the origin to the new location, re-draws it, and so on.</p>
 *
 * <p>This <code>TableCellRender</code> is written from scratch instead of overridding <code>DefaultTableCellRender</code>
 * to provide a more efficient (and more specialized) implementation: each column is rendered using a dedicated 
 * {@link com.mucommander.ui.main.table.CellLabel CellLabel} which takes into account the column's specificities.
 * Having a dedicated for each column avoids calling the label's <code>set</code> methods (alignment, border, font...) 
 * each time {@link #getTableCellRendererComponent(javax.swing.JTable, Object, boolean, boolean, int, int)}}
 * is invoked, making cell rendering faster.
 *
 * <p>Contrarily to <code>DefaultTableCellRender</code>, <code>FileTableCellRenderer</code> does not extend JLabel,
 * instead the dedicated {@link CellLabel} class is used to render cells, making the implementation
 * less confusing IMO.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class FileTableCellRenderer implements Columns, TableCellRenderer, ThemeListener {

    private FileTable table;
    private FileTableModel tableModel;

    /** Custom JLabel used to render extension column's cells */
    private CellLabel extensionLabel;
    /** Custom JLabel used to render name column's cells */
    private CellLabel nameLabel;
    /** Custom JLabel used to render size column's cells */
    private CellLabel sizeLabel;
    /** Custom JLabel used to render date column's cells */
    private CellLabel dateLabel;
    /** Custom JLabel used to render permissions column's cells */
    private CellLabel permissionsLabel;



    // - Color definitions -----------------------------------------------------------
    // -------------------------------------------------------------------------------
    private static Color[][][][] colors;
    private static Color         unmatchedForeground;
    private static Color         unmatchedBackground;
    private static final int NORMAL               = 0;
    private static final int SELECTED             = 1;
    private static final int FOREGROUND           = 0;
    private static final int BACKGROUND           = 1;
    private static final int UNFOCUSED            = 0;
    private static final int FOCUSED              = 1;
    private static final int HIDDEN_FILE          = 0;
    private static final int FOLDER               = 1;
    private static final int ARCHIVE              = 2;
    private static final int SYMLINK              = 3;
    private static final int MARKED               = 4;
    private static final int PLAIN_FILE           = 5;



    // - Font definitions ------------------------------------------------------------
    // -------------------------------------------------------------------------------
    private static Font font;



    // - Initialisation --------------------------------------------------------------
    // -------------------------------------------------------------------------------
    static {
        colors = new Color[2][2][2][6];

        // Normal foreground colors.
        colors[FOCUSED][FOREGROUND][NORMAL][HIDDEN_FILE]     = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_FOREGROUND_COLOR);
        colors[FOCUSED][FOREGROUND][NORMAL][FOLDER]          = ThemeManager.getCurrentColor(Theme.FOLDER_FOREGROUND_COLOR);
        colors[FOCUSED][FOREGROUND][NORMAL][ARCHIVE]         = ThemeManager.getCurrentColor(Theme.ARCHIVE_FOREGROUND_COLOR);
        colors[FOCUSED][FOREGROUND][NORMAL][SYMLINK]         = ThemeManager.getCurrentColor(Theme.SYMLINK_FOREGROUND_COLOR);
        colors[FOCUSED][FOREGROUND][NORMAL][MARKED]          = ThemeManager.getCurrentColor(Theme.MARKED_FOREGROUND_COLOR);
        colors[FOCUSED][FOREGROUND][NORMAL][PLAIN_FILE]      = ThemeManager.getCurrentColor(Theme.FILE_FOREGROUND_COLOR);

        // Normal background colors.
        colors[FOCUSED][BACKGROUND][NORMAL][HIDDEN_FILE]     = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_BACKGROUND_COLOR);
        colors[FOCUSED][BACKGROUND][NORMAL][FOLDER]          = ThemeManager.getCurrentColor(Theme.FOLDER_BACKGROUND_COLOR);
        colors[FOCUSED][BACKGROUND][NORMAL][ARCHIVE]         = ThemeManager.getCurrentColor(Theme.ARCHIVE_BACKGROUND_COLOR);
        colors[FOCUSED][BACKGROUND][NORMAL][SYMLINK]         = ThemeManager.getCurrentColor(Theme.SYMLINK_BACKGROUND_COLOR);
        colors[FOCUSED][BACKGROUND][NORMAL][MARKED]          = ThemeManager.getCurrentColor(Theme.MARKED_BACKGROUND_COLOR);
        colors[FOCUSED][BACKGROUND][NORMAL][PLAIN_FILE]      = ThemeManager.getCurrentColor(Theme.FILE_BACKGROUND_COLOR);

        // Normal unfocused background colors.
        colors[UNFOCUSED][BACKGROUND][NORMAL][HIDDEN_FILE]   = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_UNFOCUSED_BACKGROUND_COLOR);
        colors[UNFOCUSED][BACKGROUND][NORMAL][FOLDER]        = ThemeManager.getCurrentColor(Theme.FOLDER_UNFOCUSED_BACKGROUND_COLOR);
        colors[UNFOCUSED][BACKGROUND][NORMAL][ARCHIVE]       = ThemeManager.getCurrentColor(Theme.ARCHIVE_UNFOCUSED_BACKGROUND_COLOR);
        colors[UNFOCUSED][BACKGROUND][NORMAL][SYMLINK]       = ThemeManager.getCurrentColor(Theme.SYMLINK_UNFOCUSED_BACKGROUND_COLOR);
        colors[UNFOCUSED][BACKGROUND][NORMAL][MARKED]        = ThemeManager.getCurrentColor(Theme.MARKED_UNFOCUSED_BACKGROUND_COLOR);
        colors[UNFOCUSED][BACKGROUND][NORMAL][PLAIN_FILE]    = ThemeManager.getCurrentColor(Theme.FILE_UNFOCUSED_BACKGROUND_COLOR);

        // Normal unfocused foreground colors.
        colors[UNFOCUSED][FOREGROUND][NORMAL][HIDDEN_FILE]   = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_UNFOCUSED_FOREGROUND_COLOR);
        colors[UNFOCUSED][FOREGROUND][NORMAL][FOLDER]        = ThemeManager.getCurrentColor(Theme.FOLDER_UNFOCUSED_FOREGROUND_COLOR);
        colors[UNFOCUSED][FOREGROUND][NORMAL][ARCHIVE]       = ThemeManager.getCurrentColor(Theme.ARCHIVE_UNFOCUSED_FOREGROUND_COLOR);
        colors[UNFOCUSED][FOREGROUND][NORMAL][SYMLINK]       = ThemeManager.getCurrentColor(Theme.SYMLINK_UNFOCUSED_FOREGROUND_COLOR);
        colors[UNFOCUSED][FOREGROUND][NORMAL][MARKED]        = ThemeManager.getCurrentColor(Theme.MARKED_UNFOCUSED_FOREGROUND_COLOR);
        colors[UNFOCUSED][FOREGROUND][NORMAL][PLAIN_FILE]    = ThemeManager.getCurrentColor(Theme.FILE_UNFOCUSED_FOREGROUND_COLOR);

        // Selected foreground colors.
        colors[FOCUSED][FOREGROUND][SELECTED][HIDDEN_FILE]   = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR);
        colors[FOCUSED][FOREGROUND][SELECTED][FOLDER]        = ThemeManager.getCurrentColor(Theme.FOLDER_SELECTED_FOREGROUND_COLOR);
        colors[FOCUSED][FOREGROUND][SELECTED][ARCHIVE]       = ThemeManager.getCurrentColor(Theme.ARCHIVE_SELECTED_FOREGROUND_COLOR);
        colors[FOCUSED][FOREGROUND][SELECTED][SYMLINK]       = ThemeManager.getCurrentColor(Theme.SYMLINK_SELECTED_FOREGROUND_COLOR);
        colors[FOCUSED][FOREGROUND][SELECTED][MARKED]        = ThemeManager.getCurrentColor(Theme.MARKED_SELECTED_FOREGROUND_COLOR);
        colors[FOCUSED][FOREGROUND][SELECTED][PLAIN_FILE]    = ThemeManager.getCurrentColor(Theme.FILE_SELECTED_FOREGROUND_COLOR);

        // Selected background colors.
        colors[FOCUSED][BACKGROUND][SELECTED][HIDDEN_FILE]   = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_SELECTED_BACKGROUND_COLOR);
        colors[FOCUSED][BACKGROUND][SELECTED][FOLDER]        = ThemeManager.getCurrentColor(Theme.FOLDER_SELECTED_BACKGROUND_COLOR);
        colors[FOCUSED][BACKGROUND][SELECTED][ARCHIVE]       = ThemeManager.getCurrentColor(Theme.ARCHIVE_SELECTED_BACKGROUND_COLOR);
        colors[FOCUSED][BACKGROUND][SELECTED][SYMLINK]       = ThemeManager.getCurrentColor(Theme.SYMLINK_SELECTED_BACKGROUND_COLOR);
        colors[FOCUSED][BACKGROUND][SELECTED][MARKED]        = ThemeManager.getCurrentColor(Theme.MARKED_SELECTED_BACKGROUND_COLOR);
        colors[FOCUSED][BACKGROUND][SELECTED][PLAIN_FILE]    = ThemeManager.getCurrentColor(Theme.FILE_SELECTED_BACKGROUND_COLOR);

        // Selected unfocused background colors.
        colors[UNFOCUSED][BACKGROUND][SELECTED][HIDDEN_FILE] = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR);
        colors[UNFOCUSED][BACKGROUND][SELECTED][FOLDER]      = ThemeManager.getCurrentColor(Theme.FOLDER_SELECTED_UNFOCUSED_BACKGROUND_COLOR);
        colors[UNFOCUSED][BACKGROUND][SELECTED][ARCHIVE]     = ThemeManager.getCurrentColor(Theme.ARCHIVE_SELECTED_UNFOCUSED_BACKGROUND_COLOR);
        colors[UNFOCUSED][BACKGROUND][SELECTED][SYMLINK]     = ThemeManager.getCurrentColor(Theme.SYMLINK_SELECTED_UNFOCUSED_BACKGROUND_COLOR);
        colors[UNFOCUSED][BACKGROUND][SELECTED][MARKED]      = ThemeManager.getCurrentColor(Theme.MARKED_SELECTED_UNFOCUSED_BACKGROUND_COLOR);
        colors[UNFOCUSED][BACKGROUND][SELECTED][PLAIN_FILE]  = ThemeManager.getCurrentColor(Theme.FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR);

        // Selected unfocused foreground colors.
        colors[UNFOCUSED][FOREGROUND][SELECTED][HIDDEN_FILE] = ThemeManager.getCurrentColor(Theme.HIDDEN_FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR);
        colors[UNFOCUSED][FOREGROUND][SELECTED][FOLDER]      = ThemeManager.getCurrentColor(Theme.FOLDER_SELECTED_UNFOCUSED_FOREGROUND_COLOR);
        colors[UNFOCUSED][FOREGROUND][SELECTED][ARCHIVE]     = ThemeManager.getCurrentColor(Theme.ARCHIVE_SELECTED_UNFOCUSED_FOREGROUND_COLOR);
        colors[UNFOCUSED][FOREGROUND][SELECTED][SYMLINK]     = ThemeManager.getCurrentColor(Theme.SYMLINK_SELECTED_UNFOCUSED_FOREGROUND_COLOR);
        colors[UNFOCUSED][FOREGROUND][SELECTED][MARKED]      = ThemeManager.getCurrentColor(Theme.MARKED_SELECTED_UNFOCUSED_FOREGROUND_COLOR);
        colors[UNFOCUSED][FOREGROUND][SELECTED][PLAIN_FILE]  = ThemeManager.getCurrentColor(Theme.FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR);

        unmatchedForeground                                  = ThemeManager.getCurrentColor(Theme.FILE_TABLE_UNMATCHED_FOREGROUND_COLOR);
        unmatchedBackground                                  = ThemeManager.getCurrentColor(Theme.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR);
        font                                                 = ThemeManager.getCurrentFont(Theme.FILE_TABLE_FONT);
    }


    public FileTableCellRenderer(FileTable table) {
    	this.table = table;
        this.tableModel = table.getFileTableModel();

        // Create a label for each column
        this.extensionLabel = new CellLabel();
        this.nameLabel = new CellLabel();
        this.sizeLabel = new CellLabel();
        this.dateLabel = new CellLabel();
        this.permissionsLabel = new CellLabel();

        // Set labels' font.
        setCellLabelsFont(font);

        // Set labels' text alignment
        extensionLabel.setHorizontalAlignment(CellLabel.CENTER);
        nameLabel.setHorizontalAlignment(CellLabel.LEFT);
        sizeLabel.setHorizontalAlignment(CellLabel.RIGHT);
        dateLabel.setHorizontalAlignment(CellLabel.RIGHT);
        permissionsLabel.setHorizontalAlignment(CellLabel.LEFT);

        // Listens to certain configuration variables
        ThemeManager.addCurrentThemeListener(this);
    }


    /**
     * Returns the font used to render all table cells.
     */
    public static Font getCellFont() {return font;}

	
    /**
     * Sets CellLabels' font to the current one.
     */
    private void setCellLabelsFont(Font newFont) {
        // Set custom font
        nameLabel.setFont(newFont);
        sizeLabel.setFont(newFont);
        dateLabel.setFont(newFont);
        permissionsLabel.setFont(newFont);
        // No need to set extension label's font as only icons (no text) are rendered by this label
    } 


    
    ///////////////////////////////
    // TableCellRenderer methods //
    ///////////////////////////////

    private static int getColorIndex(int row, AbstractFile file, FileTableModel tableModel) {
        // Parent directory.
        if(row==0 && tableModel.hasParentFolder())
            return FOLDER;

        // Marked file.
        if(tableModel.isRowMarked(row))
            return MARKED;

        // Symlink.
        if(file.isSymlink())
            return SYMLINK;

        // Hidden file.
        if(file.isHidden())
            return HIDDEN_FILE;

        // Directory.
        if(file.isDirectory())
            return FOLDER;

        // Archive.
        if(file.isBrowsable())
            return ARCHIVE;

        // Plain file.
        return PLAIN_FILE;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        int                   columnId;
        int                   colorIndex;
        int                   focusedIndex;
        int                   selectedIndex;
        CellLabel             label;
        AbstractFile          file;
        boolean               matches;

        // Need to check that row index is not out of bounds because when the folder
        // has just been changed, the JTable may try to repaint the old folder and
        // ask for a row index greater than the length if the old folder contained more files
        if(row < 0 || row >= tableModel.getRowCount())
            return null;

        // Sanity check.
        file = tableModel.getCachedFileAtRow(row);
        if(file==null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("tableModel.getCachedFileAtRow("+row+") RETURNED NULL !"); 
            return null;
        }

        if(!table.hasFocus())
            matches = true;
        else {
            FileTable.QuickSearch search;
            search = this.table.getQuickSearch();
            if(search.isActive())
                matches = search.matches((row == 0 && tableModel.hasParentFolder()) ? ".." : tableModel.getFileAtRow(row).getName());
            else
                matches = true;
        }

        // Retrieves the various indexes of the colors to apply.
        // Selection only applies when the table is the active one
        selectedIndex =  (isSelected && ((FileTable)table).isActiveTable()) ? SELECTED : NORMAL;
        focusedIndex  = table.hasFocus() ? FOCUSED : UNFOCUSED;
        colorIndex    = getColorIndex(row, file, tableModel);

        columnId = table.convertColumnIndexToModel(column);
        
        // Extension/icon column: return ImageIcon instance
        if(columnId == EXTENSION) {
            label = extensionLabel;

            // Set file icon (parent folder icon if '..' file)
            extensionLabel.setIcon(
                                   row==0 && tableModel.hasParentFolder()?
                                   FileIcons.getParentFolderIcon()
                                   :FileIcons.getFileIcon(file)
                                   );
        }
        // Any other column (name, date or size)
        else {
            switch(columnId) {
                case NAME:
                    label = nameLabel;
                    break;
                case SIZE:
                    label = sizeLabel;
                    break;
                case DATE:
                    label = dateLabel;
                    break;
                default:
                    label = permissionsLabel;
                    break;
            }

            String text = (String)value;
            if(matches || isSelected)
                label.setForeground(colors[focusedIndex][FOREGROUND][selectedIndex][colorIndex]);
            else
                label.setForeground(unmatchedForeground);

            // If component's preferred width is bigger than column width then the component is not entirely
            // visible so we set a tooltip text that will display the whole text when mouse is over the 
            // component
            if (table.getColumnModel().getColumn(column).getWidth() < label.getPreferredSize().getWidth())
                label.setToolTipText(text);
            // Have to set it to null otherwise the defaultRender sets the tooltip text to the last one
            // specified
            else
                label.setToolTipText(null);


            // Set label's text
            label.setText(text); 
        }

        // Set background color depending on whether the row is selected or not, and whether the table has focus or not
        if(matches || isSelected)
            label.setBackground(colors[focusedIndex][BACKGROUND][selectedIndex][colorIndex]);
        else
            label.setBackground(unmatchedBackground);

        return label;
    }



    // - Theme listening -------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /**
     * Receives theme color changes notifications.
     */
    public void colorChanged(ColorChangedEvent event) {
        switch(event.getColorId()) {
            // Plain file color.
        case Theme.FILE_FOREGROUND_COLOR:
            colors[FOCUSED][FOREGROUND][NORMAL][PLAIN_FILE] = event.getColor();
            break;

            // Selected file color.
        case Theme.FILE_SELECTED_FOREGROUND_COLOR:
            colors[FOCUSED][FOREGROUND][SELECTED][PLAIN_FILE] = event.getColor();
            break;

            // Hidden files.
        case Theme.HIDDEN_FILE_FOREGROUND_COLOR:
            colors[FOCUSED][FOREGROUND][NORMAL][HIDDEN_FILE] = event.getColor();
            break;

            // Selected hidden files.
        case Theme.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR:
            colors[FOCUSED][FOREGROUND][SELECTED][HIDDEN_FILE] = event.getColor();
            break;

            // Folders.
        case Theme.FOLDER_FOREGROUND_COLOR:
            colors[FOCUSED][FOREGROUND][NORMAL][FOLDER] = event.getColor();
            break;

            // Selected folders.
        case Theme.FOLDER_SELECTED_FOREGROUND_COLOR:
            colors[FOCUSED][FOREGROUND][SELECTED][FOLDER] = event.getColor();
            break;

            // Archives.
        case Theme.ARCHIVE_FOREGROUND_COLOR:
            colors[FOCUSED][FOREGROUND][NORMAL][ARCHIVE] = event.getColor();
            break;

            // Selected archives.
        case Theme.ARCHIVE_SELECTED_FOREGROUND_COLOR:
            colors[FOCUSED][FOREGROUND][SELECTED][ARCHIVE] = event.getColor();
            break;

            // Symlinks.
        case Theme.SYMLINK_FOREGROUND_COLOR:
            colors[FOCUSED][FOREGROUND][NORMAL][SYMLINK] = event.getColor();
            break;

            // Selected symlinks.
        case Theme.SYMLINK_SELECTED_FOREGROUND_COLOR:
            colors[FOCUSED][FOREGROUND][SELECTED][SYMLINK] = event.getColor();
            break;

            // Marked files.
        case Theme.MARKED_FOREGROUND_COLOR:
            colors[FOCUSED][FOREGROUND][NORMAL][MARKED] = event.getColor();
            break;

            // Selected marked files.
        case Theme.MARKED_SELECTED_FOREGROUND_COLOR:
            colors[FOCUSED][FOREGROUND][SELECTED][MARKED] = event.getColor();
            break;

            // Plain file color.
        case Theme.FILE_UNFOCUSED_FOREGROUND_COLOR:
            colors[UNFOCUSED][FOREGROUND][NORMAL][PLAIN_FILE] = event.getColor();
            break;

            // Selected file color.
        case Theme.FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
            colors[UNFOCUSED][FOREGROUND][SELECTED][PLAIN_FILE] = event.getColor();
            break;

            // Hidden files.
        case Theme.HIDDEN_FILE_UNFOCUSED_FOREGROUND_COLOR:
            colors[UNFOCUSED][FOREGROUND][NORMAL][HIDDEN_FILE] = event.getColor();
            break;

            // Selected hidden files.
        case Theme.HIDDEN_FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
            colors[UNFOCUSED][FOREGROUND][SELECTED][HIDDEN_FILE] = event.getColor();
            break;

            // Folders.
        case Theme.FOLDER_UNFOCUSED_FOREGROUND_COLOR:
            colors[UNFOCUSED][FOREGROUND][NORMAL][FOLDER] = event.getColor();
            break;

            // Selected folders.
        case Theme.FOLDER_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
            colors[UNFOCUSED][FOREGROUND][SELECTED][FOLDER] = event.getColor();
            break;

            // Archives.
        case Theme.ARCHIVE_UNFOCUSED_FOREGROUND_COLOR:
            colors[UNFOCUSED][FOREGROUND][NORMAL][ARCHIVE] = event.getColor();
            break;

            // Selected archives.
        case Theme.ARCHIVE_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
            colors[UNFOCUSED][FOREGROUND][SELECTED][ARCHIVE] = event.getColor();
            break;

            // Symlinks.
        case Theme.SYMLINK_UNFOCUSED_FOREGROUND_COLOR:
            colors[UNFOCUSED][FOREGROUND][NORMAL][SYMLINK] = event.getColor();
            break;

            // Selected symlinks.
        case Theme.SYMLINK_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
            colors[UNFOCUSED][FOREGROUND][SELECTED][SYMLINK] = event.getColor();
            break;

            // Marked files.
        case Theme.MARKED_UNFOCUSED_FOREGROUND_COLOR:
            colors[UNFOCUSED][FOREGROUND][NORMAL][MARKED] = event.getColor();
            break;

            // Selected marked files.
        case Theme.MARKED_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
            colors[UNFOCUSED][FOREGROUND][SELECTED][MARKED] = event.getColor();
            break;

            // Plain file color.
        case Theme.FILE_BACKGROUND_COLOR:
            colors[FOCUSED][BACKGROUND][NORMAL][PLAIN_FILE] = event.getColor();
            break;

            // Selected file color.
        case Theme.FILE_SELECTED_BACKGROUND_COLOR:
            colors[FOCUSED][BACKGROUND][SELECTED][PLAIN_FILE] = event.getColor();
            break;

            // Hidden files.
        case Theme.HIDDEN_FILE_BACKGROUND_COLOR:
            colors[FOCUSED][BACKGROUND][NORMAL][HIDDEN_FILE] = event.getColor();
            break;

            // Selected hidden files.
        case Theme.HIDDEN_FILE_SELECTED_BACKGROUND_COLOR:
            colors[FOCUSED][BACKGROUND][SELECTED][HIDDEN_FILE] = event.getColor();
            break;

            // Folders.
        case Theme.FOLDER_BACKGROUND_COLOR:
            colors[FOCUSED][BACKGROUND][NORMAL][FOLDER] = event.getColor();
            break;

            // Selected folders.
        case Theme.FOLDER_SELECTED_BACKGROUND_COLOR:
            colors[FOCUSED][BACKGROUND][SELECTED][FOLDER] = event.getColor();
            break;

            // Archives.
        case Theme.ARCHIVE_BACKGROUND_COLOR:
            colors[FOCUSED][BACKGROUND][NORMAL][ARCHIVE] = event.getColor();
            break;

            // Selected archives.
        case Theme.ARCHIVE_SELECTED_BACKGROUND_COLOR:
            colors[FOCUSED][BACKGROUND][SELECTED][ARCHIVE] = event.getColor();
            break;

            // Symlinks.
        case Theme.SYMLINK_BACKGROUND_COLOR:
            colors[FOCUSED][BACKGROUND][NORMAL][SYMLINK] = event.getColor();
            break;

            // Selected symlinks.
        case Theme.SYMLINK_SELECTED_BACKGROUND_COLOR:
            colors[FOCUSED][BACKGROUND][SELECTED][SYMLINK] = event.getColor();
            break;

            // Marked files.
        case Theme.MARKED_BACKGROUND_COLOR:
            colors[FOCUSED][BACKGROUND][NORMAL][MARKED] = event.getColor();
            break;

            // Selected marked files.
        case Theme.MARKED_SELECTED_BACKGROUND_COLOR:
            colors[FOCUSED][BACKGROUND][SELECTED][MARKED] = event.getColor();
            break;

            // Plain file color.
        case Theme.FILE_UNFOCUSED_BACKGROUND_COLOR:
            colors[UNFOCUSED][BACKGROUND][NORMAL][PLAIN_FILE] = event.getColor();
            break;

            // Selected file color.
        case Theme.FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
            colors[UNFOCUSED][BACKGROUND][SELECTED][PLAIN_FILE] = event.getColor();
            break;

            // Hidden files.
        case Theme.HIDDEN_FILE_UNFOCUSED_BACKGROUND_COLOR:
            colors[UNFOCUSED][BACKGROUND][NORMAL][HIDDEN_FILE] = event.getColor();
            break;

            // Selected hidden files.
        case Theme.HIDDEN_FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
            colors[UNFOCUSED][BACKGROUND][SELECTED][HIDDEN_FILE] = event.getColor();
            break;

            // Folders.
        case Theme.FOLDER_UNFOCUSED_BACKGROUND_COLOR:
            colors[UNFOCUSED][BACKGROUND][NORMAL][FOLDER] = event.getColor();
            break;

            // Selected folders.
        case Theme.FOLDER_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
            colors[UNFOCUSED][BACKGROUND][SELECTED][FOLDER] = event.getColor();
            break;

            // Archives.
        case Theme.ARCHIVE_UNFOCUSED_BACKGROUND_COLOR:
            colors[UNFOCUSED][BACKGROUND][NORMAL][ARCHIVE] = event.getColor();
            break;

            // Selected archives.
        case Theme.ARCHIVE_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
            colors[UNFOCUSED][BACKGROUND][SELECTED][ARCHIVE] = event.getColor();
            break;

            // Symlinks.
        case Theme.SYMLINK_UNFOCUSED_BACKGROUND_COLOR:
            colors[UNFOCUSED][BACKGROUND][NORMAL][SYMLINK] = event.getColor();
            break;

            // Selected symlinks.
        case Theme.SYMLINK_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
            colors[UNFOCUSED][BACKGROUND][SELECTED][SYMLINK] = event.getColor();
            break;

            // Marked files.
        case Theme.MARKED_UNFOCUSED_BACKGROUND_COLOR:
            colors[UNFOCUSED][BACKGROUND][NORMAL][MARKED] = event.getColor();
            break;

            // Selected marked files.
        case Theme.MARKED_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
            colors[UNFOCUSED][BACKGROUND][SELECTED][MARKED] = event.getColor();
            break;

            // Unmatched foreground
        case Theme.FILE_TABLE_UNMATCHED_FOREGROUND_COLOR:
            unmatchedForeground = event.getColor();
            break;

            // Unmached background
        case Theme.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR:
            unmatchedBackground = event.getColor();
            break;

        default:
            return;
        }
        table.repaint();
    }

    /**
     * Receives theme font changes notifications.
     */
    public void fontChanged(FontChangedEvent event) {
        if(event.getFontId() == Theme.FILE_TABLE_FONT) {
            font = event.getFont();
            setCellLabelsFont(font);
        }
    }
}
