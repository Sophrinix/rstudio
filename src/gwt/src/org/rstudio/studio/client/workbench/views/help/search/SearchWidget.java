/*
 * SearchWidget.java
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
package org.rstudio.studio.client.workbench.views.help.search;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.rstudio.core.client.events.HasSelectionCommitHandlers;
import org.rstudio.core.client.events.SelectionCommitEvent;
import org.rstudio.core.client.events.SelectionCommitHandler;
import org.rstudio.core.client.theme.res.ThemeResources;
import org.rstudio.core.client.theme.res.ThemeStyles;
import org.rstudio.core.client.widget.CanFocus;
import org.rstudio.studio.client.workbench.views.help.search.Search.Display;

public class SearchWidget extends Composite
                          implements Display,
                                     HasSelectionCommitHandlers<String>,
                                     HasKeyDownHandlers,
                                     HasFocusHandlers,
                                     HasBlurHandlers,
                                     CanFocus
{
   interface MyUiBinder extends UiBinder<Widget, SearchWidget> {}
   private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

   class FocusSuggestBox extends SuggestBox implements HasFocusHandlers,
                                                       HasBlurHandlers
   {
      FocusSuggestBox(SuggestOracle oracle)
      {
         super(oracle);
      }

      public HandlerRegistration addBlurHandler(BlurHandler handler)
      {
         return addDomHandler(handler, BlurEvent.getType());
      }

      public HandlerRegistration addFocusHandler(FocusHandler handler)
      {
         return addDomHandler(handler, FocusEvent.getType());
      }
   }

   @Inject
   public SearchWidget(@Named(value = "Search") SuggestOracle oracle)
   {
      suggestBox_ = new FocusSuggestBox(oracle);
      initWidget(uiBinder.createAndBindUi(this));

      ThemeStyles styles = ThemeResources.INSTANCE.themeStyles();

      suggestBox_.setStylePrimaryName(styles.searchBox());
      suggestBox_.setAutoSelectEnabled(false) ;
      suggestBox_.addKeyDownHandler(new KeyDownHandler() {
         public void onKeyDown(KeyDownEvent event)
         {
            switch (event.getNativeKeyCode())
            {
            case KeyCodes.KEY_ENTER:
               DeferredCommand.addCommand(new Command() {
                  public void execute()
                  {
                     SelectionCommitEvent.fire(SearchWidget.this, 
                                               suggestBox_.getText()) ;
                  }
               }) ;
               break ;
            case KeyCodes.KEY_ESCAPE:
               suggestBox_.setText("") ;
               break ;
            }
         }
      }) ;

      // Unlike SuggestBox's ValueChangeEvent impl, we want the
      // event to fire as soon as the value changes
      suggestBox_.addKeyUpHandler(new KeyUpHandler() {
         public void onKeyUp(KeyUpEvent event)
         {
            String value = suggestBox_.getText();
            if (!value.equals(lastValueSent_))
            {
               lastValueSent_ = value;
               ValueChangeEvent.fire(SearchWidget.this, value);
            }
         }
      });
      suggestBox_.addValueChangeHandler(new ValueChangeHandler<String>()
      {
         public void onValueChange(ValueChangeEvent<String> evt)
         {
            if (!evt.getValue().equals(lastValueSent_))
            {
               lastValueSent_ = evt.getValue();
               delegateEvent(SearchWidget.this, evt);
            }
         }
      });

      close_.addMouseDownHandler(new MouseDownHandler()
      {
         public void onMouseDown(MouseDownEvent event)
         {
            event.preventDefault();
            
            suggestBox_.setText("");
            ValueChangeEvent.fire(suggestBox_, "");
         }
      });
   }
   
   public HandlerRegistration addFocusHandler(FocusHandler handler)
   {
      return suggestBox_.addFocusHandler(handler);
   }

   public HandlerRegistration addBlurHandler(BlurHandler handler)
   {
      return suggestBox_.addBlurHandler(handler);
   }

   public HandlerRegistration addValueChangeHandler(
                                           ValueChangeHandler<String> handler)
   {
      return addHandler(handler, ValueChangeEvent.getType());
   }

   public HandlerRegistration addSelectionCommitHandler(
                                       SelectionCommitHandler<String> handler)
   {
      return addHandler(handler, SelectionCommitEvent.getType()) ;
   }

   public HandlerRegistration addKeyDownHandler(final KeyDownHandler handler)
   {
      return suggestBox_.addKeyDownHandler(new KeyDownHandler()
      {
         public void onKeyDown(KeyDownEvent event)
         {
            if (ignore_ = !ignore_)
               handler.onKeyDown(event);
         }

         private boolean ignore_ = false;
      });
   }

   public String getText()
   {
      return suggestBox_.getText() ;
   }

   public void setText(String text)
   {
      suggestBox_.setText(text) ;
   }

   public void setText(String text, boolean fireEvents)
   {
      suggestBox_.setValue(text, fireEvents);
   }
   
   public void focus()
   {
      suggestBox_.setFocus(true);      
   }

   @UiField(provided=true)
   FocusSuggestBox suggestBox_;
   @UiField
   Image close_;

   private String lastValueSent_ = null;
  
}
