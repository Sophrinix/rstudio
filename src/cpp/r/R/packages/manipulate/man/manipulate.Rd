\name{manipulate}
\alias{manipulate}
\title{Create an interactive plot}
\description{
The \code{\link{manipulate}} function accepts a plotting expression and a set of controls (e.g. \code{\link{slider}}, \code{\link{picker}}, or \code{\link{checkbox}}) which are used to dynamically change values within the expression. When a value is changed using its corresponding control the expression is automatically re-executed and the plot is redrawn.
}
\usage{
manipulate(expr, ..., controls = list())
}


\arguments{
  \item{expr}{
    Expression to evalulate. The expression should result in the creation of a plot (e.g. \code{plot} or \code{qplot}). Note that the expression need not be a top-level plotting function, it could also be a custom function that creates a plot as part of its implementation. This expression will be re-evaluated with appropriate parameter substitution each time one of the manipulator control values is changed. 
}
  \item{\dots}{
    One or more controls (i.e. \code{\link{slider}}, \code{\link{picker}}, or \code{\link{checkbox}}). Note that controls may also be specified as a pre-set list using the \code{controls} argument.
}
  \item{controls}{
    A named list of manipulator controls. If controls are specified using both variable arguments and the \code{controls} list the variable argument controls will be appended to the \code{controls} list.
}
}

\details{
    Once a set of manipulator controls are attached to a plot they remain attached and can be recalled whenever viewing the plot (a gear button is added to the top-left of the plot to indicate that it has a manipulator).
    
    Note that because of R's partial matching rules for function arguments, controls whose names partially match the \code{expr} argument (e.g. '\code{e}', '\code{ex}', etc.) are not permitted since they will conflict with this argument. If you wish to include a control with a name that conflicts then as a workaround you can include it as a named item within the \code{controls} list.
    
    The \code{expr} parameter is evaluated using \code{\link{withVisible}}. If it's return value is visible then \code{\link{print}} is called. This enables manipulate expressions to behave simillarly to their being executed directly at the console. 

  The \code{\link{manipulatorSetState}} and \code{\link{manipulatorGetState}} functions can be used to associate custom state with a manipulator (for example, to track the values used for previous plot executions). These values are stored in a custom environment which is stored along with the rest of the manipulator context.
}

\author{
JJ Allaire <jj@rstudio.org>
}

\examples{
\donttest{
## create a plot with a manipulator
manipulate(plot(1:x), x = slider(1, 10))

## use more than one slider
manipulate(plot(x:y), 
           x = slider(1, 10),
           y = slider(1, 100))
}
\dontrun{
## use with ggplot w/ picker for log param
require(ggplot2)
manipulate(qplot(mpg$displ, mpg$cty, log = log),
           log = picker("x", "y", "xy"))

## specify controls as a dynamically built list
controls <- list()
controls$x <- slider(1, 10)
controls$y <- slider(1, 100)
manipulate(plot(x:y), controls)
}
}

