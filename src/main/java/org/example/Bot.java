package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.stream.Collectors;

import static org.example.Constants.botManagement.*;
import static org.example.Constants.firstMessages;
import static org.example.Constants.firstMessages.START_MESSAGE;
import static org.example.Constants.response.*;
import static org.example.Constants.surveySetup.*;
import static org.example.UserState.*;

public class Bot extends TelegramLongPollingBot {
    private List<User> usersList;
    private Question question;
    private List<Question> questionList;
    private Map<String, UserState> userStates;
    private int numOfOptionsPerQuestion;
    private int numberOfQuestions;
    private String surveyCreatorChatId;
    private int timeToSend;
    private List<String> votedUsers;
    private Map<String, Integer> pollIdToQuestionId;
    private boolean createdPoll;

    private Map<Integer, Integer> questionResults;
    private Map<Integer, Map<Integer, Integer>> allQuestionResults;
    private Map<String, Integer> userVoteCount ;




    public Bot() {
        this.usersList = new ArrayList<>();
        this.userStates = new HashMap<>();
        this.question = new Question();
        this.numOfOptionsPerQuestion = 0;
        this.numberOfQuestions = 0;
        this.createdPoll = false;
        this.pollIdToQuestionId = new HashMap<>();
        this.questionList = new LinkedList<>();
        this.timeToSend=0;
        this.votedUsers = new ArrayList<>();
        this.questionResults=new HashMap<>();
        this.allQuestionResults = new HashMap<>();
        this.userVoteCount=new HashMap<>();
    }

    @Override
    public void onUpdateReceived(Update update) {
    if (update.hasPollAnswer()) {
          String pollId = update.getPollAnswer().getPollId();
            List<Integer> selectedOptions = update.getPollAnswer().getOptionIds();
            String chatId = update.getPollAnswer().getUser().getId().toString();
        int currentCount = userVoteCount.getOrDefault(chatId, 0) + 1;
        userVoteCount.put(chatId, currentCount);

        if (currentCount == this.questionList.size()) {
            votedUsers.add(chatId);
        }
            Integer questionId = pollIdToQuestionId.get(pollId);
            if (questionId != null) {
                Map<Integer, Integer> questionResults = allQuestionResults.computeIfAbsent(questionId, k -> new HashMap<>());
                for (Integer option : selectedOptions) {
                    questionResults.put(option, questionResults.getOrDefault(option, 0) + 1);
                }

            }
    }


    String message = update.getMessage().getText();
    String name = update.getMessage().getFrom().getFirstName();
    String lastName = update.getMessage().getFrom().getLastName();
    String chatId = update.getMessage().getChatId().toString();
    User user = new User(name, lastName, chatId);
    SendMessage response = new SendMessage();
    response.setChatId(update.getMessage().getChatId().toString());
    String userQuestion = "";
    System.out.println(usersList);
    UserState state = userStates.getOrDefault(chatId, START);

    ////////////////////////////////////////////////////////
    System.out.println(message);
    System.out.println("Update received: " + update);

    if (!userStates.containsKey(chatId)) {
        userStates.put(chatId, START);
    }

    if (message.equals(START_MESSAGE) || containHiWord(message)) {
        registerToChancel(user, chatId);

    } else {

        switch (state) {
            case START -> {
                    if (message.equals(MAKE_SURVEY) && !createdPoll) {
                        if (hasAtLeastThreeMembers()) {
                            surveyCreatorChatId = chatId;
                            sendNumberOfQuestionSurveyMenu(chatId);
                            createdPoll = true;
                            userStates.put(chatId, STAGE_1_CHOSE_NUM_OF_QUESTION);
                        }
                        else {
                            sendMessageToUser(chatId,MIN_MEMBERS_TO_STARTS_MSG);
                        }
                    }
                else {
                    sendMessageToUser(chatId, ELSE_S_ONE_MESSAGE);
                }
            }
            case STAGE_1_CHOSE_NUM_OF_QUESTION -> {
                if (message.matches("[1-3]")) {
                    sendMessageToUser(chatId, TYPE_QUESTION_MESSAGE);
                    numberOfQuestions = Integer.parseInt(message);
                    userStates.put(chatId, STAGE_2_SEND_NUM_OF_OPTIONS);
                } else {
                    sendMessageToUser(chatId, ELSE_S_TWO_MESSAGE);
                }
            }
            case STAGE_2_SEND_NUM_OF_OPTIONS -> {
                userQuestion = message;
                question.addQuestion(userQuestion);
                sendMessageToUser(chatId, "You're Question: " + question.getQuestion());
                sendNumberOptionsPerQuestion(chatId);
                userStates.put(chatId, STAGE_3_CHOSE_NUM_OF_OPTIONS);

            }

            case STAGE_3_CHOSE_NUM_OF_OPTIONS -> {
                if (message.matches("[2-4]")) {
                    numOfOptionsPerQuestion = Integer.parseInt(message);
                    sendMessageToUser(chatId, TYPE_OPTIONS_MESSAGE);
                    sendMessageToUser(chatId, numOfOptionsPerQuestion + " ");
                    userStates.put(chatId, STAGE_4_ADD_QUEST_OR_OPTIONS);
                } else {
                    sendMessageToUser(chatId, ELSE_S_FOUR_MESSAGE);
                }

            }
            case STAGE_4_ADD_QUEST_OR_OPTIONS -> {
                question.addOption(message);
                if (question.getOptions().size() == numOfOptionsPerQuestion ) {
                    sendMessageToUser(chatId, " " + question.toString());
                    questionList.add(question);
                    sendMessageToUser(chatId, "Total number of questions: " + questionList.size() + "/" + numberOfQuestions);

                    if (questionList.size() < numberOfQuestions) {
                        question = new Question();
                        sendMessageToUser(chatId, TYPE_NEXT_QUESTION_MSG);
                        userStates.put(chatId, STAGE_2_SEND_NUM_OF_OPTIONS);
                    }
                    else {
                        sendMessageToUser(chatId, SURVEY_PREVIEW_MESSAGE);
                        printSurveyWithOutPoll(chatId);
                        menuToSendSurvey(chatId);
                        userStates.put(chatId, STAGE_5_SELECT_TIME_TO_SEND);

                    }


                }

        }


            case STAGE_5_SELECT_TIME_TO_SEND -> {
                if (message.equals(SEND_SURVEY_NOW)) {
                    sendSurvey(timeToSend);
                    sendMessageToUser(chatId, SURVEY_SENT_MSG);
                    userStates.put(chatId,STAGE_7_WAIT_FOR_RESULTS);
                } else if (message.equals(ADJUST_TIME)) {
                    sendMessageToUser(chatId,ENTER_TIME_TO_SEND_MSG);
                   userStates.put(chatId, STAGE_6_CHOSE_MINUTES);
                }

            }
            case STAGE_6_CHOSE_MINUTES -> {
               timeToSend=Integer.parseInt(message);
               sendSurvey(timeToSend);
                sendMessageToUser(chatId,"The Survey Will Be Sent In " + timeToSend + " Minutes");
               userStates.put(chatId, STAGE_7_WAIT_FOR_RESULTS);

            }

            case STAGE_7_WAIT_FOR_RESULTS -> {


            }

        }


    }

    try {
        execute(response);
    } catch (TelegramApiException e) {
        e.printStackTrace();
    }

    }

    private synchronized void printQuestionResults(int questionId, Map<Integer, Integer> questionResults) {
        Question question = questionList.get(questionId - 1);
        Map<Integer, Integer> resultsMap = new HashMap<>();
        int totalVotes = 0;
        for (int i = 0; i < question.getOptions().size(); i++) {
            int votes = questionResults.getOrDefault(i, 0);
            resultsMap.put(i, votes);
            totalVotes += votes;
        }
        List<Map.Entry<Integer, Integer>> sortedResults = new ArrayList<>(resultsMap.entrySet());
        sortedResults.sort((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()));

        StringBuilder resultsMessage = new StringBuilder();
        resultsMessage.append("Results for Question: ").append(question.getQuestion()).append("\n");

        for (Map.Entry<Integer, Integer> entry : sortedResults) {
            int optionId = entry.getKey();
            int votes = entry.getValue();
            double percentage = (votes / (double) totalVotes) * 100;

            resultsMessage.append("Option ").append(optionId + 1).append(": ")
                    .append(question.getOptions().get(optionId))
                    .append(" - ").append(String.format("%.2f", percentage)).append("%").append("\n");
        }

        sendMessageToUser(surveyCreatorChatId, resultsMessage.toString());
    }



    private synchronized void printAllQuestionResults() {
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : allQuestionResults.entrySet()) {
            Integer questionId = entry.getKey();
            Map<Integer, Integer> resultMap = entry.getValue();
            printQuestionResults(questionId, resultMap);
        }
        allQuestionResults.clear();
    }


    private void startSurveyTimeout(long sendTime) {
        new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < sendTime) {
                    synchronized (this) {
                        if (votedUsers.size() == usersList.size()) {
                            break;
                        }
                    }
                    Thread.sleep(2000); // בדיקה כל 2 שניות אם עוד הצביעו
                }
                    System.out.println("5 Minutes Pass");
                    printAllQuestionResults();
                    questionResults.clear();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public synchronized void sendSurvey(long time){
        new Thread(()->{
           try {
                   Thread.sleep(time * MINUETS);
               System.out.println(this.usersList.toString());
                   for (User currentUser : this.usersList) {
                       printedSurvey(currentUser.getChatId());
                   }
                   startSurveyTimeout(TOTAL_TIME_TO_SEND_RESULTS);
                   sendMessageToUser(surveyCreatorChatId,WAITING_MESSAGE );
           } catch (InterruptedException e) {
               throw new RuntimeException(e);
           }

       }).start();
    }

    public void printSurveyWithOutPoll(String chatId) {
        for (Question q : questionList) {
            sendMessageToUser(chatId, q.toString());
        }
    }

    public void printedSurvey(String chatId) {
        for (Question q : questionList) {
            sendSurveyWithPoll(chatId, q, q.getQuestionNumber());
        }
    }

    public void menuToSendSurvey(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Choose When To Send Survey:");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(SEND_SURVEY_NOW));
        row1.add(new KeyboardButton(ADJUST_TIME));
        keyboard.add(row1);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);


        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }


    public void sendNumberOfQuestionSurveyMenu(String chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("1"));
        row1.add(new KeyboardButton("2"));
        row1.add(new KeyboardButton("3"));
        keyboard.add(row1);
        keyboardMarkup.setKeyboard(keyboard);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setReplyMarkup(keyboardMarkup);  // הגדרת המקלדת החדשה בהודעה
        message.setText(CHOSE_NUM_OF_QUESTIONS_MESSAGE);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendNumberOptionsPerQuestion(String chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("2"));
        row1.add(new KeyboardButton("3"));
        row1.add(new KeyboardButton("4"));
        keyboard.add(row1);

        keyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setReplyMarkup(keyboardMarkup);
        message.setText(CHOSE_NUM_OF_OPTIONS_MESSAGE);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void sendMessageToUser(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void registerToChancel(User user, String chatId) {
        if (checkIfAlreadyRegistered(user)) {
            sendMessageToUser(chatId, ALREADY_REGISTERED_MESSAGE);
            sendReplyMenu(chatId);
        } else {
            updateUsersList(user);
            sendMessageToAllMembers("Greetings " + user + JOINED_MESSAGE + this.usersList.size());
            sendReplyMenu(chatId);
        }

    }


    public boolean containHiWord(String text) {
        if (text.equals(firstMessages.CHECK_HI_ENGLISH) || text.equals(firstMessages.CHECK_HI_HEBREW)) {
            return true;
        }
            return false;
        }


    public boolean hasAtLeastThreeMembers() {
        if (this.usersList.size() >= 3) {
            return true;
        }
        return false;
    }

    public boolean checkIfAlreadyRegistered(User user) {
        if (this.usersList.contains(user)) {
            return true;
        } else {
            return false;
        }
    }

    public void updateUsersList(User user) {
        this.usersList.add(user);
    }

    public void sendMessageToAllMembers(String text) {
        for (User user : this.usersList) {
            sendMessageToUser(user.getChatId(), text);
        }
    }


    public void sendSurveyWithPoll(String chatId, Question question, int questionId) {
        SendPoll poll=new SendPoll();
        poll.setChatId(chatId);
        poll.setQuestion(question.getQuestion());
        poll.setOptions(question.getOptions());
        poll.setAllowMultipleAnswers(false);
        poll.setIsAnonymous(false);
        try {

            Message sentPollMessage = execute(poll);
            String pollId = sentPollMessage.getPoll().getId();
            int questionNumber = question.getQuestionNumber();
            sendMessageToUser(chatId, "Question Number: " + questionId);
            pollIdToQuestionId.put(pollId, questionNumber);
        } catch (TelegramApiException e) {
            System.out.println("Failed to send poll to: " + chatId);
            e.printStackTrace();
        }
    }

    private void sendReplyMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Choose an option:");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(MAKE_SURVEY));
        keyboard.add(row1);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    public String getBotToken() {
        return BOT_TOKEN;
    }


}
