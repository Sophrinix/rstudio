/*
 * PrimaryWindowFrame.java
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
package org.rstudio.core.client.theme;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.rstudio.core.client.events.WindowStateChangeEvent;
import org.rstudio.core.client.layout.WindowState;
import org.rstudio.core.client.theme.res.ThemeResources;
import org.rstudio.core.client.theme.res.ThemeStyles;
import org.rstudio.core.client.widget.DoubleClickState;

public class PrimaryWindowFrame extends WindowFrame
{
   private static class ClickFlowPanel extends FlowPanel implements
                                                         HasClickHandlers,
                                                         HasMouseDownHandlers
   {
      public HandlerRegistration addClickHandler(ClickHandler handler)
      {
         return addDomHandler(handler, ClickEvent.getType());
      }

      public HandlerRegistration addMouseDownHandler(MouseDownHandler handler)
      {
         return addDomHandler(handler, MouseDownEvent.getType());
      }
   }

   public PrimaryWindowFrame()
   {
   }

   public PrimaryWindowFrame(String title,
                             Widget mainWidget)
   {
      ThemeStyles styles = ThemeResources.INSTANCE.themeStyles();

      ClickFlowPanel panel = new ClickFlowPanel();
      panel.setStylePrimaryName(styles.primaryWindowFrameHeader());

      Label label = new Label(title);
      label.setStylePrimaryName(styles.title());
      panel.addMouseDownHandler(new MouseDownHandler()
      {
         public void onMouseDown(MouseDownEvent event)
         {
            event.preventDefault();
         }
      });
      panel.addClickHandler(new ClickHandler()
      {
         public void onClick(ClickEvent event)
         {
            focus();
            event.preventDefault();
            event.stopPropagation();

            if (doubleClickState_.checkForDoubleClick(event.getNativeEvent()))
               fireEvent(new WindowStateChangeEvent(WindowState.MAXIMIZE));
         }
      });

      subtitle_ = new Label();
      subtitle_.setStylePrimaryName(styles.subtitle());

      panel.add(label);
      panel.add(subtitle_);

      setHeaderWidget(panel);

      setMainWidget(mainWidget);
   }

   public void setSubtitle(String subtitle)
   {
      subtitle_.setText(subtitle);
   }

   private final DoubleClickState doubleClickState_ = new DoubleClickState();
   private Label subtitle_;
}
