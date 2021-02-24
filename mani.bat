@echo off
IF %1.==. GOTO NO_PARAMS
groovy mani.groovy %1
// unzip -p %1 META-INF/MANIFEST.MF
goto END

:NO_PARAMS
echo No parameter provided

:END
