package telegramBot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegramBot.client.ClientApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static telegramBot.BotConstants.*;

public class TelegramBot extends TelegramLongPollingBot {

    //private ClientApi client = new ClientApi();
    //private Map<String, Currency> contex = new ConcurrentHashMap<>();
    private Map<String, List<String>> userSelectedCurrencies = new ConcurrentHashMap<>();


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

            toggleCurrencySelection(chatId, callbackData);

            SendMessage message = new SendMessage(chatId, createSelectionMessage(chatId));
            message.setReplyMarkup(setupCurrencyButtons(chatId));
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().equalsIgnoreCase(START)) {
            String chatId = update.getMessage().getChatId().toString();
            SendMessage message = new SendMessage(chatId, "Виберіть валюту:");
            message.setReplyMarkup(setupCurrencyButtons(chatId));
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void toggleCurrencySelection(String chatId, String currency) {
        List<String> selections = userSelectedCurrencies.computeIfAbsent(chatId, k -> new ArrayList<>());
        if (selections.contains(currency)) {
            selections.remove(currency);
        } else {
            selections.add(currency);
        }
    }

    private String createSelectionMessage(String chatId) {
        List<String> selections = userSelectedCurrencies.getOrDefault(chatId, new ArrayList<>());
        if (selections.isEmpty()) {
            return "Валюта не вибрана.";
        } else {
            return "Ви вибрали валюту: " + String.join(", ", selections);
        }
    }

    @Override
    public String getBotUsername() {
        return JAVA_CURRENCY_BOT;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }


    private InlineKeyboardMarkup setupCurrencyButtons(String chatId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        String selectedEmoji = " ✅";

        InlineKeyboardButton dollarButton = new InlineKeyboardButton();
        dollarButton.setText("USD" + (userSelectedCurrencies.getOrDefault(chatId, new ArrayList<>()).contains("USD") ? selectedEmoji : ""));
        dollarButton.setCallbackData("USD");

        InlineKeyboardButton euroButton = new InlineKeyboardButton();
        euroButton.setText("EUR" + (userSelectedCurrencies.getOrDefault(chatId, new ArrayList<>()).contains("EUR") ? selectedEmoji : ""));
        euroButton.setCallbackData("EUR");

        rowsInline.add(List.of(dollarButton));  // USD button in the first row
        rowsInline.add(List.of(euroButton));  // EUR button in the second row

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }


}
