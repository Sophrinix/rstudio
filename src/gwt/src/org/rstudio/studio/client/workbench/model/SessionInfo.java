/*
 * SessionInfo.java
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
package org.rstudio.studio.client.workbench.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import org.rstudio.core.client.jsonrpc.RpcObjectList;
import org.rstudio.studio.client.workbench.views.source.model.SourceDocument;

public class SessionInfo extends JavaScriptObject
{
   protected SessionInfo()
   {
   }
   
   public final native String getClientId() /*-{
      return this.clientId;
   }-*/;
   
   public final native double getClientVersion() /*-{
      return this.version;
   }-*/;

   public final native String getUserIdentity() /*-{
      return this.userIdentity;
   }-*/;

   public final native boolean isTexInstalled() /*-{
      return this.tex_installed;
   }-*/;

   public final native String getLogDir() /*-{
      return this.log_dir;
   }-*/;

   public final native String getScratchDir() /*-{
      return this.scratch_dir;
   }-*/;

   public final static String DESKTOP_MODE = "desktop";
   public final static String SERVER_MODE = "server";
   
   public final native String getMode() /*-{
      return this.mode;
   }-*/;

   public final native boolean getResumed() /*-{
      return this.resumed;
   }-*/;
   
   public final native String getDefaultPrompt() /*-{
      return this.prompt;
   }-*/;
   
   public final native JsArrayString getConsoleHistory() /*-{
      return this.console_history;
   }-*/;

   public final native RpcObjectList<ConsoleAction> getConsoleActions() /*-{
      return this.console_actions;
   }-*/;

   public final native int getConsoleActionsLimit() /*-{
      return this.console_actions_limit;
   }-*/;

   public final native ClientInitState getClientState() /*-{
      return this.client_state;
   }-*/;
   
   public final native JsArray<SourceDocument> getSourceDocuments() /*-{
      return this.source_documents;
   }-*/;
   
   public final native boolean hasAgreement() /*-{
      return this.hasAgreement;
   }-*/;
   
   public final native Agreement pendingAgreement() /*-{
      return this.pendingAgreement;
   }-*/;
   
   public final native String docsURL() /*-{
      return this.docsURL;
   }-*/;

   public final native boolean isGoogleDocsIntegrationEnabled() /*-{
      return this.googleDocsIntegrationEnabled;
   }-*/;
   
   public final native String getRstudioVersion() /*-{
      return this.rstudio_version;
   }-*/;
   
}
