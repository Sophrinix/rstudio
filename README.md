RStudio
=============================================================================

RStudio is an integrated development environment (IDE) for the 
[R programming language](http://www.r-project.org). Some of its
features include:

- Customizable workbench with all of the tools required to work with R in one
place (console, source, plots, workspace, help, history, etc.).
- Syntax highlighting editor with code completion.
- Execute code directly from the source editor (line, selection, or file).
- Full support for authoring Sweave and TeX documents.
- Runs on all major platforms (Windows, Mac, and Linux) and can also be
run as a server, enabling multiple users to access the RStudio IDE using
a web browser.

For more information on RStudio please visit the 
[project website](http://www.rstudio.org/).

Getting the Code
-----------------------------------------------------------------------------

RStudio is licensed under the AGPLv3, the terms of which are included in
the file COPYING. To retreive the most up to date version of the RStudio
source code you should clone the git repository and then update
all submodules:

    git clone git@github.com:rstudio/rstudio.git
    git submodule update --init --recursive
    
When you want to pull new changes into your local repository you should 
use the pull script which is located in the root of the distribution. This 
will ensure that you get changes from both the main RStudio repository
as well as external submodules:

    ./pull.sh
    

Documentation
-----------------------------------------------------------------------------

For information on how to use RStudio check out our
[online documentation](http://www.rstudio.org/docs/). 

For documentation on running your own RStudio Server see the 
[server getting started](http://www.rstudio.org/docs/server/getting_started)
guide.

See also the following files included with the distribution:

- COPYING - RStudio license (AGPLv3)
- NOTICE  - Additional open source software included with RStudio
- SOURCE  - How to obtain the source code for RStudio
- INSTALL - How to build and install RStudio from source

If you have problems or want to share feedback with us please visit our
[support website](http://support.rstudio.org/). For other inquiries you can
also email us at [info@rstudio.org](mailto:info@rstudio.org). 
