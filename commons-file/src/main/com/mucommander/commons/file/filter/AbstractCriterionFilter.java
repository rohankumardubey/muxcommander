/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;

import java.util.Vector;

/**
 * <code>AbstractCriterionFilter</code> implements the bulk of the {@link CriterionFilter} interface, matching
 * files based on the criteria values generated by a given {@link CriterionValueGenerator}. The only method left for
 * subclasses to implement is {@link #accept(Object)}.
 *
 * @author Maxence Bernard
 */
public abstract class AbstractCriterionFilter<C> extends AbstractFileFilter implements CriterionFilter<C> {

    private CriterionValueGenerator<C> generator;

    /**
     * Creates a new <code>AbstractCriterionFilter</code> using the specified {@link CriterionValueGenerator} and operating
     * in non-inverted mode.
     *
     * @param generator generates criterion values for files as requested
     */
    public AbstractCriterionFilter(CriterionValueGenerator<C> generator) {
        this(generator, false);
    }

    /**
     * Creates a new <code>AbstractCriterionFilter</code> using the specified {@link CriterionValueGenerator} and operating
     * in the specified mode.
     *
     * @param generator generates criterion values for files as requested
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public AbstractCriterionFilter(CriterionValueGenerator<C> generator, boolean inverted) {
        super(inverted);

        this.generator = generator;
    }

    /**
     * Returns <code>true</code> if this filter matched the given value, according to the current {@link #isInverted()}
     * mode:
     * <ul>
     *  <li>if this filter currently operates in normal (non-inverted) mode, this method will return the value of
     * {@link #accept(Object)}</li>
     *  <li>if this filter currently operates in inverted mode, this method will return the value of
     * {@link #reject(Object)}</li>
     * </ul>
     *
     * @param value the value to test
     * @return true if this filter matched the given value, according to the current inverted mode
     */
    public boolean match(C value) {
        if(inverted)
            return reject(value);

        return accept(value);
    }

    /**
     * Returns <code>true</code> if the given value was rejected by this filter, <code>false</code> if it was accepted.
     *
     * <p>The {@link #isInverted() inverted} mode has no effect on the values returned by this method.</p>
     *
     * @param value the value to be tested
     * @return true if the given value was rejected by this filter
     */
    public boolean reject(C value) {
        return !accept(value);
    }

    /**
     * Convenience method that filters out files that do not {@link #match(AbstractFile) match} this filter and
     * returns a file array of matched <code>AbstractFile</code> instances.
     *
     * @param values values to be tested
     * @return an array of accepted AbstractFile instances
     */
    public C[] filter(C values[]) {
        Vector<C> filteredValuesV = new Vector<C>();
        int nbvalues = values.length;
        C value;
        for(int i=0; i<nbvalues; i++) {
            value = values[i];
            if(accept(value))
                filteredValuesV.add(value);
        }

        C filteredValues[] = (C[]) new Object[filteredValuesV.size()];
        filteredValuesV.toArray(filteredValues);
        return filteredValues;
    }

    /**
     * Convenience method that returns <code>true</code> if all the values in the specified array were matched by
     * {@link #match(Object)}, <code>false</code> if one of the values wasn't.
     *
     * @param values the values to be tested
     * @return true if all the values in the specified array were accepted
     */
    public boolean match(C values[]) {
        int nbFiles = values.length;
        for(int i=0; i<nbFiles; i++)
            if(!match(values[i]))
                return false;

        return true;
    }

    /**
     * Convenience method that returns <code>true</code> if all the values in the specified array were accepted by
     * {@link #accept(Object)}, <code>false</code> if one of the values wasn't.
     *
     * @param values the values to be tested
     * @return true if all the values in the specified array were accepted
     */
    public boolean accept(C values[]) {
        int nbFiles = values.length;
        for(int i=0; i<nbFiles; i++)
            if(!accept(values[i]))
                return false;

        return true;
    }

    /**
     * Convenience method that returns <code>true</code> if all the values in the specified array were rejected by
     * {@link #reject(Object)}, <code>false</code> if one of the values wasn't.
     *
     * @param values the values to be tested
     * @return true if all the values in the specified array were rejected
     */
    public boolean reject(C values[]) {
        int nbFiles = values.length;
        for(int i=0; i<nbFiles; i++)
            if(!reject(values[i]))
                return false;

        return true;
    }


    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    public boolean accept(AbstractFile file) {
        return accept(generator.getCriterionValue(file));
    }
}
