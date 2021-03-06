#
# CMakeLists.txt
#
# Copyright (C) 2009-11 by RStudio, Inc.
#
# This program is licensed to you under the terms of version 3 of the
# GNU Affero General Public License. This program is distributed WITHOUT
# ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
# MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
# AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
#
#

project (SESSION)

add_subdirectory(workers)

# include files
file(GLOB_RECURSE SESSION_HEADER_FILES "*.h*")

# source files
set (SESSION_SOURCE_FILES
   SessionClientEvent.cpp
   SessionClientEventQueue.cpp
   SessionClientEventService.cpp
   SessionMain.cpp
   SessionModuleContext.cpp
   SessionOptions.cpp
   SessionPersistentState.cpp
   SessionPostback.cpp
   SessionSourceDatabase.cpp
   SessionUserSettings.cpp
   SessionWorkerContext.cpp
   http/SessionHttpConnectionQueue.cpp
   http/SessionHttpLog.cpp
   modules/SessionAgreement.cpp
   modules/SessionConsole.cpp
   modules/SessionContentUrls.cpp
   modules/SessionData.cpp
   modules/SessionDiff.cpp
   modules/SessionFiles.cpp
   modules/SessionFilesQuotas.cpp
   modules/SessionHelp.cpp
   modules/SessionHistory.cpp
   modules/SessionLimits.cpp
   modules/SessionPackages.cpp
   modules/SessionPlots.cpp
   modules/SessionSource.cpp
   modules/SessionTeX.cpp
   modules/SessionWorkbench.cpp
   modules/SessionWorkspace.cpp
   ${CMAKE_CURRENT_BINARY_DIR}/SessionAddins.cpp
)

# platform specific source files
if(UNIX)
   set(SESSION_SOURCE_FILES ${SESSION_SOURCE_FILES}
      http/SessionPosixHttpConnectionListener.cpp
   )
else()
   set(SESSION_SOURCE_FILES ${SESSION_SOURCE_FILES}
      http/SessionWin32HttpConnectionListener.cpp
   )
endif()

# R files
file(GLOB_RECURSE SESSION_R_FILES "modules/*.R")

# define core include dirs
set(CORE_INCLUDE_DIRS ${CORE_SOURCE_DIR}/include)

# include addins
if(RSTUDIO_ADDINS_PATH)
   # search for addins (then remove special core directory)
   file(GLOB RSTUDIO_ADDINS ${RSTUDIO_ADDINS_PATH}/*)
   list(REMOVE_ITEM RSTUDIO_ADDINS "core")

   # incorporate all addins found
   foreach(RSTUDIO_ADDIN ${RSTUDIO_ADDINS})
      set(SESSION_ADDIN_PATH  ${RSTUDIO_ADDIN}/session)
      if(EXISTS ${SESSION_ADDIN_PATH})
         # glob the hpp, cpp, and R files
         file(GLOB_RECURSE ADDIN_HEADER_FILES "${SESSION_ADDIN_PATH}/*.h*")
         list(APPEND SESSION_HEADER_FILES ${ADDIN_HEADER_FILES})
         file(GLOB_RECURSE ADDIN_SOURCE_FILES "${SESSION_ADDIN_PATH}/*.c*")
         list(APPEND SESSION_SOURCE_FILES ${ADDIN_SOURCE_FILES})
         file(GLOB_RECURSE ADDIN_R_FILES "${SESSION_ADDIN_PATH}/*.R")
         list(APPEND SESSION_R_FILES ${ADDIN_R_FILES})

         # generate an initialize call
         get_filename_component(ADDIN_NAME ${RSTUDIO_ADDIN} NAME)
         set(SESSION_ADDIN_DECLARATIONS
            "${SESSION_ADDIN_DECLARATIONS}namespace ${ADDIN_NAME} { Error initialize(); }\n" )
         set(SESSION_ADDIN_INITIALIZATIONS
            "${SESSION_ADDIN_INITIALIZATIONS}(${ADDIN_NAME}::initialize) ")
      endif()
   endforeach()

   # add to core include dirs if appropriate
   set(CORE_ADDINS_INCLUDE_DIR ${RSTUDIO_ADDINS_PATH}/core/include)
   if(EXISTS ${CORE_ADDINS_INCLUDE_DIR})
      list(APPEND CORE_INCLUDE_DIRS ${CORE_ADDINS_INCLUDE_DIR})
   endif()

endif()

# config file
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/config.h.in
               ${CMAKE_CURRENT_BINARY_DIR}/config.h)

# always configure the addins bootstrap file
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/SessionAddins.cpp.in
               ${CMAKE_CURRENT_BINARY_DIR}/SessionAddins.cpp)

# configure R files into the binary directory
foreach(SESSION_R_FILE ${SESSION_R_FILES})
   get_filename_component(R_FILE_NAME ${SESSION_R_FILE} NAME)
   configure_file(${SESSION_R_FILE}
                  "${CMAKE_CURRENT_BINARY_DIR}/modules/R/${R_FILE_NAME}"
                  COPYONLY)
endforeach()

# set include directories
include_directories(
   include
   ${Boost_INCLUDE_DIRS}
   ${LIBR_INCLUDE_DIRS}
   ${CORE_INCLUDE_DIRS}
   ${R_SOURCE_DIR}/include
   ${CMAKE_CURRENT_BINARY_DIR}
)

# link directories
link_directories(${R_GRAPHICS_HANDLER_SYSTEM_LIBRARY_DIRS})

# define executable
add_executable(rsession ${SESSION_SOURCE_FILES} ${SESSION_HEADER_FILES})

# set link dependencies
target_link_libraries(rsession
   rstudio-core
   rstudio-r
   rstudio-session-workers
   ${SESSION_SYSTEM_LIBRARIES}
)

# make sure rsession can find libR.so when installed on unix
# note this can also be done automatically for all non-system
# dependencies using INSTALL_RPATH_USE_LINK_PATH TRUE however
# we set it explicitly to the R lib dir so there are no
# unexpected additions to the rpath
if(UNIX AND NOT APPLE)
   set_target_properties(rsession PROPERTIES
                         INSTALL_RPATH ${LIBR_HOME}/lib)
endif()

# install binary
install(TARGETS rsession DESTINATION ${RSTUDIO_INSTALL_BIN})

# include resources, R scripts and 64bit binaries if this isn't a session 64bit build
if (NOT RSTUDIO_SESSION_WIN64)
   # Resources
   install(FILES resources/R.css
           DESTINATION ${RSTUDIO_INSTALL_SUPPORTING}/resources)

   # R scripts
   file(GLOB R_MODULE_SRC_FILES "${CMAKE_CURRENT_BINARY_DIR}/modules/R/*.R")
   install(FILES ${R_MODULE_SRC_FILES}
           DESTINATION ${RSTUDIO_INSTALL_SUPPORTING}/R/modules)
           
   # install 64 bit binaries if we are on win64
   if(WIN32)
      if(NOT ("$ENV{PROGRAMW6432}" STREQUAL ""))
         install(DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/x64"
                 USE_SOURCE_PERMISSIONS
                 DESTINATION  ${RSTUDIO_INSTALL_BIN})
      endif()
   endif()
endif()

# add gpl for desktop
if(RSTUDIO_DESKTOP)
   install(FILES resources/agpl-3.0-standalone.html
           DESTINATION ${RSTUDIO_INSTALL_SUPPORTING}/resources)
endif()

 # install 64-bit gcc runtime for session win64
if(RSTUDIO_SESSION_WIN64)
   get_filename_component(GCC_PATH ${CMAKE_C_COMPILER} PATH CACHE)
   install(PROGRAMS ${GCC_PATH}/libgcc_s_sjlj-1.dll
           DESTINATION ${RSTUDIO_INSTALL_BIN})
endif()


