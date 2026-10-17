@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------
@IF "%DEBUG%"=="" @ECHO OFF
@SETLOCAL
@SET ERROR_CODE=0

@SET MAVEN_PROJECTBASEDIR=%~dp0
@IF NOT "%MAVEN_PROJECTBASEDIR%"=="" @SET MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

@SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@IF NOT EXIST "%WRAPPER_JAR%" (
    @ECHO Downloading maven-wrapper.jar...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar', '%WRAPPER_JAR%')" >nul 2>&1
)

@IF NOT EXIST "%WRAPPER_JAR%" (
    mvn %*
    @GOTO end
)

@IF NOT "%JAVA_HOME%"=="" @GOTO checkJavaHome
@FOR %%i IN (java.exe) DO @SET JAVACMD=%%~$PATH:i
@IF "%JAVACMD%"=="" (
    @ECHO JAVA_HOME is not set and no 'java' command could be found in your PATH.
    @GOTO error
)
@GOTO run

:checkJavaHome
@SET JAVACMD="%JAVA_HOME%\bin\java.exe"
@IF NOT EXIST %JAVACMD% (
    @ECHO JAVA_HOME "%JAVA_HOME%" does not contain a valid bin\java.exe.
    @GOTO error
)

:run
%JAVACMD% -classpath "%WRAPPER_JAR%" %WRAPPER_LAUNCHER% %*
@IF ERRORLEVEL 1 @GOTO error
@GOTO end

:error
@SET ERROR_CODE=1

:end
@EXIT /B %ERROR_CODE%
