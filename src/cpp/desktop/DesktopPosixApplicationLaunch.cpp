/*
 * DesktopPosixApplicationLaunch.cpp
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

#include "DesktopApplicationLaunch.hpp"

#include "3rdparty/qtsingleapplication/QtSingleApplication"

namespace desktop {

namespace {

QtSingleApplication* app()
{
   return qobject_cast<QtSingleApplication*>(qApp);
}

} // anonymous namespace

ApplicationLaunch::ApplicationLaunch() :
    QWidget(NULL),
    pMainWindow_(NULL)
{
   connect(app(), SIGNAL(messageReceived(QString)),
           this, SIGNAL(openFileRequest(QString)));
}

void ApplicationLaunch::init(QString appName,
                             int& argc,
                             char* argv[],
                             boost::scoped_ptr<QApplication>* ppApp,
                             boost::scoped_ptr<ApplicationLaunch>* ppAppLaunch)
{
   // Immediately stuffed into scoped_ptr
   QtSingleApplication* pSingleApplication = new QtSingleApplication(argc, argv);
   pSingleApplication->setApplicationName(appName);
   ppApp->reset(pSingleApplication);

   ppAppLaunch->reset(new ApplicationLaunch());
}

void ApplicationLaunch::setActivationWindow(QWidget* pWindow)
{
   pMainWindow_ = pWindow;
   app()->setActivationWindow(pWindow, true);
}

bool ApplicationLaunch::sendMessage(QString filename)
{
   return app()->sendMessage(filename);
}

} // namespace desktop
