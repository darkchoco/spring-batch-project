CURRENT_DATE=$(date '+%Y/%m/%d')
CURRENT_TIME=$(date '+%H%M%S')
LESSON=$(basename "$PWD")
#mvn clean package -Dmaven.test.skip=true;
java -jar ./target/sbp-04-01-0.0.1-SNAPSHOT.jar "run.date(date)=$CURRENT_DATE" "run.time=$CURRENT_TIME" "lesson=$LESSON";
