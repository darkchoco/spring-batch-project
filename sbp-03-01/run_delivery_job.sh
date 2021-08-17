CURRENT_DATE=$(date '+%Y/%m/%d')
CURRENT_TIME=$(date '+%H%M%S')
LESSON=$(basename "$PWD")
java -jar ./target/sbp-03-01-0.0.1-SNAPSHOT.jar "item=shoes" "run.date(date)=$CURRENT_DATE" "run.time=$CURRENT_TIME" "lesson=$LESSON"
#echo "$CURRENT_DATE" "$LESSON"
