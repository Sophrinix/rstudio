/*
 * EditSession.java
 *
 * Copyright (C) 2009-11 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.workbench.views.source.editors.text.ace;

import com.google.gwt.core.client.JavaScriptObject;

public class EditSession extends JavaScriptObject
{
   protected EditSession() {}

   public native final String getValue() /*-{
      return this.toString();
   }-*/;

   public native final void setValue(String code) /*-{
      this.setValue(code);
   }-*/;

   public native final void insert(Position position, String text) /*-{
      this.insert(position, text);
   }-*/;

   public native final Selection getSelection() /*-{
      return this.getSelection();
   }-*/;

   public native final Position replace(Range range, String text) /*-{
      return this.replace(range, text);
   }-*/;

   public native final String getTextRange(Range range) /*-{
      return this.getTextRange(range);
   }-*/;

   public native final String getLine(int row) /*-{
      return this.getLine(row);
   }-*/;

   public native final void setUseWrapMode(boolean useWrapMode) /*-{
      return this.setUseWrapMode(useWrapMode);
   }-*/;

   /**
    * Number of rows
    */
   public native final int getLength() /*-{
      return this.getLength();
   }-*/;

   public native final void setEditorMode(String parserName,
                                          boolean suppressHighlighting) /*-{
      var Mode = $wnd.require(parserName).Mode;
      this.setMode(new Mode(suppressHighlighting));
   }-*/;
}
