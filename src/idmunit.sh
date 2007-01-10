#!/bin/ksh

# Script Name: idmunit.sh
# Version:     2.0

# This script will execute the command-line IdMUnit instance
# Note that the tests configured in ./build.xml will be executed 
# and an html report will be generated
#
# This script will return a non-zero exit code and error message if
# it fails to find java or execute ant.  This only checks if ant ran
# and executed properly.  It does not report on errors that ant
# encounters when performing the build.
#
# History:
#    - Version 2.0 created by Chad Dodd.
#

####################################################################
#
# User defined variables
#
export JAVA_HOME=/opt/oblix/opt/sdk1.5.0_09/jdk/jre
export IDMUNITHOME=/opt/oblix/software/IdMUnit

export BUILDDIR=$IDMUNITHOME/src
export LOGFILE=$BUILDDIR/idmunit.out
export ANT=$IDMUNITHOME/util/ant/bin/ant

# Set RMLOG to 1 if you want to delete LOGFILE when this script
# finishes.  Otherwise, the output from this script will be
# concatenated to the end of LOGFILE.
export RMLOG=0

# No configuration required below this point.
####################################################################

cleanup () {
  if [ -f $LOGFILE -a "$RMLOG" -gt 0 ]; then
    rm $LOGFILE
  fi
}

####################################################################
#
# Run Ant build command
#
cd $BUILDDIR
$ANT > $LOGFILE 2>&1

####################################################################
#
# check for errors via exit code
#
if [ $? != 0 ]; then
  echo "FAILURE: Unable to run ant to build IdMUnit."
  echo
  cat $LOGFILE
  cleanup
  exit 1
fi

####################################################################
#
# Verify build was successful
#
foundit=`grep "^BUILD SUCCESSFUL$" $LOGFILE`
if [ "$foundit" == "" ]; then
  echo "FAILURE: IdMUnit build not successful."
  echo
  cat $LOGFILE
  cleanup
  exit 1
fi

cleanup
exit 0

