rm ../huaxia-jar/gateway/huaxia-gateway.jar
rm ../huaxia-jar/assistant/huaxia-assistant.jar
rm ../huaxia-jar/term/huaxia-term.jar
rm ../huaxia-jar/translate/huaxia-translate.jar
rm ../huaxia-jar/user/huaxia-user.jar

copy ../../../huaxia-gateway/target/huaxia-gateway-1.0-SNAPSHOT.jar ../huaxia-jar/gateway/huaxia-gateway.jar
copy ../../../huaxia-modules/huaxia-assistant/target/huaxia-assistant-1.0-SNAPSHOT.jar ../huaxia-jar/assistant/huaxia-assistant.jar
copy ../../../huaxia-modules/huaxia-term/target/huaxia-term-1.0-SNAPSHOT.jar ../huaxia-jar/term/huaxia-term.jar
copy ../../../huaxia-modules/huaxia-translate/target/huaxia-translate-1.0-SNAPSHOT.jar ../huaxia-jar/translate/huaxia-translate.jar
copy ../../../huaxia-modules/huaxia-user/target/huaxia-user-1.0-SNAPSHOT.jar ../huaxia-jar/user/huaxia-user.jar

pause
