package org.example;

public class Constants {

    public static class botManagement{
        public static final int MINUETS =60000;
        public static final int TOTAL_TIME_TO_SEND_RESULTS=MINUETS*5;
        public static final String BOT_TOKEN = "7346451863:AAETk5-CKvVe9KrEYwtb5Kj69K0H_15mviU";
        public static final String BOT_USERNAME = "Survey_College_Bot";
    }

    public static class firstMessages{
        public static final String START_MESSAGE = "/start";
        public static final String CHECK_HI_HEBREW = "היי";
        public static final String CHECK_HI_ENGLISH = "Hi";


    }

public static class surveySetup{
    public static final String MAKE_SURVEY="Make A Survey";
    public static final String SEND_SURVEY_NOW="Send Survey Now";
    public static final String ADJUST_TIME="Adjust time";
    public static final String TYPE_QUESTION_MESSAGE="Type Question And Than Press Enter";
    public static final String TYPE_OPTIONS_MESSAGE="Start Typing Options - Enter Between Options";
    public static final String CHOSE_NUM_OF_QUESTIONS_MESSAGE="Please choose the number of questions (Between 1-3) for your survey";
    public static final String CHOSE_NUM_OF_OPTIONS_MESSAGE="Please choose the number of options (Between 2-4) for this Question";
    public static final String SURVEY_PREVIEW_MESSAGE = "This is How Your Survey Will Look Like:";
    public static final String TYPE_NEXT_QUESTION_MSG = "Please Type the next question";
    public static final String SURVEY_SENT_MSG = "Survey Has Been Sent To All members!";
    public static final String MIN_MEMBERS_TO_STARTS_MSG="Make Sure that There are at least 3 Members in the Channel";
    public static final String ENTER_TIME_TO_SEND_MSG="Please Enter you Time In Minutes";
    public static final String ELSE_S_TWO_MESSAGE="Please Type Number Between 1-3";
    public static final String ELSE_S_FOUR_MESSAGE="Please Type Number Between 2-4";
}

    public static class response{
        public static final String ELSE_S_ONE_MESSAGE = "Please Register First by Typing /start or Hi/היי";

        public static  final String ALREADY_REGISTERED_MESSAGE = "You Are Already Registered !";
        public static final String WAITING_MESSAGE="Waiting 5 Minutes For Results Or That All Members Voted";
        public static final String JOINED_MESSAGE = "Has Joined to Chanel! The Total Members are: ";
    }
}
