# Build file jar
export $(cat .env | xargs)

#!/bin/sh
SERVICE_NAME=CryptocurrencyWalletApplication # service name
PATH_TO_JAR=target/cryptocurrency_wallet-0.0.1-SNAPSHOT.jar # service jar file
PID_PATH_NAME=/tmp/CryptocurrencyWalletApplication-pid # pid name
case $1 in
    start)
        export $(cat .env | xargs)
        mvn clean install -DskipTests=true
        echo "Starting $SERVICE_NAME ..."
        if [ ! -f $PID_PATH_NAME ]; then
            nohup java -Dspring.profiles.active=dev -jar $PATH_TO_JAR /tmp 2>> /dev/null >> /dev/null & # -Dspring.profiles.active=dev
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is already running ..."
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stoping ..."
            kill $PID;
            echo "$SERVICE_NAME stopped ..."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill $PID;
            echo "$SERVICE_NAME stopped ...";
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME starting ..."
            export $(cat .env | xargs)
            mvn clean install -DskipTests=true
            nohup java  -Dspring.profiles.active=dev -jar $PATH_TO_JAR /tmp 2>> /dev/null >> /dev/null & # -Dspring.profiles.active=dev
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
esac
