#!/bin/sh

# "mvn install" jar files from the IIB Toolkit's SDPShared directory. An adaption of this script could be used to "mvn deploy" the artifacts

FILE_LIST=com.ibm.etools.mft.config_*

function install {
  echo Installing...
  echo file: $1
  echo groupId: $2
  echo artifactId: $3
  echo version: $4

  echo mvn install:install-file -Dfile=$1 -DgroupId=$2 -DartifactId=$3 -Dversion=$4 -Dpackaging=jar
}

for FILE in $FILE_LIST
do
  echo FILE: $FILE

  # BASEFILENAME is either the directory name or the .jar filename withouth ".jar"
  BASEFILENAME=$(echo $FILE | sed 's/.jar$//')

  echo BASEFILENAME: $BASEFILENAME

  FILENAME_NO_VERSION=$(echo $BASEFILENAME | sed 's/_/ /' | cut -f1 -d " ")

  echo FILENAME_NO_VERSION: $FILENAME_NO_VERSION

  VERSION=$(echo $BASEFILENAME | sed 's/^'$FILENAME_NO_VERSION'_//')

  # if the "file" is actually a directory
  if [ -d $FILE ]
  then
    # the directory name is <groupId>_<version>
    GROUPID=$FILENAME_NO_VERSION

    cd $FILE

    for JAR_FILE in *.jar
    do

      ARTIFACTID=$(echo $JAR_FILE | sed 's/.jar$//')

      install "$JAR_FILE" "$GROUPID" "$ARTIFACTID" "$VERSION"

    done

    cd ..

  else
    # it's the jar file - the name is <groupId>.<artifactId>_<version>, where <groupId> may contain .'s
 
    # The artifactId is the last dotted component of FILENAME_NO_VERSION
    ARTIFACTID=${FILENAME_NO_VERSION##*.}

    GROUPID=$(echo $FILENAME_NO_VERSION | sed 's/\.'$ARTIFACTID'$//')

    install "$FILE" "$GROUPID" "$ARTIFACTID" "$VERSION"

  fi


done


