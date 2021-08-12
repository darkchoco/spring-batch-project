CURRENT_DATE=$(date '+%Y/%m/%d')
LESSON=$(basename "$PWD")
java -jar ./target/sbp-02-01-0.0.1-SNAPSHOT.jar "item=shoes" "run.date(date)=$CURRENT_DATE" "lesson=$LESSON"
#echo "$CURRENT_DATE" "$LESSON"
