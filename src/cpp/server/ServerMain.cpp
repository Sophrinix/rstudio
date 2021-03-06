/*
 * ServerMain.cpp
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

#include <core/Error.hpp>
#include <core/ProgramStatus.hpp>

#include <core/text/TemplateFilter.hpp>

#include <core/system/System.hpp>

#include <core/http/URL.hpp>
#include <core/http/AsyncUriHandler.hpp>
#include <core/http/TcpIpAsyncServer.hpp>

#include <core/gwt/GwtLogHandler.hpp>
#include <core/gwt/GwtFileHandler.hpp>

#include <session/SessionConstants.hpp>

#include <server/util/system/System.hpp>
#include <server/util/system/Crypto.hpp>

#include <server/auth/ServerAuthHandler.hpp>
#include <server/auth/ServerValidateUser.hpp>
#include <server/auth/ServerSecureCookie.hpp>
#include <server/auth/ServerSecureUriHandler.hpp>

#include <server/ServerOptions.hpp>
#include <server/ServerUriHandlers.hpp>

#include "ServerAddins.hpp"
#include "ServerBrowser.hpp"
#include "ServerOffline.hpp"
#include "ServerPAMAuth.hpp"
#include "ServerSessionProxy.hpp"

using namespace core ;
using namespace server;

namespace {
   
bool mainPageFilter(const core::http::Request& request,
                    core::http::Response* pResponse)
{
   return server::browser::supportedBrowserFilter(request, pResponse) &&
          auth::handler::mainPageFilter(request, pResponse);
}


http::UriHandlerFunction blockingFileHandler()
{
   Options& options = server::options();
   return gwt::fileHandlerFunction(options.wwwLocalPath(),
                                   "/",
                                   mainPageFilter);
}

//
// some fancy footwork is required to take the standand blocking file handler
// and make it work within a secure async context.
//
auth::SecureAsyncUriHandlerFunction secureAsyncFileHandler()
{
   // create a functor which can adapt a synchronous file handler into
   // an asynchronous handler
   class FileRequestHandler {
   public:
      static void handleRequest(
            const http::UriHandlerFunction& fileHandlerFunction,
            boost::shared_ptr<http::AsyncConnection> pConnection)
      {
         fileHandlerFunction(pConnection->request(), &(pConnection->response()));
         pConnection->writeResponse();
      }
   };

   // use this functor to generate an async uri handler function from the
   // stock blockingFileHandler (defined above)
   http::AsyncUriHandlerFunction asyncFileHandler =
      boost::bind(FileRequestHandler::handleRequest, blockingFileHandler(), _1);


   // finally, adapt this to be a secure async uri handler by binding out the
   // first parameter (username, which the gwt file handler knows nothing of)
   return boost::bind(asyncFileHandler, _2);
}

// http server
boost::scoped_ptr<http::TcpIpAsyncServer> s_pHttpServer;

Error httpServerInit()
{
   // create http server
   s_pHttpServer.reset(new http::TcpIpAsyncServer("RStudio"));

   // set server options
   s_pHttpServer->setAbortOnResourceError(true);

   // initialize the http server
   Options& options = server::options();
   return s_pHttpServer->init(options.wwwAddress(), options.wwwPort());
}

void httpServerAddHandlers()
{
   // establish json-rpc handlers
   using namespace server::auth;
   using namespace server::session_proxy;
   uri_handlers::add("/rpc", secureAsyncJsonRpcHandler(proxyRpcRequest));
   uri_handlers::add("/events", secureAsyncJsonRpcHandler(proxyEventsRequest));

   // establish content handlers
   uri_handlers::add("/graphics", secureAsyncHttpHandler(proxyContentRequest));
   uri_handlers::add("/upload", secureAsyncUploadHandler(proxyContentRequest));
   uri_handlers::add("/export", secureAsyncHttpHandler(proxyContentRequest));
   uri_handlers::add("/source", secureAsyncHttpHandler(proxyContentRequest));
   uri_handlers::add("/content", secureAsyncHttpHandler(proxyContentRequest));
   uri_handlers::add("/diff", secureAsyncHttpHandler(proxyContentRequest));
   uri_handlers::add("/file_show", secureAsyncHttpHandler(proxyContentRequest));
   uri_handlers::add("/agreement", secureAsyncHttpHandler(proxyContentRequest));

   // content handlers which might be accessed outside the context of the
   // workbench get secure + authentication when required
   uri_handlers::add("/help", secureAsyncHttpHandler(proxyContentRequest, true));
   uri_handlers::add("/files", secureAsyncHttpHandler(proxyContentRequest, true));
   uri_handlers::add("/custom", secureAsyncHttpHandler(proxyContentRequest, true));
   uri_handlers::add("/docs", secureAsyncHttpHandler(secureAsyncFileHandler(), true));

   // establish logging handler
   uri_handlers::addBlocking("/log", secureJsonRpcHandler(gwt::handleLogRequest));

   // establish progress handler
   FilePath wwwLocalPath(server::options().wwwLocalPath());
   FilePath progressPagePath = wwwLocalPath.complete("progress.htm");
   uri_handlers::addBlocking("/progress",
                               secureHttpHandler(boost::bind(
                               core::text::handleSecureTemplateRequest,
                               _1, progressPagePath, _2, _3)));

   // establish browser unsupported handler
   using namespace server::browser;
   uri_handlers::addBlocking(kBrowserUnsupported,
                             handleBrowserUnsupportedRequest);

   // restrct access to templates directory
   uri_handlers::addBlocking("/templates", http::notFoundHandler);

   // add default handler for gwt app
   uri_handlers::setBlockingDefault(blockingFileHandler());
}

} // anonymous namespace

// provide global access to handlers
namespace server {
namespace uri_handlers {

void add(const std::string& prefix,
         const http::AsyncUriHandlerFunction& handler)
{
   s_pHttpServer->addHandler(prefix, handler);
}

void addBlocking(const std::string& prefix,
                 const http::UriHandlerFunction& handler)
{
   s_pHttpServer->addBlockingHandler(prefix, handler);
}

void setDefault(const http::AsyncUriHandlerFunction& handler)
{
   s_pHttpServer->setDefaultHandler(handler);
}

// set blocking default handler
void setBlockingDefault(const http::UriHandlerFunction& handler)
{
  s_pHttpServer->setBlockingDefaultHandler(handler);
}


} // namespace uri_handlers
} // namespace server


int main(int argc, char * const argv[]) 
{
   try
   {
      // initialize log
      initializeSystemLog("rserver", core::system::kLogLevelWarning);

      // read program options 
      Options& options = server::options();
      ProgramStatus status = options.read(argc, argv); 
      if ( status.exit() )
         return status.exitCode() ;
      
      // daemonize if requested
      if (options.serverDaemonize())
      {
         Error error = util::system::daemonize();
         if (error)
            return core::system::exitFailure(error, ERROR_LOCATION);

         error = core::system::ignoreTerminalSignals();
         if (error)
            return core::system::exitFailure(error, ERROR_LOCATION);

         // set file creation mask to 022 (might have inherted 0 from init)
         setUMask(util::system::OthersNoWriteMask);
      }

      // increase the number of open files allowed (need more files
      // so we can supports lots of concurrent connectins)
      if (util::system::realUserIsRoot())
      {
         Error error = setResourceLimit(util::system::FilesLimit, 4096);
         if (error)
            return core::system::exitFailure(error, ERROR_LOCATION);
      }

      // automatically reap children
      Error error = core::system::reapChildren();
      if (error)
         return core::system::exitFailure(error, ERROR_LOCATION);

      // set working directory
      error = FilePath(options.serverWorkingDir()).makeCurrentPath();
      if (error)
         return core::system::exitFailure(error, ERROR_LOCATION);

      // initialize crypto utils
      util::system::crypto::initialize();

      // initialize secure cookie module
      error = auth::secure_cookie::initialize();
      if (error)
         return core::system::exitFailure(error, ERROR_LOCATION);

      // initialize the session proxy
      error = session_proxy::initialize();
      if (error)
         return core::system::exitFailure(error, ERROR_LOCATION);

      // initialize http server
      error = httpServerInit();
      if (error)
         return core::system::exitFailure(error, ERROR_LOCATION);

      // add handlers and initiliaze addins (offline has distinct behavior)
      if (server::options().serverOffline())
      {
         offline::httpServerAddHandlers();
      }
      else
      {
         // add handlers
         httpServerAddHandlers();

         // initialize addins
         error = addins::initialize();
         if (error)
            return core::system::exitFailure(error, ERROR_LOCATION);

         // initialize pam auth if we don't already have an auth handler
         if (!auth::handler::isRegistered())
         {
            error = pam_auth::initialize();
            if (error)
               return core::system::exitFailure(error, ERROR_LOCATION);
         }
      }

      // give up root privilige if requested
      std::string runAsUser = options.serverUser();
      if (!runAsUser.empty())
      {
         // drop root priv
         Error error = util::system::temporarilyDropPriv(runAsUser);
         if (error)
            return core::system::exitFailure(error, ERROR_LOCATION);
      }

      // run http server
      error = s_pHttpServer->run(options.wwwThreadPoolSize());
      if (error)
         return core::system::exitFailure(error, ERROR_LOCATION);

      // wait on termination of the server
      // NOTE: current implementation does not ever terminate
      s_pHttpServer->waitUntilStopped();

      // return success
      return EXIT_SUCCESS;
   }
   CATCH_UNEXPECTED_EXCEPTION
   
   // if we got this far we had an unexpected exception
   return EXIT_FAILURE ;
}


