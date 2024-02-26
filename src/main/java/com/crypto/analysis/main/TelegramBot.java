package com.crypto.analysis.main;





import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
class TelegramBot extends TelegramLongPollingBot {

    final ValuesBot valuesBot;
    public TelegramBot(ValuesBot valuesBot) {
        this.valuesBot = valuesBot;
    }
    @Override
    public String getBotUsername() {
        return valuesBot.getBotName();
    }
    @Override
    public String getBotToken() {
        return valuesBot.getBotKey();
    }
    @Override
    public void onUpdateReceived(Update update) {
          if(update.getMessage().hasText()) {
              String TextMessage = update.getMessage().getText();
              long chatId = update.getMessage().getChatId();
              switch(TextMessage){
                  case "/start":
                      startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                default:sendMessage(chatId,"Suck suck");

              }
          }

    }
    private void startCommandReceived(long ChatId,String name){
     String answer = "Hi" + name + "Let's start the working";
     sendMessage(ChatId,answer);
    }
    private void sendMessage(long chatId,String text){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        try{
            execute(sendMessage);
        }catch (TelegramApiException exception){

        }
    }
}


