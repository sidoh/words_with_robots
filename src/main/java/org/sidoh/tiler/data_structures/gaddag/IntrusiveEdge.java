package org.sidoh.tiler.data_structures.gaddag;

/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2007, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* -------------------
 * IntrusiveEdge.java
 * -------------------
 * (C) Copyright 2006-2007, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id: IntrusiveEdge.java 568 2007-09-30 00:12:18Z perfecthash $
 *
 * Changes
 * -------
 * 28-May-2006 : Initial revision (JVS);
 *
 */

import java.io.Serializable;


/**
 * IntrusiveEdge encapsulates the internals for the default edge implementation.
 * It is not intended to be referenced directly (which is why it's not public);
 * use DefaultEdge for that.
 *
 * @author John V. Sichi
 */
class IntrusiveEdge
        implements Cloneable,
        Serializable
{
  //~ Static fields/initializers ---------------------------------------------

  private static final long serialVersionUID = 3258408452177932855L;

  //~ Instance fields --------------------------------------------------------

  Object source;

  Object target;

  //~ Methods ----------------------------------------------------------------

  /**
   * @see Object#clone()
   */
  public Object clone()
  {
    throw new RuntimeException("clone not supported on IntrusiveEdge");
  }
}

// End IntrusiveEdge.java
