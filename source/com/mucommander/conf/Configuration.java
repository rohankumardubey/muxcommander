/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.conf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.Stack;
import java.util.EmptyStackException;

/**
 * @author Nicolas Rinaudo
 */
public class Configuration {
    // - Class variables -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Used to get access to the configuration source's input and output streams. */
    private ConfigurationSource        source;
    /** Used to create objects that will read from the configuration source. */
    private ConfigurationReaderFactory readerFactory;
    /** Used to create objects that will write to the configuration source. */
    private ConfigurationWriterFactory writerFactory;
    /** Holds the content of the configuration file. */
    private ConfigurationSection       root = new ConfigurationSection();
    /** Contains all registered configuration listeners, stored as weak references */
    private static WeakHashMap         listeners = new WeakHashMap();



    // - Synchronisation locks -----------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Used to synchronise concurent access of the configuration source. */
    private Object sourceLock = new Object();
    /** Used to synchronise concurent access of the reader factory. */
    private Object readerLock = new Object();
    /** Used to synchronise concurent access of the writer factory. */
    private Object writerLock = new Object();



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public Configuration() {}



    // - Configuration source --------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Sets the source that will be used to read and write configuration information.
     * @param s new configuration source.
     */
    public void setSource(ConfigurationSource s) {synchronized(sourceLock) {source = s;}}

    /**
     * Returns the current configuration source.
     * @return the current configuration source.
     */
    public ConfigurationSource getSource() {synchronized(sourceLock) {return source;}}



    // - Reader handling -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Sets the factory that will be used to create {@link ConfigurationReader reader} instances.
     * @param f factory that will be used to create reader instances.
     */
    public void setReaderFactory(ConfigurationReaderFactory f) {synchronized(readerLock) {readerFactory = f;}}

    /**
     * Returns the factory that is being used to create {@link ConfigurationReader reader} instances.
     * @return the factory that is being used to create reader instances.
     */
    public ConfigurationReaderFactory getReaderFactory() {synchronized(readerLock) {return readerFactory;}}

    /**
     * Returns an instance of the class that will be used to read configuration data.
     * <p>
     * By default, this method will return an instance of {@link XmlConfigurationReader}. However, this can be
     * modified by {@link #setReaderFactory(ConfigurationReaderFactory)}.
     * </p>
     * @return an instance of the class that will be used to read configuration data.
     */
    public ConfigurationReader getReader() {
        ConfigurationReaderFactory factory;

        // If no factory has been set, return an XML configuration reader.
        if((factory = getReaderFactory()) == null)
            return new XmlConfigurationReader();

        return factory.getReaderInstance();
    }



    // - Writer handling -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Sets the factory that will be used to create {@link ConfigurationWriter writer} instances.
     * @param f factory that will be used to create writer instances.
     */
    public void setWriterFactory(ConfigurationWriterFactory f) {synchronized(writerLock) {writerFactory = f;}}

    /**
     * Returns the factory that is being used to create {@link ConfigurationWriter writer} instances.
     * @return the factory that is being used to create writer instances.
     */
    public ConfigurationWriterFactory getWriterFactory() {synchronized(writerLock) {return writerFactory;}}

    /**
     * Returns an instance of the class that will be used to write configuration data.
     * <p>
     * By default, this method will return an instance of {@link XmlConfigurationWriter}. However, this can be
     * modified by {@link #setWriterFactory(ConfigurationWriterFactory)}.
     * </p>
     * @return an instance of the class that will be used to read configuration data.
     */
    public ConfigurationWriter getWriter() {
        ConfigurationWriterFactory factory;

        // If no factory was set, return an XML configuration writer.
        if((factory = getWriterFactory()) == null)
            return new XmlConfigurationWriter();

        return factory.getWriterInstance();
    }



    // - Configuration reading -------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Loads configuration from the specified input stream, using the specified configuration reader.
     * @param  in                           where to read the configuration from.
     * @param  reader                       reader that will be used to interpret the content of <code>in</code>.
     * @throws IOException                  if an I/O error occurs.
     * @throws ConfigurationException       if a configuration error occurs.
     * @throws ConfigurationFormatException if there is an error in the format of the configuration file.
     * @see                                 #write(OutputStream,ConfigurationWriter)
     */
    public synchronized void read(InputStream in, ConfigurationReader reader) throws ConfigurationException, IOException, ConfigurationFormatException {
        reader.read(in, new ConfigurationLoader(root));
    }

    /**
     * Loads configuration from the specified input stream.
     * <p>
     * This method will use the configuration reader set by {@link #setReaderFactory(ConfigurationReaderFactory)} if any,
     * or an {@link com.mucommander.conf.XmlConfigurationReader} instance if not.
     * </p>
     * @param  in                           where to read the configuration from.
     * @throws ConfigurationException       if a configuration error occurs.
     * @throws ConfigurationFormatException if there is an error in the format of the configuration file.
     * @throws IOException                  if an I/O error occurs.
     * @see                                 #write(OutputStream)
     */
    public void read(InputStream in) throws ConfigurationException, IOException, ConfigurationFormatException {read(in, getReader());}

    /**
     * Loads configuration using the specified configuration reader.
     * <p>
     * This method will use the input stream provided by {@link #setSource(ConfigurationSource)} if any, or
     * fail otherwise.
     * </p>
     * @param  reader                       reader that will be used to interpret the content of <code>in</code>.
     * @throws IOException                  if an I/O error occurs.
     * @throws ConfigurationFormatException if there is an error in the format of the configuration file.
     * @throws ConfigurationException       if a configuration error occurs.
     * @see                                 #write(ConfigurationWriter)
     */
    public void read(ConfigurationReader reader) throws IOException, ConfigurationException, ConfigurationFormatException {
        InputStream in;

        in = null;
        try {read(in = getSource().getInputStream(), reader);}
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }
    }

    /**
     * Loads configuration.
     * <p>
     * If a reader has been specified through {@link #setReaderFactory(ConfigurationReaderFactory)}, it
     * will be used to analyse the configuration. Otherwise, an {@link com.mucommander.conf.XmlConfigurationReader} instance
     * will be used.
     * </p>
     * <p>
     * If a configuration source has been specified through {@link #setSource(ConfigurationSource)}, it will be
     * used. Otherwise, this method will fail.
     * </p>
     * @throws IOException                  if an I/O error occurs.
     * @throws ConfigurationFormatException if there is an error in the format of the configuration file.
     * @throws ConfigurationException       if a configuration error occurs.
     * @see                                 #write()
     */
    public void read() throws ConfigurationException, IOException, ConfigurationFormatException {read(getReader());}



    // - Configuration writing -------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Writes configuration to the specified output stream using the specified writer.
     * @param out                     where to write the configuration to.
     * @param writer                  writer that will be used to format the configuration.
     * @throws ConfigurationException if any error occurs.
     * @see                           #read(InputStream,ConfigurationReader)
     */
    public void write(OutputStream out, ConfigurationWriter writer) throws ConfigurationException {
        writer.setOutputStream(out);
        build(writer);
    }

    /**
     * Writes configuration to the specified output stream.
     * <p>
     * If a writer was specified through {@link #setWriterFactory(ConfigurationWriterFactory)}, this will be
     * used to format the configuration. Otherwise, an {@link XmlConfigurationWriter} will be used.
     * </p>
     * @param out                     where to write the configuration to.
     * @throws ConfigurationException if any error occurs.
     * @see                           #read(InputStream)
     */
    public void write(OutputStream out) throws ConfigurationException {write(out, getWriter());}

    /**
     * Writes configuration using the specified writer.
     * <p>
     * If a configuration source was specified through {@link #setSource(ConfigurationSource)}, it will be used
     * to open an output stream. Otherwise, this method will fail.
     * </p>
     * @param writer                  writer that will be used to format the configuration.
     * @throws ConfigurationException if any error occurs.
     * @throws IOException            if any I/O error occurs.
     * @see                           #read(ConfigurationReader)
     */
    public void write(ConfigurationWriter writer) throws IOException, ConfigurationException {
        OutputStream out;

        out = null;
        try {write(out = getSource().getOutputStream(), writer);}
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }

    /**
     * Writes configuration.
     * <p>
     * If a writer was specified through {@link #setWriterFactory(ConfigurationWriterFactory)}, this will be
     * used to format the configuration. Otherwise, an {@link XmlConfigurationWriter} will be used.
     * </p>
     * <p>
     * If a configuration source was specified through {@link #setSource(ConfigurationSource)}, it will be used
     * to open an output stream. Otherwise, this method will fail.
     * </p>
     * @throws ConfigurationException if any error occurs.
     * @throws IOException            if any I/O error occurs.
     * @see                           #read()
     */
    public void write() throws IOException, ConfigurationException {write(getWriter());}



    // - Configuration building ------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Recursively explores the specified section and sends messages to the specified builder.
     * @param  builder                object that will receive building events.
     * @param  root                   section to explore.
     * @throws ConfigurationException if any error occurs.
     */
    private synchronized void build(ConfigurationBuilder builder, ConfigurationSection root) throws ConfigurationException {
        Enumeration          enumeration; // Enumeration on the section's variables, then subsections.
        String               name;        // Name of the current variable, then section.
        String               value;       // Value of the current variable.
        ConfigurationSection section;     // Current section.

        // Explores the section's variables.
        enumeration = root.variableNames();
        while(enumeration.hasMoreElements())
            builder.addVariable(name = (String)enumeration.nextElement(), root.getVariable(name));

        // Explores the section's subsections.
        enumeration = root.sectionNames();
        while(enumeration.hasMoreElements()) {
            name    = (String)enumeration.nextElement();
            section = root.getSection(name);

            // We only go through subsections if contain either variables or subsections of their own.
            if(section.hasSections() || section.hasVariables()) {
                builder.startSection(name);
                build(builder, section);
                builder.endSection(name);
            }
        }
    }

    /**
     * Explores the whole configuration tree and sends build messages to <code>builder</code>.
     * @param  builder                object that will receive configuration building messages.
     * @throws ConfigurationException if any error occurs while going through the configuration tree.
     */
    public void build(ConfigurationBuilder builder) throws ConfigurationException {
        builder.startConfiguration();
        build(builder, root);
        builder.endConfiguration();
    }



    // - Variable setting ------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Moves the value of <code>fromVar</code> to <code>toVar</code>.
     * <p>
     * At the end of this call, <code>fromVar</code> will have been deleted. Note that if <code>fromVar</code> doesn't exist,
     * but <code>toVar</code> does, <code>toVar</code> will be deleted.
     * </p>
     * <p>
     * This method might trigger as many as two {@link ConfigurationEvent events}:
     * <ul>
     *  <li>One when <code>fromVar</code> is removed.</li>
     *  <li>One when <code>toVar</code> is set.</li>
     * </ul>
     * </p>
     * @param fromVar fully qualified name of the variable to rename.
     * @param toVar   fully qualified name of the variable that will receive <code>fromVar</code>'s value.
     */
    public void renameVariable(String fromVar, String toVar) {setVariable(toVar, removeVariable(fromVar));}

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a way
     * of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal to the
     * new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed to all
     * listeners.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value, <code>false</code> otherwise.
     */
    public synchronized boolean setVariable(String name, String value) {
        ConfigurationExplorer explorer; // Used to navigate to the variable's parent section.
        String                buffer;   // Buffer for the variable's name trimmed of section information.

        // Moves to the parent section.
        buffer = moveToParent(explorer = new ConfigurationExplorer(root), name, true);

        // If the variable's value was actually modified, triggers an event.
        if(explorer.getSection().setVariable(buffer, value)) {
            triggerEvent(new ConfigurationEvent(name, value));
            return true;
        }
        return false;
    }

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a way
     * of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal to the
     * new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed to all
     * listeners.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value, <code>false</code> otherwise.
     */
    public boolean setVariable(String name, int value) {return setVariable(name, ConfigurationSection.getValue(value));}

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a way
     * of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal to the
     * new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed to all
     * listeners.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value, <code>false</code> otherwise.
     */
    public boolean setVariable(String name, float value) {return setVariable(name, ConfigurationSection.getValue(value));}

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a way
     * of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal to the
     * new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed to all
     * listeners.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value, <code>false</code> otherwise.
     */
    public boolean setVariable(String name, boolean value) {return setVariable(name, ConfigurationSection.getValue(value));}

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a way
     * of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal to the
     * new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed to all
     * listeners.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value, <code>false</code> otherwise.
     */
    public boolean setVariable(String name, long value) {return setVariable(name, ConfigurationSection.getValue(value));}

    /**
     * Sets the value of the specified variable.
     * <p>
     * This method will return <code>false</code> if it didn't modify <code>name</code>'s value. This, however, is not a way
     * of indicating that the call failed: <code>false</code> is only ever returned if the previous value is equal to the
     * new value.
     * </p>
     * <p>
     * If the value of the specified variable is actually modified, an {@link ConfigurationEvent event} will be passed to all
     * listeners.
     * </p>
     * @param  name  fully qualified name of the variable to set.
     * @param  value new value for the variable.
     * @return       <code>true</code> if this call resulted in a modification of the variable's value, <code>false</code> otherwise.
     */
    public boolean setVariable(String name, double value) {return setVariable(name, ConfigurationSection.getValue(value));}



    // - Variable retrieval ----------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the value of the specified variable.
     * @param  name fully qualified name of the variable whose value should be retrieved.
     * @return      the variable's value if set, <code>null</code> otherwise.
     */
    public synchronized String getVariable(String name) {
        ConfigurationExplorer explorer; // Used to navigate to the variable's parent section.

        // If the variable's 'path' doesn't exist, return null.
        if((name = moveToParent(explorer = new ConfigurationExplorer(root), name, false)) == null)
            return null;
        return explorer.getSection().getVariable(name);
    }

    /**
     * Returns the value of the specified variable as an integer.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to an integer.
     */
    public int getIntegerVariable(String name) {return ConfigurationSection.getIntegerValue(getVariable(name));}

    /**
     * Returns the value of the specified variable as a long.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to a long.
     */
    public long getLongVariable(String name) {return ConfigurationSection.getLongValue(getVariable(name));}

    /**
     * Returns the value of the specified variable as a float.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to a float.
     */
    public float getFloatVariable(String name) {return ConfigurationSection.getFloatValue(getVariable(name));}

    /**
     * Returns the value of the specified variable as a double.
     * @param                        name fully qualified name of the variable whose value should be retrieved.
     * @return                       the variable's value if set, <code>0</code> otherwise.
     * @throws NumberFormatException if the variable's value cannot be cast to a double.
     */
    public double getDoubleVariable(String name) {return ConfigurationSection.getDoubleValue(getVariable(name));}

    /**
     * Returns the value of the specified variable as a boolean.
     * @param  name fully qualified name of the variable whose value should be retrieved.
     * @return the variable's value if set, <code>false</code> otherwise.
     */
    public boolean getBooleanVariable(String name) {return ConfigurationSection.getBooleanValue(getVariable(name));}

    /**
     * Checks whether the specified variable has been set.
     * @param  name fully qualified name of the variable to check for.
     * @return      <code>true</code> if the variable is set, <code>false</code> otherwise.
     */
    public boolean isVariableSet(String name) {return getVariable(name) != null;}



    // - Variable removal ------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>null</code> if it wasn't set.
     */
    public synchronized String removeVariable(String name) {
        ConfigurationExplorer explorer; // Used to navigate to the variable's parent section.
        String                buffer;   // Buffer for the variable's name trimmed of section information.

        // If the variable's 'path' doesn't exist, return null.
        if((buffer = moveToParent(explorer = new ConfigurationExplorer(root), name, false)) == null)
            return null;

        // If the variable was actually set, triggers an event.
        if((buffer = explorer.getSection().removeVariable(buffer)) != null)
            triggerEvent(new ConfigurationEvent(name, null));

        return buffer;
    }

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public int removeIntegerVariable(String name) {return ConfigurationSection.getIntegerValue(removeVariable(name));}

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public long removeLongVariable(String name) {return ConfigurationSection.getLongValue(removeVariable(name));}

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public float removeFloatVariable(String name) {return ConfigurationSection.getFloatValue(removeVariable(name));}

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>0</code> if it wasn't set.
     */
    public double removeDoubleVariable(String name) {return ConfigurationSection.getDoubleValue(removeVariable(name));}

    /**
     * Deletes the specified variable from the configuration.
     * <p>
     * If the variable was set, a configuration {@link ConfigurationEvent event} will be passed to
     * all registered listeners.
     * </p>
     * @param  name name of the variable to remove.
     * @return      the variable's old value, or <code>false</code> if it wasn't set.
     */
    public boolean removeBooleanVariable(String name) {return ConfigurationSection.getBooleanValue(removeVariable(name));}



    // - Advanced variable retrieval -------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Retrieves the value of the specified variable.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered listeners.
     * </p>
     * @param  name         name of the variable to retrieve.
     * @param  defaultValue value to use if <code>name</code> is not set.
     * @return              the specified variable's value.
     */
    public synchronized String getVariable(String name, String defaultValue) {
        ConfigurationExplorer explorer; // Used to navigate to the variable's parent section.
        String                value;    // Buffer for the variable's value.
        String                buffer;   // Buffer for the variable's name trimmed of section information.

        // Navigates to the parent section. We do not have to check for null values here,
        // as the section will be created if it doesn't exist.
        buffer = moveToParent(explorer = new ConfigurationExplorer(root), name, true);

        // If the variable isn't set, set it to defaultValue and triggers an event.
        if((value = explorer.getSection().getVariable(buffer)) == null) {
            explorer.getSection().setVariable(buffer, defaultValue);
            triggerEvent(new ConfigurationEvent(name, defaultValue));
            return defaultValue;
        }
        return value;
    }

    /**
     * Retrieves the value of the specified variable as an integer.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered listeners.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     * @throws NumberFormatException if the variable's value cannot be cast to an integer.
     */
    public int getVariable(String name, int defaultValue) {
        return ConfigurationSection.getIntegerValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }

    /**
     * Retrieves the value of the specified variable as a long.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered listeners.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     * @throws NumberFormatException if the variable's value cannot be cast to a long.
     */
    public long getVariable(String name, long defaultValue) {
        return ConfigurationSection.getLongValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }

    /**
     * Retrieves the value of the specified variable as a float.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered listeners.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     * @throws NumberFormatException if the variable's value cannot be cast to a float.
     */
    public float getVariable(String name, float defaultValue) {
        return ConfigurationSection.getFloatValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }

    /**
     * Retrieves the value of the specified variable as a boolean.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered listeners.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     */
    public boolean getVariable(String name, boolean defaultValue) {
        return ConfigurationSection.getBooleanValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }

    /**
     * Retrieves the value of the specified variable as a double.
     * <p>
     * If the variable isn't set, this method will set it to <code>defaultValue</code> before
     * returning it. If this happens, a configuration {@link ConfigurationEvent event} will
     * be sent to all registered listeners.
     * </p>
     * @param  name                  name of the variable to retrieve.
     * @param  defaultValue          value to use if <code>name</code> is not set.
     * @return                       the specified variable's value.
     * @throws NumberFormatException if the variable's value cannot be cast to a double.
     */
    public double getVariable(String name, double defaultValue) {
        return ConfigurationSection.getDoubleValue(getVariable(name, ConfigurationSection.getValue(defaultValue)));
    }



    // - Helper methods --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Navigates the specified explorer to the parent section of the specified variable.
     * @param  root where to start exploring from.
     * @param  name name of the variable to seek.
     * @param  create whether or not the path to the variable should be created if it doesn't exist.
     * @return the name of the variable trimmed of section information, <code>null</code> if not found.
     */
    private String moveToParent(ConfigurationExplorer root, String name, boolean create) {
        StringTokenizer parser; // Used to parse the variable's path.

        // Goes through each element of the path.
        parser = new StringTokenizer(name, ".");
        while(parser.hasMoreTokens()) {
            // If we've reached the variable's name, return it.
            name = (String)parser.nextToken();
            if(!parser.hasMoreTokens())
                return name;

            // If we've reached a dead-end, return null.
            if(!root.moveTo(name, create))
                return null;
        }
        return name;
    }



    // - Configuration listening -----------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Adds the specified object to the list of registered configuration listeners.
     * @param listener object to register as a configuration listener.
     */
    public static void addConfigurationListener(ConfigurationListener listener) {listeners.put(listener, null);}

    /**
     * Removes the specified object from the list of registered configuration listeners.
     * @param listener object to remove from the list of registered configuration listeners.
     */
    public static void removeConfigurationListener(ConfigurationListener listener) {listeners.remove(listener);}

    /**
     * Passes the specified event to all registered configuration listeners.
     * @param event event to propagate.
     */
    private static void triggerEvent(ConfigurationEvent event) {
        Iterator iterator;

        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ConfigurationListener)iterator.next()).configurationChanged(event);
    }



    // - Loading ---------------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * @author Nicolas Rinaudo
     */
    private class ConfigurationLoader implements ConfigurationBuilder {
        // - Instance variables ------------------------------------------------
        // ---------------------------------------------------------------------
        /** Parents of {@link #currentSection}. */
        private Stack                sections;
        /** Fully qualified names of {@link #currentSection}. */
        private Stack                sectionNames;
        /** Section that we're currently building. */
        private ConfigurationSection currentSection;



        // - Initialisation ----------------------------------------------------
        // ---------------------------------------------------------------------
        /**
         * Creates a new configuration loader.
         * @param root where to create the configuration in.
         */
        public ConfigurationLoader(ConfigurationSection root) {currentSection = root;}



        // - Building ----------------------------------------------------------
        // ---------------------------------------------------------------------
        /**
         * Initialises the configuration bulding.
         */
        public void startConfiguration() {
            sections     = new Stack();
            sectionNames = new Stack();
        }

        /**
         * Ends the configuration building.
         * @throws ConfigurationException if not all opened sections have been closed.
         */
        public void endConfiguration() throws ConfigurationException {
            // Makes sure currentSection is the root section.
            if(!sections.empty())
                throw new ConfigurationException("Not all sections have been closed.");
            sections     = null;
            sectionNames = null;
        }

        /**
         * Creates a new sub-section to the current section.
         * @param name name of the new section.
         */
        public void startSection(String name) throws ConfigurationException {
            ConfigurationSection buffer;

            buffer = currentSection.addSection(name);
            sections.push(currentSection);
            if(sectionNames.empty())
                sectionNames.push(name + '.');
            else
                sectionNames.push(((String)sectionNames.peek()) + name + '.');
            currentSection = buffer;
        }

        /**
         * Ends the current section.
         * @param  name                   name of the section that's being closed.
         * @throws ConfigurationException if we're not closing a legal section.
         */
        public void endSection(String name) throws ConfigurationException {
            ConfigurationSection buffer;

            // Makes sure there is a section to close.
            try {
                buffer = (ConfigurationSection)sections.pop();
                sectionNames.pop();
            }
            catch(EmptyStackException e) {throw new ConfigurationException("Section " + name + " was already closed.");}

            // Makes sure we're closing the right section.
            if(buffer.getSection(name) != currentSection)
                throw new ConfigurationException("Section " + name + " is not the currently opened section.");
            currentSection = buffer;
        }

        /**
         * Adds the specified variable to the current section.
         * @param name  name of the variable.
         * @param value value of the variable.
         */
        public void addVariable(String name, String value) {
            // If the variable's value was modified, trigger an event.
            if(currentSection.setVariable(name, value)) {
                if(sectionNames.empty())
                    triggerEvent(new ConfigurationEvent(name, value));
                else
                    triggerEvent(new ConfigurationEvent(((String)sectionNames.peek()) + name, value));
            }
        }
    }
}