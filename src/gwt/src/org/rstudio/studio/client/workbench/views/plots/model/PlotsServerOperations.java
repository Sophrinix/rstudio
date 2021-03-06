/*
 * PlotsServerOperations.java
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
package org.rstudio.studio.client.workbench.views.plots.model;

import org.rstudio.core.client.files.FileSystemItem;
import org.rstudio.studio.client.server.ServerRequestCallback;
import org.rstudio.studio.client.server.Void;
import com.google.gwt.json.client.JSONObject;


public interface PlotsServerOperations
{
   String getGraphicsUrl(String filename);
   
   void nextPlot(ServerRequestCallback<Void> requestCallback);
   void previousPlot(ServerRequestCallback<Void> requestCallback);
   
   void clearPlots(ServerRequestCallback<Void> requestCallback);
      
   void refreshPlot(ServerRequestCallback<Void> requestCallback);
   
   void setManipulatorValues(JSONObject values,
                             ServerRequestCallback<Void> requestCallback);
   
   void locatorCompleted(Point point, 
                        ServerRequestCallback<Void> requestCallback);

   void exportPlot(FileSystemItem file,
                   int width,
                   int height,
                   ServerRequestCallback<Void> requestCallback);
}
