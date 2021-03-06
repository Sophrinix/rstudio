/*
 * Workbench.java
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
package org.rstudio.studio.client.workbench;

import com.google.inject.Inject;
import org.rstudio.core.client.StringUtil;
import org.rstudio.core.client.TimeBufferedCommand;
import org.rstudio.core.client.widget.Operation;
import org.rstudio.studio.client.application.events.EventBus;
import org.rstudio.studio.client.common.GlobalDisplay;
import org.rstudio.studio.client.common.GlobalDisplay.NewWindowOptions;
import org.rstudio.studio.client.server.Server;
import org.rstudio.studio.client.server.VoidServerRequestCallback;
import org.rstudio.studio.client.workbench.events.*;
import org.rstudio.studio.client.workbench.model.*;

public class Workbench implements BusyHandler,
                                  ShowErrorMessageHandler,
                                  BrowseUrlHandler,
                                  QuotaStatusHandler,
                                  OAuthApprovalHandler,
                                  WorkbenchLoadedHandler,
                                  WorkbenchMetricsChangedHandler
{
   @Inject
   public Workbench(WorkbenchMainView view, 
                    GlobalDisplay globalDisplay,
                    EventBus eventBus,
                    Server server,
                    RemoteFileSystemContext fsContext)
   {
      view_ = view;
      globalDisplay_ = globalDisplay;
      server_ = server;
     
      // edit
      eventBus.addHandler(BusyEvent.TYPE, this);
      eventBus.addHandler(ShowErrorMessageEvent.TYPE, this);
      eventBus.addHandler(BrowseUrlEvent.TYPE, this);
      eventBus.addHandler(QuotaStatusEvent.TYPE, this);
      eventBus.addHandler(OAuthApprovalEvent.TYPE, this);
      eventBus.addHandler(WorkbenchLoadedEvent.TYPE, this);
      eventBus.addHandler(WorkbenchMetricsChangedEvent.TYPE, this);

      // We don't want to send setWorkbenchMetrics more than once per 1/2-second
      metricsChangedCommand_ = new TimeBufferedCommand(-1, -1, 500)
      {
         @Override
         protected void performAction(boolean shouldSchedulePassive)
         {
            assert !shouldSchedulePassive;
            
            server_.setWorkbenchMetrics(lastWorkbenchMetrics_,
                                        new VoidServerRequestCallback());
         }
      };
   }

   public WorkbenchMainView getMainView()
   {
      return view_ ;
   }

   public void onWorkbenchLoaded(WorkbenchLoadedEvent event)
   {
      server_.onWorkbenchReady();
   }
   
   public void onBusy(BusyEvent event)
   {  
   }

   public void onShowErrorMessage(ShowErrorMessageEvent event)
   {
      ErrorMessage errorMessage = event.getErrorMessage();
      globalDisplay_.showErrorMessage(errorMessage.getTitle(), 
                                      errorMessage.getMessage());
     
   }
   
   public void onBrowseUrl(BrowseUrlEvent event)
   {
      BrowseUrlInfo urlInfo = event.getUrlInfo();
      NewWindowOptions newWindowOptions = new NewWindowOptions();
      newWindowOptions.setName(urlInfo.getWindow());
      newWindowOptions.setAlwaysUseBrowser(true);
      globalDisplay_.openWindow(urlInfo.getUrl(), newWindowOptions);
   }
     
   public void onWorkbenchMetricsChanged(WorkbenchMetricsChangedEvent event)
   {
      lastWorkbenchMetrics_ = event.getWorkbenchMetrics();
      metricsChangedCommand_.nudge();
   }
   
   public void onQuotaStatus(QuotaStatusEvent event)
   {
      QuotaStatus quotaStatus = event.getQuotaStatus();
      
      // always show warning if the user is over quota
      if (quotaStatus.isOverQuota())
      {
         long over = quotaStatus.getUsed() - quotaStatus.getQuota();
         StringBuilder msg = new StringBuilder();
         msg.append("You are ");
         msg.append(StringUtil.formatFileSize(over));
         msg.append(" over your ");
         msg.append(StringUtil.formatFileSize(quotaStatus.getQuota()));
         msg.append(" file storage limit. Please remove files to ");
         msg.append("continue working.");
         globalDisplay_.showWarningBar(false, msg.toString());
      }
      
      // show a warning if the user is near their quota (but no more
      // than one time per instantiation of the application)
      else if (quotaStatus.isNearQuota() && !nearQuotaWarningShown_)
      {
         StringBuilder msg = new StringBuilder();
         msg.append("You are nearly over your ");
         msg.append(StringUtil.formatFileSize(quotaStatus.getQuota()));
         msg.append(" file storage limit.");
         globalDisplay_.showWarningBar(false, msg.toString());
         
         nearQuotaWarningShown_ = true;
      }
   }

   
   public void onOAuthApproval(OAuthApprovalEvent event)
   {
      final OAuthApproval approval = event.getApproval();
      
      String caption = "Approval Required";
      String message = 
         "In order to connect to " + approval.getRealm() + " you first " +
         "need to grant access to RStudio. Do you want to do this now? " +
         "(you'll be navigated to a form on the " + approval.getRealm() + 
         " website for confirmation)";
      
      globalDisplay_.showYesNoMessage(
            GlobalDisplay.MSG_QUESTION, 
            caption, 
            message, 
            new Operation()
            {
               public void execute()
               {
                  globalDisplay_.openMinimalWindow(approval.getLocation(),
                                                   true,
                                                   700,
                                                   550);
               }   
            }, 
            true);
      
   }
   
   private final Server server_;
   private final WorkbenchMainView view_;
   GlobalDisplay globalDisplay_;

   private final TimeBufferedCommand metricsChangedCommand_;
   private WorkbenchMetrics lastWorkbenchMetrics_;
   private boolean nearQuotaWarningShown_ = false; 
}
