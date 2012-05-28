/*
 * Copyright (C) 2011 reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.in2p3.jsaga.adaptor.batchssh.job

import java.io.OutputStream
import sun.text.normalizer.UTF16

class StringBuilderOutputStream(val builder: StringBuilder = new StringBuilder) extends OutputStream {
  override def write(b: Int) = builder.append(UTF16.valueOf(b))
  override def toString = builder.toString
}
