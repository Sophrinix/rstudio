/*
 * WindowEx.java
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
package org.rstudio.core.client.dom;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import org.rstudio.core.client.Point;

public class WindowEx extends JavaScriptObject
{
   public static native WindowEx get() /*-{
      return $wnd;
   }-*/;

   protected WindowEx()
   {
   }

   public final native void focus() /*-{
      this.focus();
   }-*/;

   public final native void print() /*-{
      this.print() ;
   }-*/;

   public final native void back() /*-{
      this.history.back() ;
   }-*/;
   
   public final native void forward() /*-{
      this.history.forward() ;
   }-*/;
   
   public final native String getLocationHref() /*-{
      return this.location.href ;
   }-*/;

   public final native void setLocationHref(String helpURL) /*-{
      this.location.href = helpURL ;
   }-*/;

   public final native void replaceLocationHref(String helpURL) /*-{
      this.location.replace(helpURL) ;
   }-*/;

   public final Point getScrollPosition()
   {
      JsArrayInteger pos = getScrollPositionInternal();
      return new Point(pos.get(0), pos.get(1));
   }

   public final void setScrollPosition(Point pos)
   {
      setScrollPositionInternal(pos.x, pos.y);
   }

   private final native JsArrayInteger getScrollPositionInternal() /*-{
      return [this.scrollX, this.scrollY];
   }-*/;

   private final native void setScrollPositionInternal(int x, int y) /*-{
      this.scrollTo(x, y);
   }-*/;

   public final native void close() /*-{
      this.close();
   }-*/;

   public final native void resizeTo(int width, int height) /*-{
      this.resizeTo(width, height);
   }-*/;

   public final native void resizeInnerTo(int width, int height) /*-{
      this.innerWidth = width;
      this.innerHeight = height;
   }-*/;

   public final native Document getDocument() /*-{
      return this.document;
   }-*/;


   
   public static HandlerRegistration addFocusHandler(FocusHandler handler)
   {
      return handlers_.addHandler(FocusEvent.getType(), handler);
   }

   public static HandlerRegistration addBlurHandler(BlurHandler handler)
   {
      return handlers_.addHandler(BlurEvent.getType(), handler);
   }

   @SuppressWarnings("unused")
   private static void fireFocusHandlers()
   {
      NativeEvent nativeEvent = Document.get().createFocusEvent();
      FocusEvent.fireNativeEvent(nativeEvent, new HasHandlers()
      {
         public void fireEvent(GwtEvent<?> event)
         {
            handlers_.fireEvent(event);
         }
      });
   }

   @SuppressWarnings("unused")
   private static void fireBlurHandlers()
   {
      NativeEvent nativeEvent = Document.get().createBlurEvent();
      BlurEvent.fireNativeEvent(nativeEvent, new HasHandlers()
      {
         public void fireEvent(GwtEvent<?> event)
         {
            handlers_.fireEvent(event);
         }
      });
   }

   static {
      registerNativeListeners();
   }

   private static native void registerNativeListeners() /*-{
      $wnd.onfocus = function() {
         @org.rstudio.core.client.dom.WindowEx::fireFocusHandlers()();
      };
      $wnd.onblur = function() {
         @org.rstudio.core.client.dom.WindowEx::fireBlurHandlers()();
      };
   }-*/;

   private static final HandlerManager handlers_ = new HandlerManager(null);
}
