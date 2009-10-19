Post-installation on Windows 2000/XP:
====================================

  [1] Set the JSAGA_HOME environment variable (optional):
      Open up the system properties (WinKey + Pause), select the tab
      "Advanced", and the "Environment Variables" button, then click on
      "New" to set the JSAGA_HOME variable with the installation path:
          $INSTALL_PATH

  [2] Update the PATH environment variable (optional):
      Open up the system properties (WinKey + Pause), select the tab
      "Advanced", and the "Environment Variables" button, then click on
      "Modify" to add ";%JSAGA_HOME%\bin" at the end of variable PATH.


Post-installation on Unix-based O.S. (Linux, Solaris and Mac OS X):
==================================================================

  [1] Set the JSAGA_HOME environment variable (optional):
      export JSAGA_HOME=$INSTALL_PATH

  [2] Update the PATH environment variable (optional):
      export PATH=$PATH:$JSAGA_HOME/bin
