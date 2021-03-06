
set PACKAGE_DIR="%CD%"

REM clean if requested
if "%1" == "clean" call clean-build.bat

REM perform 32-bit build 
cd "%PACKAGE_DIR%"
mkdir build
cd build
del CMakeCache.txt
rmdir /s /q build\_CPack_Packages
cmake -G"MinGW Makefiles" -DRSTUDIO_TARGET=Desktop -DCMAKE_BUILD_TYPE=Release ..\..\..
mingw32-make 
cd ..

REM perform 64-bit build and install it into the 32-bit tree
REM (but only do this if we are on win64)
IF "%PROCESSOR_ARCHITECTURE%" == "AMD64" call make-install-win64.bat "%PACKAGE_DIR%\build\src\cpp\session" %1

REM create packages
cd build
cpack -G NSIS
REM cpack -G ZIP
cd ..






