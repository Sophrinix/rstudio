/*
 * BottomScrollPanel.java
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

import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * An implementation of ScrollPanel that defaults its scroll position to the
 * bottom. If resized while the scroll position is at the bottom, the scroll
 * position will stay at the bottom (even if the panel is shrinking in size).
 */
public class BottomScrollPanel extends ScrollPanel
{
   public BottomScrollPanel()
   {
      addScrollHandler(new ScrollHandler()
      {
         public void onScroll(ScrollEvent event)
         {
            scrolledToBottom_ = getScrollPosition() + getOffsetHeight()
                == getElement().getScrollHeight();
         }
      });
   }

   @Override
   protected void onLoad()
   {
      super.onLoad();
      scrollToBottom();
   }

   @Override
   public void onResize()
   {
      if (scrolledToBottom_)
         scrollToBottom();
      super.onResize();
   }

   @Override
   public void scrollToBottom()
   {
      super.scrollToBottom();
      scrolledToBottom_ = true;
   }

   public void onContentSizeChanged()
   {
      if (scrolledToBottom_)
         scrollToBottom();
   }

   private boolean scrolledToBottom_;
}
