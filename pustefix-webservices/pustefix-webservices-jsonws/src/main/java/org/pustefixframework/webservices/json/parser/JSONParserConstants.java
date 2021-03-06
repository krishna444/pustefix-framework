/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/* Generated By:JavaCC: Do not edit this line. JSONParserConstants.java */
package org.pustefixframework.webservices.json.parser;

public interface JSONParserConstants {

  int EOF = 0;
  int LCB = 6;
  int RCB = 7;
  int LSB = 8;
  int RSB = 9;
  int COL = 10;
  int COM = 11;
  int TRUE = 12;
  int FALSE = 13;
  int NULL = 14;
  int QSTR = 15;
  int HEX = 16;
  int NUM = 17;
  int DATE = 18;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\r\"",
    "\"\\n\"",
    "\"\\f\"",
    "\"{\"",
    "\"}\"",
    "\"[\"",
    "\"]\"",
    "\":\"",
    "\",\"",
    "\"true\"",
    "\"false\"",
    "\"null\"",
    "<QSTR>",
    "<HEX>",
    "<NUM>",
    "<DATE>",
  };

}
