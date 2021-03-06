/*
 * ThemedPopupPanel.java
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
package org.rstudio.core.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;

public class ThemedPopupPanel extends DecoratedPopupPanel
{
   interface Resources extends ClientBundle
   {
      @Source("ThemedPopupPanel.css")
      Styles styles();

      ImageResource popupTopLeft();
      @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
      ImageResource popupTopCenter();
      ImageResource popupTopRight();
      @ImageOptions(repeatStyle = RepeatStyle.Vertical)
      ImageResource popupMiddleLeft();
      @ImageOptions(repeatStyle = RepeatStyle.Vertical)
      ImageResource popupMiddleRight();
      ImageResource popupBottomLeft();
      @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
      ImageResource popupBottomCenter();
      ImageResource popupBottomRight();
   }

   interface Styles extends CssResource
   {
      String themedPopupPanel();
   }

   public ThemedPopupPanel()
   {
      super();
      commonInit();
   }

   public ThemedPopupPanel(boolean autoHide)
   {
      super(autoHide);
      commonInit();
   }

   public ThemedPopupPanel(boolean autoHide, boolean modal)
   {
      super(autoHide, modal);
      commonInit();
   }

   private void commonInit()
   {
      addStyleName(RES.styles().themedPopupPanel());
   }

   private static Resources RES = GWT.create(Resources.class);
   public static void ensureStylesInjected()
   {
      RES.styles().ensureInjected();
   }
}
